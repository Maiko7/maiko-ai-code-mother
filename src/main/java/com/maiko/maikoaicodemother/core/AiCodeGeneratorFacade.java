package com.maiko.maikoaicodemother.core;

import com.maiko.maikoaicodemother.ai.AiCodeGeneratorService;
import com.maiko.maikoaicodemother.ai.AiCodeGeneratorServiceFactory;
import com.maiko.maikoaicodemother.ai.model.HtmlCodeResult;
import com.maiko.maikoaicodemother.ai.model.MultiFileCodeResult;
import com.maiko.maikoaicodemother.core.parser.CodeParserExecutor;
import com.maiko.maikoaicodemother.core.saver.CodeFileSaverExecutor;
import com.maiko.maikoaicodemother.exception.BusinessException;
import com.maiko.maikoaicodemother.exception.ErrorCode;
import com.maiko.maikoaicodemother.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * 【类定义】AI代码生成器外观类
 *
 * 设计模式：门面模式
 * 作用：
 *   1. 它是整个系统的“前台”。外部调用者（比如 Controller）不需要知道后面有 AI 服务、解析器、保存器。
 *   2. 它负责把“生成代码”和“保存代码”这两个大步骤串联起来。
 *   3. 它根据传入的类型（HTML 还是 多文件），自动选择正确的执行路径。
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {

    /**
     * 注入 AI 服务工厂
     * 作用：根据 appId 和类型，获取具体的 AI 实现类（比如 DeepSeek 还是 OpenAI）。
     */
    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    /**
     * 【核心逻辑】处理流式代码（Stream）
     *
     * 作用：
     *   这是一个辅助方法，专门处理“一边生成一边返回”的场景。
     *   它利用 StringBuilder 收集所有的流片段，等流结束了（doOnComplete），再统一进行解析和保存。
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType, Long appId) {
        StringBuilder codeBuilder = new StringBuilder();
        return codeStream.doOnNext(chunk -> {
            // 实时收集：把每一个流过来的代码片段拼起来
            codeBuilder.append(chunk);
        }).doOnComplete(() -> {
            // 流结束了：开始干活
            try {
                String completeCode = codeBuilder.toString();
                // 1. 解析：把拼好的字符串变成对象
                Object parsedResult = CodeParserExecutor.executeParser(completeCode, codeGenType);
                // 2. 保存：把对象写入硬盘
                File savedDir = CodeFileSaverExecutor.executeSaver(parsedResult, codeGenType, appId);
                log.info("保存成功，目录为：" + savedDir.getAbsolutePath());
            } catch (Exception e) {
                log.error("保存失败: {}", e.getMessage());
            }
        });
    }

    /**
     * 【对外接口1】流式生成并保存（SSE场景用）
     *
     * 流程：
     *   1. 根据类型获取 AI 服务。
     *   2. 调用 AI 的流式接口获取 Flux<String>。
     *   3. 交给 processCodeStream 处理（收集+最后保存）。
     *   4. 把 Flux 返回给 Controller，实现“字一个个蹦出来”的效果。
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);

        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                // 处理流并返回
                yield processCodeStream(codeStream, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            case VUE_PROJECT -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
                // 注意：这里虽然类型是 VUE，但解析和保存目前可能复用 MULTI_FILE 的逻辑（视具体实现而定）
                yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的生成类型：" + codeGenTypeEnum.getValue());
        };
    }

    /**
     * 【对外接口2】普通生成并保存（非流式）
     *
     * 流程：
     *   1. 调用 AI 服务，直接拿到完整的代码结果对象（HtmlCodeResult 或 MultiFileCodeResult）。
     *   2. 直接调用保存执行器，把结果存盘。
     *   3. 返回保存好的文件目录。
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);

        return switch (codeGenTypeEnum) {
            case HTML -> {
                // 1. 获取结果
                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
                // 2. 保存并返回目录
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的生成类型：" + codeGenTypeEnum.getValue());
        };
    }
}