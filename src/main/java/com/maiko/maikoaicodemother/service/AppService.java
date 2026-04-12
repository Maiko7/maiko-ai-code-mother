package com.maiko.maikoaicodemother.service;

import com.maiko.maikoaicodemother.model.dto.app.AppQueryRequest;
import com.maiko.maikoaicodemother.model.entity.App;
import com.maiko.maikoaicodemother.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 应用服务层
 *
 * @author 代码卡壳Maiko7
 */
public interface AppService extends IService<App> {

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


}
