package com.maiko.maikoaicodemother.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * 【类定义】HTML 代码结果对象
 *
 * 作用：
 *   这是一个标准的 Java Bean（POJO），用来存储解析出来的 HTML 代码数据。
 *   它不仅包含代码本身，还预留了描述字段，方便后续展示或日志记录。
 *
 * 关键注解：
 *   @Description：这是 LangChain4j 框架的注解。
 *   作用：它告诉 AI 模型，“当你生成 JSON 或结构化输出时，这个字段代表什么意思”。
 *   比如，AI 看到 `htmlCode` 字段上的 @Description，就会明白这里应该填入实际的 HTML 源码。
 */
@Data // Lombok 注解，自动生成 Getter、Setter、toString 等方法
public class HtmlCodeResult {

    /**
     * 【核心字段】HTML 代码内容
     *
     * 内容示例：
     * <html>
     *   <body><h1>Hello World</h1></body>
     * </html>
     */
    @Description("HTML代码")
    private String htmlCode;

    /**
     * 【辅助字段】代码描述
     *
     * 作用：存储这段代码的功能简介，比如“这是一个红色的按钮”。
     * 场景：前端展示代码预览时，可以用这个字段作为标题或备注。
     */
    @Description("生成代码的描述")
    private String description;
}