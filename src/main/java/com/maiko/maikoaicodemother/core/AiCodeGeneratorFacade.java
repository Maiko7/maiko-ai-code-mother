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
 * 门面模式
 * AI代码生成器外观类
 * <p>
 * 提供统一的门面接口来协调AI代码生成和文件保存流程。
 * 封装了AI服务调用和文件保存的复杂逻辑，客户端只需通过此外观类即可完成代码生成和保存操作。
 * 支持HTML单文件和多文件两种代码生成模式。
 * </p>
 *
 * @author Maiko7
 * @create 2026-04-11 15:43
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {

    /**
     * AI代码生成服务实例
     * <p>
     * 由Spring容器自动注入，负责与AI模型交互并生成代码
     * </p>
     */
    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    /**
     * 根据用户描述和代码生成类型生成并保存代码
     * <p>
     * 该方法作为外观方法，统一处理不同类型的代码生成请求：
     * <ul>
     *   <li>HTML模式：生成单个HTML文件</li>
     *   <li>多文件模式：生成HTML、CSS和JavaScript三个文件</li>
     * </ul>
     * 生成后的代码会自动保存到文件系统的唯一目录下。
     * </p>
     *
     * @param userMessage 用户提供的代码需求描述
     * @param codeGenTypeEnum 代码生成类型枚举，不能为空
     * @param appId 应用ID
     * @return 保存代码文件的目录对象
     * @throws BusinessException 当codeGenTypeEnum为空或传入不支持的类型时抛出业务异常
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }
        // 根据appId获取相应的AI服务实例
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        return switch (codeGenTypeEnum) {
            case HTML -> {
//                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(1, userMessage);
                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode( userMessage);
                yield  CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.PARAMS_ERROR, errorMessage);
            }
        };
    }

    /**
     * 通用流式代码处理方法
     *
     * @param codeStream  代码流
     * @param codeGenType 代码生成类型
     * @param appId 应用ID
     * @return 流式响应
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType, Long appId) {
        StringBuilder codeBuilder = new StringBuilder();
        return codeStream.doOnNext(chunk -> {
            // 实时收集代码片段
            codeBuilder.append(chunk);
        }).doOnComplete(() -> {
            // 流式返回完成后保存代码
            try {
                String completeCode = codeBuilder.toString();
                // 使用执行器解析代码
                Object parsedResult = CodeParserExecutor.executeParser(completeCode, codeGenType);
                // 使用执行器保存代码
                File savedDir = CodeFileSaverExecutor.executeSaver(parsedResult, codeGenType, appId);
                log.info("保存成功，路径为：" + savedDir.getAbsolutePath());
            } catch (Exception e) {
                log.error("保存失败: {}", e.getMessage());
            }
        });
    }


    /**
     * 根据用户描述和代码生成类型生成并保存代码（流式）
     * <p>
     * 该方法作为外观方法，统一处理不同类型的代码生成请求：
     * <ul>
     *   <li>HTML模式：生成单个HTML文件</li>
     *   <li>多文件模式：生成HTML、CSS和JavaScript三个文件</li>
     * </ul>
     * 生成后的代码会自动保存到文件系统的唯一目录下。
     * </p>
     *
     * @param userMessage 用户提供的代码需求描述
     * @param codeGenTypeEnum 代码生成类型枚举，不能为空
     * @return 保存代码文件的目录对象
     * @throws BusinessException 当codeGenTypeEnum为空或传入不支持的类型时抛出业务异常
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum,Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }
        // 根据appId获取相应的AI服务实例
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        return switch (codeGenTypeEnum) {
            case HTML ->   {
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                /**
                 * 如果你希望直接把processCodeStream(codeStream, CodeGenTypeEnum.HTML);这个返回值
                 * 传给最外层去返回就需要用yield去返回。
                 *  return processCodeStream(codeStream, CodeGenTypeEnum.HTML);这样的话报错
                 */
//                return processCodeStream(codeStream, CodeGenTypeEnum.HTML);
                yield  processCodeStream(codeStream, CodeGenTypeEnum.HTML, appId);
            }

            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield  processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.PARAMS_ERROR, errorMessage);
            }
        };
    }
}
