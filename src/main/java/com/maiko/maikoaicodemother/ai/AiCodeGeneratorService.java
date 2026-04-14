package com.maiko.maikoaicodemother.ai;

import com.maiko.maikoaicodemother.ai.model.HtmlCodeResult;
import com.maiko.maikoaicodemother.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

/**
 * 【接口定义】AI代码生成服务
 *
 * 作用：
 *   这是整个 AI 模块的“能力蓝图”。
 *   它定义了系统能做什么：生成 HTML、生成多文件代码、流式生成等。
 *
 * 核心技术：
 *   LangChain4j 的“AI Service”接口。
 *   你不需要写实现类！LangChain4j 会在运行时动态生成一个代理对象来实现这个接口。
 *   你只需要定义方法签名和注解，框架会自动帮你调用大模型、处理提示词、解析结果。
 */
public interface AiCodeGeneratorService {

    /**
     * 【功能1】生成 HTML 代码（结构化对象）
     *
     * @param userMessage 用户提示词，比如“生成一个红色的按钮”
     * @return HtmlCodeResult 对象，包含代码和描述
     *
     * 关键点：
     *   @SystemMessage：指定系统提示词，告诉 AI 它的角色和任务。
     *   返回值是 HtmlCodeResult：告诉 LangChain4j，把 AI 的输出解析成这个对象。
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    HtmlCodeResult generateHtmlCode(String userMessage);

    /**
     * 【功能2】生成多文件代码（结构化对象）
     *
     * @param userMessage 用户提示词，比如“生成一个带样式的登录页面”
     * @return MultiFileCodeResult 对象，包含 HTML、CSS、JS 代码
     *
     * 关键点：
     *   @SystemMessage：指定系统提示词，告诉 AI 要生成多文件代码。
     *   返回值是 MultiFileCodeResult：告诉 LangChain4j，把 AI 的输出解析成这个对象。
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(String userMessage);

    /**
     * 【功能3】流式生成 HTML 代码（字符串流）
     *
     * @param userMessage 用户提示词
     * @return Flux<String>，一个字符串流，每个元素是一段代码片段
     *
     * 关键点：
     *   返回值是 Flux<String>：告诉 LangChain4j，以流式方式返回结果。
     *   适用于前端实时显示生成过程。
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    Flux<String> generateHtmlCodeStream(String userMessage);

    /**
     * 【功能4】流式生成多文件代码（字符串流）
     *
     * @param userMessage 用户提示词
     * @return Flux<String>，一个字符串流，每个元素是一段代码片段
     *
     * 关键点：
     *   返回值是 Flux<String>：告诉 LangChain4j，以流式方式返回结果。
     *   适用于前端实时显示生成过程。
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    Flux<String> generateMultiFileCodeStream(String userMessage);

    /**
     * 【功能5】生成 Vue 项目代码（流式，带记忆）
     *
     * @param appId 应用ID，作为记忆ID，用于关联对话历史
     * @param userMessage 用户消息
     * @return Flux<String>，一个字符串流，每个元素是一段代码片段
     *
     * 关键点：
     *   @MemoryId：标记 appId 为记忆ID，LangChain4j 会自动管理对话历史。
     *   @UserMessage：标记 userMessage 为用户消息。
     *   @SystemMessage：指定系统提示词，告诉 AI 要生成 Vue 项目代码。
     *   返回值是 Flux<String>：告诉 LangChain4j，以流式方式返回结果。
     */
    @SystemMessage(fromResource = "prompt/codegen-vue-project-system-prompt.txt")
    Flux<String> generateVueProjectCodeStream(@MemoryId long appId, @UserMessage String userMessage);
}