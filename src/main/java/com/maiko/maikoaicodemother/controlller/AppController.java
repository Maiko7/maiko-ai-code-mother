package com.maiko.maikoaicodemother.controlller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.maiko.maikoaicodemother.annotation.AuthCheck;
import com.maiko.maikoaicodemother.common.BaseResponse;
import com.maiko.maikoaicodemother.common.DeleteRequest;
import com.maiko.maikoaicodemother.common.ResultUtils;
import com.maiko.maikoaicodemother.constant.AppConstant;
import com.maiko.maikoaicodemother.constant.UserConstant;
import com.maiko.maikoaicodemother.exception.BusinessException;
import com.maiko.maikoaicodemother.exception.ErrorCode;
import com.maiko.maikoaicodemother.exception.ThrowUtils;
import com.maiko.maikoaicodemother.model.dto.app.*;
import com.maiko.maikoaicodemother.model.entity.App;
import com.maiko.maikoaicodemother.model.entity.User;
import com.maiko.maikoaicodemother.model.vo.AppVO;
import com.maiko.maikoaicodemother.ratelimter.annotation.RateLimit;
import com.maiko.maikoaicodemother.ratelimter.enums.RateLimitType;
import com.maiko.maikoaicodemother.service.AppService;
import com.maiko.maikoaicodemother.service.ProjectDownloadService;
import com.maiko.maikoaicodemother.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 应用控制层
 *
 * @author 代码卡壳Maiko7
 */
@RestController
@RequestMapping("/app")
@Tag(name = "应用管理", description = "应用创建、更新、删除、查询等接口")
@Slf4j
public class AppController {

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    @Resource
    private ProjectDownloadService projectDownloadService;



