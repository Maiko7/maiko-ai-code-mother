package com.maiko.maikoaicodemother.core;

import com.maiko.maikoaicodemother.ai.AiCodeGeneratorService;
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
 * 这个是没用的也就是 AiCodeGeneratorFacade优化之前的，不要忘记这个步骤
 * 就弄一个OldAiCodeGeneratorFacade
 * @author: Maiko7
 * @create: 2026-04-11 22:39
 */
@Service
@Slf4j
@Deprecated
public class OldAiCodeGeneratorFacade {
    /**
     * AI代码生成服务实例
     * <p>
     * 由Spring容器自动注入，负责与AI模型交互并生成代码
     * </p>
     */
    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

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
     * @return 保存代码文件的目录对象
     * @throws BusinessException 当codeGenTypeEnum为空或传入不支持的类型时抛出业务异常
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }
//    版本1    return switch (codeGenTypeEnum) {
//            case HTML:
//                return aiCodeGeneratorService.generateAndSaveHtmlCode(userMessage);
//            case MULTI_FILE:
//                return aiCodeGeneratorService.generateAndSaveMultiFileCode(userMessage);
//            default:
//                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的生成类型");
//        };
        // 定义返回结果变量
//        String result;

// 老式 switch 对应下面的版本展开的样子。
//        switch (codeGenTypeEnum) {
//            case HTML:
//                // 执行 HTML 生成
//                result = generateAndSaveHtmlCode(userMessage);
//                break;
//
//            case MULTI_FILE:
//                // 执行多文件生成
//                result = generateAndSaveMultiFileCode(userMessage);
//                break;
//
//            default:
//                // 不支持的类型，直接抛异常
//                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
//                throw new BusinessException(ErrorCode.PARAMS_ERROR, errorMessage);
//        }
//
//        return result;
        return switch (codeGenTypeEnum) {
//            case HTML ->  generateAndSaveHtmlCode(userMessage);
//            case MULTI_FILE -> generateAndSaveMultiFileCode(userMessage);
            case HTML -> {
                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield  CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.HTML);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.MULTI_FILE);
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
     * @return 流式响应
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType) {
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
                File savedDir = CodeFileSaverExecutor.executeSaver(parsedResult, codeGenType);
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
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }

        return switch (codeGenTypeEnum) {
            case HTML ->   {
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                /**
                 * 如果你希望直接把processCodeStream(codeStream, CodeGenTypeEnum.HTML);这个返回值
                 * 传给最外层去返回就需要用yield去返回。
                 *  return processCodeStream(codeStream, CodeGenTypeEnum.HTML);这样的话报错
                 */
//                return processCodeStream(codeStream, CodeGenTypeEnum.HTML);
                yield  processCodeStream(codeStream, CodeGenTypeEnum.HTML);
            }
            // 版本1
//            case MULTI_FILE -> generateAndSaveMultiFileCodeStream(userMessage);
            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield  processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.PARAMS_ERROR, errorMessage);
            }
        };
    }

    /**
     * 生成并保存HTML代码文件（流式）
     * <p>
     * 该方法调用AI服务以流式方式生成HTML代码，在接收完所有代码块后自动解析并保存到文件系统。
     * 使用StringBuilder累积流式返回的代码片段，待流完成后统一解析和保存。
     * </p>
     *
     * @param userMessage 用户提供的HTML页面需求描述
     * @return 流式字符串响应对象，包含AI生成的HTML代码片段
     * 这个方法不直接返回一个结果，而是返回一个“管道”。AI 生成的每一个字符片段（Chunk）会通过这个管道依次发射出来。
     */
    private Flux<String> generateAndSaveHtmlCodeStream(String userMessage) {
        Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
        /**
         * codeBuilder作用：创建一个空的容器，用来把 AI 发射出来的所有碎片（Chunks）拼接回完整的代码。
         * 为什么需要它？
         * AI 发射的是碎片："<!DOCTYPE" -> "html>" -> "<head>"
         * 我们保存文件需要的是完整的："<!DOCTYPE html><head>..."
         * codeBuilder 就是负责把这些碎片粘起来的胶水。
         */
        StringBuilder codeBuilder = new StringBuilder();
        /**
         * 第一步：return result.doOnNext(...)
         * .doOnNext(...)：这是一个监听器。它监听流里的每一个数据包（Chunk）。
         * chunk：AI 生成的一个小片段字符串。
         * 动作：每当 AI 发射出一个新片段，这段代码就执行一次，把这个片段追加到 codeBuilder 里。
         * 第二步：.doOnComplete(...)
         * .doOnComplete(...)：这是一个收尾监听器。它只在 AI 全部生成完毕，流关闭之前执行一次。
         * 动作：
         * 拼接：把 codeBuilder 里的所有碎片变成一个完整的字符串。
         * 解析：调用 CodeParser（你刚才问的那个报错的类），把字符串解析成结构化的对象（比如提取出 HTML、CSS 部分）。
         * 保存：调用 CodeFileSaver 把对象保存成磁盘上的文件。
         * 日志：打印成功或失败的信息。
         */
        return result.doOnNext(chunk -> {
            codeBuilder.append(chunk);
        }).doOnComplete(() -> {
            try {
                String completeHtmlCode = codeBuilder.toString();
                // 把字符串解析成结构化的对象
                HtmlCodeResult htmlCodeResult = CodeParser.parseHtmlCode(completeHtmlCode);
                File saveDir = CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
                log.info("保存成功，目录为：{}", saveDir.getAbsolutePath());
            } catch (Exception e) {
                log.error("保存失败：{}", e.getMessage());
            }
        });
    }

    /**
     * 生成并保存多文件代码（流式）
     * <p>
     * 该方法调用AI服务以流式方式生成HTML、CSS和JavaScript代码，在接收完所有代码块后自动解析并保存到文件系统。
     * 使用StringBuilder累积流式返回的代码片段，待流完成后统一解析为多文件结果对象并保存。
     * 生成的文件包括：index.html、style.css、script.js
     * </p>
     *
     * @param userMessage 用户提供的网页需求描述
     * @return 流式字符串响应对象，包含AI生成的多文件代码片段
     */
