package com.maiko.maikoaicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.maiko.maikoaicodemother.ai.AiCodeGenTypeRoutingService;
import com.maiko.maikoaicodemother.ai.AiCodeGenTypeRoutingServiceFactory;
import com.maiko.maikoaicodemother.config.CosClientConfig;
import com.maiko.maikoaicodemother.constant.AppConstant;
import com.maiko.maikoaicodemother.core.AiCodeGeneratorFacade;
import com.maiko.maikoaicodemother.core.builder.VueProjectBuilder;
import com.maiko.maikoaicodemother.core.handler.StreamHandlerExecutor;
import com.maiko.maikoaicodemother.exception.BusinessException;
import com.maiko.maikoaicodemother.exception.ErrorCode;
import com.maiko.maikoaicodemother.exception.ThrowUtils;
import com.maiko.maikoaicodemother.innerservice.InnerScreenshotService;
import com.maiko.maikoaicodemother.innerservice.InnerUserService;
import com.maiko.maikoaicodemother.manager.CosManager;
import com.maiko.maikoaicodemother.mapper.AppMapper;
import com.maiko.maikoaicodemother.model.dto.app.AppAddRequest;
import com.maiko.maikoaicodemother.model.dto.app.AppQueryRequest;
import com.maiko.maikoaicodemother.model.entity.App;
import com.maiko.maikoaicodemother.model.entity.User;
import com.maiko.maikoaicodemother.model.enums.ChatHistoryMessageTypeEnum;
import com.maiko.maikoaicodemother.model.enums.CodeGenTypeEnum;
import com.maiko.maikoaicodemother.model.vo.AppVO;
import com.maiko.maikoaicodemother.model.vo.UserVO;
import com.maiko.maikoaicodemother.service.AppService;
import com.maiko.maikoaicodemother.service.ChatHistoryService;
import com.maiko.maikoaicodemother.service.ChatSummaryService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author 代码卡壳Maiko7
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

    @Resource
    @Lazy
    private InnerUserService userService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private ChatSummaryService chatSummaryService;

    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    @Resource
    @Lazy
    private InnerScreenshotService screenshotService;

    @Resource
    private CosManager cosManager;

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private AiCodeGenTypeRoutingServiceFactory aiCodeGenTypeRoutingServiceFactory;

    @Override
    public Long createApp(AppAddRequest appAddRequest, User loginUser) {
        // 参数校验
        String initPrompt = appAddRequest.getInitPrompt();
        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化 prompt 不能为空");
        // 构造入库对象
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        app.setUserId(loginUser.getId());
        // 应用名称暂时为 initPrompt 前 12 位
        app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 12)));
        // 使用 AI 智能选择代码生成类型（多例模式）
        AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService = aiCodeGenTypeRoutingServiceFactory.createAiCodeGenTypeRoutingService();
        CodeGenTypeEnum selectedCodeGenType = aiCodeGenTypeRoutingService.routeCodeGenType(initPrompt);
        app.setCodeGenType(selectedCodeGenType.getValue());
        // 插入数据库
        boolean result = this.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        log.info("应用创建成功，ID: {}, 类型: {}", app.getId(), selectedCodeGenType.getValue());
        return app.getId();
    }


    /**
     * 流式聊天生成代码
     * <p>
     * 核心流程：
     * 1. 校验参数合法性（AppId、消息内容）
     * 2. 校验应用存在性及用户权限（仅拥有者可操作）
     * 3. 获取应用的代码生成类型（HTML / 多文件 / Vue项目）
     * 4. 记录用户消息到对话历史
     * 5. 调用 AI 服务进行流式代码生成
     * 6. 监听流式响应：
     *    - 实时转发数据块给前端
     *    - 收集完整响应内容
     *    - 完成后保存 AI 回复到对话历史
     *    - 增加应用对话轮数
     *    - 检查并触发智能总结（异步）
     *    - 异常时记录错误信息
     * </p>
     *
     * @param appId      应用唯一标识
     * @param message    用户输入的消息内容
     * @param loginUser  当前登录用户信息
     * @return 包含代码片段的流式响应（Flux<String>）
     */
    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限访问该应用，仅本人可以生成代码
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
        }
        // 4. 获取应用的代码生成类型
        String codeGenTypeStr = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenTypeStr);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        }
        // 5. 通过校验后，添加用户消息到对话历史
        chatHistoryService.addChatMessage(appId, message, ChatHistoryMessageTypeEnum.USER.getValue(), loginUser.getId());

        // 7. 调用 AI 生成代码（流式）
        Flux<String> contentFlux = aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
        // 8. 收集AI响应内容并在完成后记录到对话历史
        Flux<String> resultFlux = streamHandlerExecutor.doExecute(contentFlux, chatHistoryService, appId, loginUser, codeGenTypeEnum);

        // 【关键修改】在这里继续链式调用，追加业务逻辑
        return resultFlux
                .doOnComplete(() -> {
                    /**
                     * Q：为什么不在SimpleTextStreamHandler里直接写 incrementAppTotalRounds 和 checkAndSummarizeIfNeeded 呢？
                     * A：如果你在 SimpleTextStreamHandler 里写了 incrementAppTotalRounds：
                     * 耦合度变高：Handler 就必须依赖 AppService。
                     * 通用性变差：万一以后有个 PythonStreamHandler 也要用这个 Handler，但 Python 项目不需要“增加轮数”，那怎么办？你就得在 Handler 里写一堆 if-else 判断。
                     * 职责混乱：一个负责“搬运数据”的类，突然跑去管“账户余额（轮数）”，这就不专业了。
                     *
                     * 展开式就是：
                     * Runnable myAction = new Runnable() {
                     *     @Override
                     *     public void run() {
                     *         // 2. 这里写具体的业务逻辑
                     *         this.incrementAppTotalRounds(appId);
                     *         this.checkAndSummarizeIfNeeded(appId, loginUser);
                     *     }
                     * };
                     * resultFlux.doOnComplete(myAction);
                     */


                    // 当流彻底传输完成后（Handler 已经存完库了），执行以下业务逻辑：

                    // 1. 增加应用对话轮数
                    this.incrementAppTotalRounds(appId);
                    // 2. 检查是否需要智能总结
                    this.checkAndSummarizeIfNeeded(appId, loginUser);
                    log.info("对话处理全流程结束，轮数已增加，总结检查完毕");
                });