    /**
     * 下载应用代码
     *
     * @param appId    应用ID
     * @param request  请求
     * @param response 响应
     */
    @GetMapping("/download/{appId}")
    @Operation(summary = "下载应用代码", description = "用户下载自己创建的应用源代码，以ZIP压缩包形式返回")
    public void downloadAppCode(@PathVariable Long appId,
                                HttpServletRequest request,
                                HttpServletResponse response) {
        // 1. 基础校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
        // 2. 查询应用信息
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 权限校验：只有应用创建者可以下载代码
        User loginUser = userService.getLoginUser(request);
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限下载该应用代码");
        }
        // 4. 构建应用代码目录路径（生成目录，非部署目录）
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        // 5. 检查代码目录是否存在
        File sourceDir = new File(sourceDirPath);
        ThrowUtils.throwIf(!sourceDir.exists() || !sourceDir.isDirectory(),
                ErrorCode.NOT_FOUND_ERROR, "应用代码不存在，请先生成代码");
        // 6. 生成下载文件名（不建议添加中文内容）
        String downloadFileName = String.valueOf(appId);
        // 7. 调用通用下载服务
        projectDownloadService.downloadProjectAsZip(sourceDirPath, downloadFileName, response);
    }




    /**
     * 应用聊天生成代码（流式 SSE）
     *
     * 关于 produces = MediaType.TEXT_EVENT_STREAM_VALUE 的作用：
     * 这个参数用于指定接口响应的媒体类型（Content-Type），具体说明如下：
     * 📌 作用：
     * 声明返回数据的格式为 text/event-stream（SSE，Server-Sent Events）
     * 告诉浏览器和客户端：这是一个流式接口，数据会分批持续返回，而不是一次性返回
     * 🎯 为什么需要它？
     * 流式传输：对于 AI 代码生成这种耗时操作，可以实时返回生成的每一段内容，用户体验更好
     * 浏览器识别：浏览器知道这是事件流，可以保持连接打开，持续接收数据
     * Content-Type 设置：自动设置响应头为 Content-Type: text/event-stream
     * 💡 与普通接口的区别：
     * 普通接口（如 @PostMapping）：一次性返回完整 JSON 响应
     * SSE 接口（加了 produces）：持续不断地推送数据片段，适合实时场景
     * 在这个例子中，AI 生成代码是逐字或逐段输出的，使用 SSE 可以让前端实时显示生成进度，就像看到 AI 在"打字"一样。
     *
     * @param appId   应用 ID
     * @param message 用户消息
     * @param request 请求对象
     * @return 生成结果流
     */
    @GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @RateLimit(limitType = RateLimitType.USER, rate = 5, rateInterval = 60, message = "AI对话请求过于频繁，请稍后再试")
    @Operation(summary = "聊天生成代码", description = "通过对话方式流式生成代码（SSE），实时返回生成进度")
    // 它这里为什么没封装对象，因为是get请求
    public Flux<ServerSentEvent<String>> chatToGenCode(@RequestParam Long appId, @RequestParam String message, HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 调用服务生成代码（流式）
        Flux<String> contentFlux = appService.chatToGenCode(appId, message, loginUser);
        // 转换为 ServerSentEvent 格式。将 AI 返回的原始文本片段，转换成前端能识别的标准 JSON 格式。
        return contentFlux
                .map(chunk -> {
                    // 将内容包装成JSON对象
                    /**
                     * 它这里为什么只是一个d而不是data，你想啊如果每次都是一个data那比返回的字符还多点
                     * 比如data: {"d": "你"}那你搞个data: {"data": "你"} 那不是data比你还多。
                     */
                    Map<String, String> wrapper = Map.of("d", chunk);
                    String jsonData = JSONUtil.toJsonStr(wrapper);
                    return ServerSentEvent.<String>builder()
                            .data(jsonData)
                            .build();
                })
                // 这段代码的作用是在数据流的最后，给前端发一个“结束信号”。
                .concatWith(Mono.just(
                        // 发送结束事件
                        ServerSentEvent.<String>builder()
                                .event("done")
                                .data("")
                                .build()
                )) .onErrorResume(error -> {
                    log.error("SSE流式输出异常", error);
                    Map<String, String> errorWrapper = Map.of("error", error.getMessage());
                    ServerSentEvent<String> errorEvent = ServerSentEvent.<String>builder()
                            .event("error")
                            .data(JSONUtil.toJsonStr(errorWrapper))
                            .build();
                    ServerSentEvent<String> doneEvent = ServerSentEvent.<String>builder()
                            .event("done")
                            .data("")
                            .build();
                    return Flux.just(errorEvent, doneEvent);
                });
    }

    /**
     * 应用部署
     *
     * @param appDeployRequest 部署请求
     * @param request          请求
     * @return 部署 URL
     */
    @PostMapping("/deploy")
    @Operation(summary = "部署应用", description = "将应用部署到服务器，返回部署后的访问地址")
    public BaseResponse<String> deployApp(@RequestBody AppDeployRequest appDeployRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appDeployRequest == null, ErrorCode.PARAMS_ERROR);
        Long appId = appDeployRequest.getAppId();
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 调用服务部署应用
        String deployUrl = appService.deployApp(appId, loginUser);
        return ResultUtils.success(deployUrl);
    }



    /**
     * 创建应用
     *
     * @param appAddRequest 创建应用请求
     * @param request       请求
     * @return 应用 id
     */
    @PostMapping("/add")
    @Operation(summary = "创建应用", description = "用户创建新的应用，需要提供初始化prompt")
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        Long appId = appService.createApp(appAddRequest, loginUser);
        return ResultUtils.success(appId);
    }

    /**
     * 更新应用（用户只能更新自己的应用名称）
     *
     * @param appUpdateRequest 更新请求
     * @param request          请求
     * @return 更新结果
     */
    @PostMapping("/update")
    @Operation(summary = "更新应用", description = "用户更新自己创建的应用名称")
    public BaseResponse<Boolean> updateApp(@RequestBody AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        if (appUpdateRequest == null || appUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long id = appUpdateRequest.getId();
        // 判断是否存在
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人可更新
        if (!oldApp.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        App app = new App();
        app.setId(id);
        app.setAppName(appUpdateRequest.getAppName());
        // 设置编辑时间
        app.setEditTime(LocalDateTime.now());
        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 删除应用（用户只能删除自己的应用）
     *
     * @param deleteRequest 删除请求
     * @param request       请求
     * @return 删除结果
     */
    @PostMapping("/delete")
    @Operation(summary = "删除应用", description = "用户删除自己创建的应用")
    public BaseResponse<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldApp.getUserId().equals(loginUser.getId()) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 它AppService重写了removeById方法，会删除关联的对话历史和应用封面。
        boolean result = appService.removeById(id);
        return ResultUtils.success(result);
    }


    /**
     * 根据 id 获取应用详情
     *
     * @param id      应用 id
     * @return 应用详情
     */
    @GetMapping("/get/vo")
    @Operation(summary = "获取应用详情", description = "根据ID获取应用的详细信息")
    public BaseResponse<AppVO> getAppVOById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类（包含用户信息）
        return ResultUtils.success(appService.getAppVO(app));
    }

    /**
     * 分页获取当前用户创建的应用列表
     *
     * @param appQueryRequest 查询请求
     * @param request         请求
     * @return 应用列表
     */
    @PostMapping("/my/list/page/vo")
    @Operation(summary = "获取我的应用列表", description = "分页获取当前用户创建的应用列表")
    public BaseResponse<Page<AppVO>> listMyAppVOByPage(@RequestBody AppQueryRequest appQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        // 限制每页最多 20 个
        long pageSize = appQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 个应用");
        long pageNum = appQueryRequest.getPageNum();
        // 只查询当前用户的应用
        appQueryRequest.setUserId(loginUser.getId());
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
        // 数据封装
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }


    /**
     * 分页获取精选应用列表
     *
     * @param appQueryRequest 查询请求
     * @return 精选应用列表
     */
    @PostMapping("/good/list/page/vo")
    @Cacheable(
            value = "good_app_page",
            key = "T(com.maiko.maikoaicodemother.utils.CacheKeyUtils).generateKey(#appQueryRequest)",
            condition = "#appQueryRequest.pageNum <= 10"
    )
    @Operation(summary = "获取精选应用列表", description = "分页获取精选的优质应用列表")
    public BaseResponse<Page<AppVO>> listGoodAppVOByPage(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 限制每页最多 20 个
        long pageSize = appQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 个应用");
        long pageNum = appQueryRequest.getPageNum();
        // 只查询精选的应用
        appQueryRequest.setPriority(AppConstant.GOOD_APP_PRIORITY);
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        // 分页查询
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
        // 数据封装
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 管理员删除应用
     *
     * @param deleteRequest 删除请求
     * @return 删除结果
     */
    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "管理员删除应用", description = "管理员删除任意应用")
    public BaseResponse<Boolean> deleteAppByAdmin(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        // 判断是否存在
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = appService.removeById(id);
        return ResultUtils.success(result);
    }

    /**
     * 管理员更新应用
     *
     * @param appAdminUpdateRequest 更新请求
     * @return 更新结果
     */
    @PostMapping("/admin/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "管理员更新应用", description = "管理员更新任意应用的信息")
    public BaseResponse<Boolean> updateAppByAdmin(@RequestBody AppAdminUpdateRequest appAdminUpdateRequest) {
        if (appAdminUpdateRequest == null || appAdminUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = appAdminUpdateRequest.getId();
        // 判断是否存在
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        App app = new App();
        BeanUtil.copyProperties(appAdminUpdateRequest, app);
        // 设置编辑时间
        app.setEditTime(LocalDateTime.now());
        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 管理员分页获取应用列表
     *
     * @param appQueryRequest 查询请求
     * @return 应用列表
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "管理员获取应用列表", description = "管理员分页查询所有应用列表")
    public BaseResponse<Page<AppVO>> listAppVOByPageByAdmin(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
        // 数据封装
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 管理员根据 id 获取应用详情
     *
     * @param id 应用 id
     * @return 应用详情
     */
    @GetMapping("/admin/get/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "管理员获取应用详情", description = "管理员根据ID获取应用的完整信息")
    public BaseResponse<AppVO> getAppVOByIdByAdmin(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(appService.getAppVO(app));
    }




}
