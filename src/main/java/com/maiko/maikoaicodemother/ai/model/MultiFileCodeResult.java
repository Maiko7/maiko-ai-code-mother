package com.maiko.maikoaicodemother.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * 【类定义】多文件代码结果对象
 *
 * 作用：
 *   这是一个标准的 Java Bean（POJO），用来存储解析出来的多文件代码数据。
 *   它把 HTML、CSS、JS 三种代码分别存储，方便后续分别写入不同的文件。
 *
 * 关键注解：
 *   @Description：这是 LangChain4j 框架的注解。
 *   作用：它告诉 AI 模型，“当你生成 JSON 或结构化输出时，这个字段代表什么意思”。
 *   比如，AI 看到 `htmlCode` 字段上的 @Description，就会明白这里应该填入 HTML 源码。
 */
@Data // Lombok 注解，自动生成 Getter、Setter、toString 等方法
public class MultiFileCodeResult {

    /**
     * 【核心字段】HTML 代码内容
     *
     * 内容示例：
     * <html>
     *   <head><link rel="stylesheet" href="style.css"></head>
     *   <body><h1>Hello World</h1></body>
     * </html>
     */
    @Description("HTML代码")
    private String htmlCode;

    /**
     * 【核心字段】CSS 代码内容
     *
     * 内容示例：
     * body {
     *   background-color: #f0f0f0;
     *   font-family: Arial, sans-serif;
     * }
     */
    @Description("CSS代码")
    private String cssCode;

    /**
     * 【核心字段】JavaScript 代码内容
     *
     * 内容示例：
     * document.addEventListener('DOMContentLoaded', function() {
     *   console.log('页面加载完成');
     * });
     */
    @Description("JS代码")
    private String jsCode;

    /**
     * 【辅助字段】代码描述
     *
     * 作用：存储这段代码的功能简介，比如“一个带样式的登录页面”。
     * 场景：前端展示代码预览时，可以用这个字段作为标题或备注。
     */
    @Description("生成代码的描述")
    private String description;
}