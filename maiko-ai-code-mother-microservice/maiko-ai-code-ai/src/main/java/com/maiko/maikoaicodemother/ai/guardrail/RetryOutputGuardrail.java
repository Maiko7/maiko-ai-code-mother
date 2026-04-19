package com.maiko.maikoaicodemother.ai.guardrail;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailResult;

/**
 * 基于 LangChain4j 的重试输出护轨
 * <p>
 * 该组件实现了对大模型（LLM）生成内容的二次校验机制。
 * 主要用于拦截空响应、过短内容或包含敏感信息的回复，并指示 LLM 重新生成。
 * </p>
 *
 * @author Maiko7
 */
public class RetryOutputGuardrail implements OutputGuardrail {

    /**
     * 验证大模型的响应内容
     * <p>
     * 按照预设规则依次检查：非空性 -> 长度合规性 -> 敏感词过滤。
     * 若任一检查失败，则返回重试指令；全部通过则返回成功。
     * </p>
     *
     * @param responseFromLLM 大模型生成的原始 AI 消息对象
     * @return 校验结果（成功或需要重试的指令）
     */
    @Override
    public OutputGuardrailResult validate(AiMessage responseFromLLM) {
        String response = responseFromLLM.text();

        // 1. 检查响应是否为空或仅包含空白字符
        if (response == null || response.trim().isEmpty()) {
            return reprompt("响应内容为空", "请重新生成完整的内容");
        }

        // 2. 检查响应长度是否过短（阈值设为 10）
        if (response.trim().length() < 10) {
            return reprompt("响应内容过短", "请提供更详细的内容");
        }

        // 3. 检查是否包含敏感信息或不当内容
        if (containsSensitiveContent(response)) {
            return reprompt("包含敏感信息", "请重新生成内容，避免包含敏感信息");
        }

        // 所有检查通过，接受响应
        return success();
    }

    /**
     * 检查文本中是否包含预定义的敏感关键词
     * <p>
     * 采用简单的字符串匹配算法，不区分大小写。
     * 涵盖密码、密钥、令牌等安全相关词汇。
     * </p>
     *
     * @param response 待检查的文本内容
     * @return true 表示包含敏感内容，false 表示安全
     */
    private boolean containsSensitiveContent(String response) {
        String lowerResponse = response.toLowerCase();
        // 定义敏感词库
        String[] sensitiveWords = {
                "密码", "password", "secret", "token",
                "api key", "私钥", "证书", "credential"
        };

        // 遍历检查
        for (String word : sensitiveWords) {
            if (lowerResponse.contains(word)) {
                return true;
            }
        }
        return false;
    }
}