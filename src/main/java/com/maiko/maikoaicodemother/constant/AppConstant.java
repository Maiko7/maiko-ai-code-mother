package com.maiko.maikoaicodemother.constant;

/**
 * 应用相关常量定义
 */
public interface AppConstant {

    /**
     * 精选应用的优先级
     */
    Integer GOOD_APP_PRIORITY = 99;

    /**
     * 默认应用优先级
     */
    Integer DEFAULT_APP_PRIORITY = 0;

    /**
     * 应用生成目录
     */
    String CODE_OUTPUT_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 应用部署目录
     */
    String CODE_DEPLOY_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_deploy";

    /**
     * 应用部署域名
     */
    String CODE_DEPLOY_HOST = "http://localhost";

    /**
     * 默认应用封面图路径（静态资源）
     * 当应用的 cover 字段为空或截图失败时，使用此默认封面
     * Spring Boot 默认会把 src/main/resources/static 目录下的文件当作静态资源根目录。
     */
    String DEFAULT_COVER_URL = "/images/Mango.png";

}