//    private Flux<String> generateAndSaveMultiFileCodeStream(String userMessage) {
//        // 版本2 你这里就2行代码了 直接放到generateAndSaveCodeStream方法里
////        Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
//////        版本1，就是之前没抽象出来的时候
//////        StringBuilder codeBuilder = new StringBuilder();
//////        return result.doOnNext(chunk -> {
//////            codeBuilder.append(chunk);
//////        }).doOnComplete(() -> {
//////            try {
//////                String completeMultiFileCode = codeBuilder.toString();
//////                MultiFileCodeResult multiFileCodeResult = CodeParser.parseMultiFileCode(completeMultiFileCode);
//////                File saveDir = CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);
//////                log.info("保存成功，目录为：{}", saveDir.getAbsolutePath());
//////            } catch (Exception e) {
//////                log.error("保存失败：{}", e.getMessage());
//////            }
//////        });
////        return processCodeStream(result, CodeGenTypeEnum.MULTI_FILE);
//    }




    /**
     * 生成并保存HTML代码文件
     * <p>
     * 调用AI服务生成HTML代码，然后将结果保存到文件系统中的唯一目录。
     * 生成的文件名为 index.html
     * </p>
     *
     * @param userMessage 用户提供的HTML页面需求描述
     * @return 保存HTML文件的目录对象
     */
    private File generateAndSaveHtmlCode(String userMessage) {
        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
        return CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
    }

    /**
     * 生成并保存多文件代码
     * <p>
     * 调用AI服务生成HTML、CSS和JavaScript代码，然后将三个文件保存到文件系统中的唯一目录。
     * 生成的文件包括：index.html、style.css、script.js
     * </p>
     *
     * @param userMessage 用户提供的网页需求描述
     * @return 保存代码文件的目录对象
     */
    private File generateAndSaveMultiFileCode(String userMessage) {
        MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
        return CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);
    }
}