//        StringBuilder aiResponseBuilder = new StringBuilder();
//        return contentFlux
//                .map(chunk -> {
//                    // 收集AI响应内容
//                    aiResponseBuilder.append(chunk);
//                    return chunk;
//                })
//                .doOnComplete(() -> {
//                    // 流式响应完成后，添加AI消息到对话历史
//                    String aiResponse = aiResponseBuilder.toString();
//                    if (StrUtil.isNotBlank(aiResponse)) {
//                        chatHistoryService.addChatMessage(appId, aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
//                        // 对话成功后，增加应用的对话轮数
//                        incrementAppTotalRounds(appId);
//                        // 检查是否需要进行智能总结（异步执行，不阻塞主流程）
//                        checkAndSummarizeIfNeeded(appId, loginUser);
//                    }
//                })
//                .doOnError(error -> {
//                    // 如果AI回复失败，也要记录错误消息
//                    String errorMessage = "AI回复失败: " + error.getMessage();
//                    chatHistoryService.addChatMessage(appId, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
//                    // 注意：失败不计入轮数
//                });

    }

    @Override
    public String deployApp(Long appId, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限部署该应用，仅本人可以部署
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限部署该应用");
        }
        // 4. 检查是否已有 deployKey
        String deployKey = app.getDeployKey();
        // 没有则生成 6 位 deployKey（大小写字母 + 数字）
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }
        // 5. 获取代码生成类型，构建源目录路径（获取原本的代码生成的位置）
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        // 6. 检查源目录是否存在（有可能存在应用有，网站生成失败了，则目录不存在）
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码不存在，请先生成代码");
        }

        // 7. Vue项目特殊处理，执行构建
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (codeGenTypeEnum == CodeGenTypeEnum.VUE_PROJECT) {
            // Vue项目需要构建（这里不调用异步是因为这是用户自己点的部署，肯定希望看到结果，而不是异步。）
            boolean buildSuccess = vueProjectBuilder.buildProject(sourceDirPath);
            ThrowUtils.throwIf(!buildSuccess, ErrorCode.SYSTEM_ERROR, "Vue项目构建失败，请重试");
            File distDir = new File(sourceDirPath, "dist");
            ThrowUtils.throwIf(!distDir.exists() || !distDir.isDirectory(), ErrorCode.SYSTEM_ERROR, "Vue项目构建失败，请重试");
            // 构建完成后，需要将构建后的文件复制到部署目录
            sourceDir = distDir;
        }
        // 8. 复制文件到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署失败：" + e.getMessage());
        }
        // 9. 更新应用的 deployKey 和部署时间
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");
        // 10. 构建应用访问 URL
        String appDeployUrl = String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);
        // 11. 异步生成截图并更新应用封面
        generateAppScreenshotAsync(appId, appDeployUrl);
        return appDeployUrl;

    }


    /**
     * 异步生成应用截图并更新封面
     *
     * 核心场景：
     * 当用户保存或发布应用时，我们需要给这个应用生成一个“缩略图”作为封面。
     * 因为截图非常耗时（需要启动浏览器、渲染页面、上传文件），
     * 所以不能让用户在前端傻等。我们采用“发后即忘”的策略，后台慢慢处理。
     *
     * @param appId  应用ID（数据库主键）
     * @param appUrl 应用的访问地址（用来打开网页截图）
     */
    @Override
    public void generateAppScreenshotAsync(Long appId, String appUrl) {
        // --- 【关键】开启异步线程 ---
        // 使用 Java 21 的虚拟线程（Virtual Thread）。
        // 相比传统线程，它更轻量级，不会因为频繁创建销毁而拖垮服务器。
        // 这行代码的意思是：“别管我，我在后台新开一个小号去干活，主线程立马返回去响应用户。”
        Thread.startVirtualThread(() -> {
            try {
                // 1. 调用截图服务
                // 这一步是同步阻塞的（打开浏览器 -> 截图 -> 上传COS -> 删本地文件）。
                // 但因为我们在虚拟线程里，所以不会卡住外面的主程序。
                String screenshotUrl = screenshotService.generateAndUploadScreenshot(appUrl);

                // 2. 准备更新数据库
                // 拿到云端返回的图片链接后，我们要把它存进 `app` 表的 `cover` 字段里。
                App updateApp = new App();
                updateApp.setId(appId);          // 指定是哪条记录
                updateApp.setCover(screenshotUrl); // 设置新的封面图链接

                // 3. 执行更新
                boolean updated = this.updateById(updateApp);

                // 4. 校验结果
                // 如果更新失败（比如数据库锁、连接断开），直接抛异常。
                // 注意：这里的异常只会打印在日志里，不会传给前端，因为这是异步线程。
                ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "更新应用封面字段失败");

                log.info("应用封面更新成功: appId={}, url={}", appId, screenshotUrl);

            } catch (Exception e) {
                // 【兜底】捕获异常
                // 异步线程最怕静默失败（报错了但没人知道）。
                // 万一截图失败了（比如网页打不开），这里必须记日志，方便排查。
                log.error("异步更新应用封面失败: appId={}, error={}", appId, e.getMessage(), e);
            }
        });
    }



    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        
        // 【封面图兜底逻辑】如果 cover 字段为空，使用默认封面
        if (StrUtil.isBlank(app.getCover())) {
            appVO.setCover(AppConstant.DEFAULT_COVER_URL);
            log.debug("应用 {} 的封面为空，使用默认封面: {}", app.getId(), AppConstant.DEFAULT_COVER_URL);
        }
        
        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList.stream()
                //版本1 .map(app -> app.getUserId()) 当你不需要任何参数的时候可以直接写成App::getUserId
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }

    /**
     * 删除应用时关联删除对话历史和云端封面文件
     *
     * @param id 应用ID
     * @return 是否成功
     */
    @Override
    public boolean removeById(Serializable id) {
        if (id == null) {
            return false;
        }
        // 转换为 Long 类型
        long appId = Long.parseLong(id.toString());
        if (appId <= 0) {
            return false;
        }
        
        // 【资源清理1】先查询应用信息，获取云端封面文件的 key
        App app = this.getById(appId);
        if (app != null && StrUtil.isNotBlank(app.getCover())) {
            deleteCoverFromCos(app.getCover());
        }
        
        // 【资源清理2】删除关联的对话历史
        try {
            chatHistoryService.deleteByAppId(appId);
        } catch (Exception e) {
            // 记录日志但不阻止应用删除
            log.error("删除应用关联对话历史失败: {}", e.getMessage());
        }
        
        // 删除应用
        return super.removeById(id);
    }

    /**
     * 从 COS 中删除应用的封面文件
     *
     * @param coverUrl 完整的封面图 URL
     */
    private void deleteCoverFromCos(String coverUrl) {
        try {
            // 从完整 URL 中提取 COS key
            // 例如: https://xxx.cos.ap-guangzhou.myqcloud.com/screenshots/2026/04/16/abc.jpg
            // 提取出: /screenshots/2026/04/16/abc.jpg
            String host = cosClientConfig.getHost();
            String key = coverUrl.replace("https://" + host, "");
            
            // 如果替换后没有变化，说明格式不匹配，尝试http格式
            if (key.equals(coverUrl)) {
                key = coverUrl.replace("http://" + host, "");
            }
            
            // 如果还是没变化，可能是相对路径，直接使用
            if (!key.equals(coverUrl)) {
                boolean deleted = cosManager.deleteFile(key);
                if (deleted) {
                    log.info("应用云端封面删除成功: {}", coverUrl);
                } else {
                    log.warn("应用云端封面删除失败: {}", coverUrl);
                }
            } else {
                log.warn("无法解析封面URL，跳过删除: {}", coverUrl);
            }
        } catch (Exception e) {
            // 记录日志但不阻止应用删除
            log.error("删除应用云端封面异常: {}", coverUrl, e);
        }
    }

    /**
     * 增加应用的对话轮数
     *
     * @param appId 应用ID
     */
    private void incrementAppTotalRounds(Long appId) {
        try {
            App app = this.getById(appId);
            if (app != null) {
                Integer currentRounds = app.getTotalRounds();
                if (currentRounds == null) {
                    currentRounds = 0;
                }
                app.setTotalRounds(currentRounds + 1);
                this.updateById(app);
                log.info("应用 {} 的对话轮数已更新为: {}", appId, app.getTotalRounds());
            }
        } catch (Exception e) {
            // 记录日志但不影响主流程
            log.error("更新应用对话轮数失败: {}", e.getMessage());
        }
    }

    /**
     * 检查是否需要进行智能总结，如果需要则异步执行
     *
     * @param appId     应用ID
     * @param loginUser 登录用户
     */
    private void checkAndSummarizeIfNeeded(Long appId, User loginUser) {
        try {
            // 检查是否需要总结
            if (chatSummaryService.shouldSummarize(appId)) {
                log.info("应用 {} 达到总结阈值，开始异步执行智能总结", appId);
                
                // 异步执行总结（不阻塞用户响应）
                new Thread(() -> {
                    try {
                        Long summaryId = chatSummaryService.summarizeChatHistory(appId, 10, loginUser);
                        if (summaryId != null) {
                            log.info("应用 {} 智能总结完成，总结ID: {}", appId, summaryId);
                        }
                    } catch (Exception e) {
                        log.error("应用 {} 智能总结失败", appId, e);
                    }
                }, "chat-summary-" + appId).start();
            }
        } catch (Exception e) {
            // 记录日志但不影响主流程
            log.error("检查智能总结条件失败: {}", e.getMessage());
        }
    }

}
