package com.maiko.maikoaicodemother.ai.tools;

import cn.hutool.json.JSONObject;

/**
 * 工具基类
 * 定义所有工具的通用接口
 * <p>
 * 所有具体的工具类（如文件读取、文件写入等）都必须继承此类，
 * 并实现其抽象方法，以保证工具调用的统一性。
 */
public abstract class BaseTool {

    /**
     * 获取工具的英文名称（对应方法名）
     *
     * @return 工具英文名称（例如：file_read, file_write）
     */
    public abstract String getToolName();

    /**
     * 获取工具的中文显示名称
     *
     * @return 工具中文名称（例如：文件读取, 文件写入）
     */
    public abstract String getDisplayName();

    /**
     * 生成工具请求时的返回值（显示给用户）
     * <p>
     * 当AI决定调用工具时，前端或日志中会先显示此信息，
     * 告知用户即将执行什么操作。
     *
     * @return 工具请求显示内容
     */
    public String generateToolRequestResponse() {
        return String.format("\n\n[选择工具] %s\n\n", getDisplayName());
    }

    /**
     * 生成工具执行结果格式（保存到数据库）
     * <p>
     * 工具执行完毕后，将参数和结果格式化为统一字符串，
     * 用于存入对话历史记录。
     *
     * @param arguments 工具执行参数（JSON格式）
     * @return 格式化的工具执行结果
     */
    public abstract String generateToolExecutedResult(JSONObject arguments);
}