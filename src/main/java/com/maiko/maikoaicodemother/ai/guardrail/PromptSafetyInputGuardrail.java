package com.maiko.maikoaicodemother.ai.guardrail;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 提示词安全审查护轨
 * <p>
 * 该组件实现了对用户输入（Prompt）的安全防御机制。
 * 主要用于防止提示词注入攻击、越狱尝试以及恶意指令，确保大模型在安全的上下文中运行。
 * </p>
 *
 * 存在的问题：
 * 硬编码：SENSITIVE_WORDS、长度限制1000。
 * 匹配逻辑太简单：敏感词是 hack，攻击者输入 ha ck 或者 h@ck，你的代码就检测不到了。
 * 缺少“多模态”防御：如果用户在图片里写了“忽略之前的指令”，你的代码完全看不见。
 *
 * @author Maiko7
 */
public class PromptSafetyInputGuardrail implements InputGuardrail {

    /**
     * 预定义的敏感词列表
     * <p>
     * 包含常见的试图绕过系统限制的中文和英文关键词。
     * 例如：“忽略之前的指令”、“越狱”、“破解”等。
     * </p>
     */
//    private static final List<String> SENSITIVE_WORDS = Arrays.asList(
//            "忽略之前的指令", "ignore previous instructions", "ignore above",
//            "破解", "hack", "绕过", "bypass", "越狱", "jailbreak"
//    );
    private static final List<String> SENSITIVE_WORDS = List.of(
            "忽略之前的指令", "ignore previous instructions", "ignore above",
            "破解", "hack", "绕过", "bypass", "越狱", "jailbreak"
    );

    /**
     * 提示词注入攻击的正则模式列表
     * <p>
     * 使用正则表达式匹配更复杂的攻击句式，不区分大小写。
     * 涵盖：忽略指令、忘记上下文、角色扮演伪装、伪造系统提示词等。
     * </p>
     */
    private static final List<Pattern> INJECTION_PATTERNS = Arrays.asList(
            // 匹配 "Ignore previous instructions" 等变体
            Pattern.compile("(?i)ignore\\s+(?:previous|above|all)\\s+(?:instructions?|commands?|prompts?)"),
            // 匹配 "Forget everything above" 等变体
            Pattern.compile("(?i)(?:forget|disregard)\\s+(?:everything|all)\\s+(?:above|before)"),
            // 匹配 "Pretend you are..." 等角色扮演攻击
            Pattern.compile("(?i)(?:pretend|act|behave)\\s+(?:as|like)\\s+(?:if|you\\s+are)"),
            // 匹配伪造的系统级指令
            Pattern.compile("(?i)system\\s*:\\s*you\\s+are"),
            // 匹配新的指令注入尝试
            Pattern.compile("(?i)new\\s+(?:instructions?|commands?|prompts?)\\s*:")
    );

    /**
     * 验证用户输入的提示词
     * <p>
     * 依次执行以下检查：
     * 1. 长度限制（防止超长文本攻击）。
     * 2. 非空检查。
     * 3. 敏感词匹配。
     * 4. 正则模式匹配（防注入）。
     * 若任一检查失败，立即阻断请求并返回错误信息。
     * </p>
     *
     * @param userMessage 用户发送的消息对象
     * @return 校验结果（成功或阻断指令）
     */
    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        // 从用户消息对象中提取纯文本内容，作为后续安全检查的输入源
        String input = userMessage.singleText();

        // 1. 检查是否为空
        if (input == null || input.trim().isEmpty()) {
            return fatal("输入内容不能为空");
        }

        // 2. 检查输入长度，防止资源耗尽（DoS）
        if (input.length() > 1000) {
            return fatal("输入内容过长，不要超过 1000 字");
        }


        // 3. 检查敏感词（简单字符串包含匹配）
        String lowerInput = input.toLowerCase();
        for (String sensitiveWord : SENSITIVE_WORDS) {
            if (lowerInput.contains(sensitiveWord.toLowerCase())) {
                return fatal("输入包含不当内容，请修改后重试");
            }
        }

        // 4. 检查复杂的注入攻击模式（正则匹配）
        for (Pattern pattern : INJECTION_PATTERNS) {
            if (pattern.matcher(input).find()) {
                return fatal("检测到恶意输入，请求被拒绝");
            }
        }

        // 所有安全检查通过
        return success();
    }
}