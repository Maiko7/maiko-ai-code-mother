package com.maiko.maikoaicodemother.service;

import com.maiko.maikoaicodemother.model.dto.app.AppQueryRequest;
import com.maiko.maikoaicodemother.model.entity.App;
import com.maiko.maikoaicodemother.model.entity.User;
import com.maiko.maikoaicodemother.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用服务层
 *
 * @author 代码卡壳Maiko7
 */
public interface AppService extends IService<App> {

    void generateAppScreenshotAsync(Long appId, String appUrl);

    /**
     * 获取应用封装类
     * @param app
     * @return
     */
    AppVO getAppVO(App app);


    /**
     * 根据查询请求构建查询包装器
     *
     * @param appQueryRequest 应用查询请求对象，包含查询条件
     * @return QueryWrapper 查询包装器对象，用于构建SQL查询条件
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 批量转换应用实体为视图对象
     *
     * @param appList 应用实体列表
     * @return List<AppVO> 应用视图对象列表
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 通过对话方式生成代码（流式返回）
     *
     * @param appId 应用ID，指定要生成代码的应用
     * @param message 用户输入的对话消息
     * @param loginUser 当前登录用户信息
     * @return Flux<String> 流式返回生成的代码片段
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    /**
     * 应用部署
     * @param appId 应用ID
     * @param loginUser 当前登录用户信息
     * @return
     */
    String deployApp(Long appId, User loginUser);
}
