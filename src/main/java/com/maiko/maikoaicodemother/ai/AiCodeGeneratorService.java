package com.maiko.maikoaicodemother.ai;

import com.maiko.maikoaicodemother.ai.model.HtmlCodeResult;
import com.maiko.maikoaicodemother.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.SystemMessage;
import reactor.core.publisher.Flux;

/**
 * AI代码生成服务接口
 * <p>提供基于自然语言描述的代码生成功能</p>
 */
public interface AiCodeGeneratorService {

    /**
     * 根据用户描述生成HTML代码
     *
     * 虽然已经能够调用AI 生成代码，但直接返回字符串的方式不便于后续解析代码并保存为文件。因此我们需要
     * 将AI的输出转换为结构化的对象，利用 LangChain4j 的结构化输出特性可以轻松实现。
     * 这就是为什么不用版本1
     *
     * @param userMessage 用户提示词
     * @return AI输出的结果
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    HtmlCodeResult generateHtmlCode(String userMessage);
//版本1    String generateHtmlCode(String userMessage);

    /**
     * 根据用户描述流式生成多文件代码
     * @param userMessage 用户提示词
     * @return AI输出的结果
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(String userMessage);
//版本1    String generateMultiFileCode(String userMessage);


    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    Flux<String> generateHtmlCodeStream(String userMessage);
//版本1    String generateHtmlCode(String userMessage);

    /**
     * 根据用户描述流式生成多文件代码
     * @param userMessage 用户提示词
     * @return AI输出的结果
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    Flux<String> generateMultiFileCodeStream(String userMessage);
}
