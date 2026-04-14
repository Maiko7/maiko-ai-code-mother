package com.maiko.maikoaicodemother.core.parser;

import com.maiko.maikoaicodemother.ai.model.HtmlCodeResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 【类定义】HTML 单文件代码解析器
 *
 * 作用：专门用来从 AI 返回的文本中提取 HTML 代码。
 * 场景：AI 通常会返回类似 "```html ... ```" 这样的 Markdown 格式文本。
 *      这个类的任务就是把这些标记去掉，只留下纯净的 HTML 代码。
 */
public class HtmlCodeParser implements CodeParser<HtmlCodeResult> {

    /**
     * 【核心正则】匹配 HTML 代码块
     *
     * 解释：
     *   ```html        -> 匹配开始的标记（不区分大小写）
     *   \\s*\\n        -> 匹配标记后的换行符
     *   ([\\s\\S]*?)   -> 【捕获组1】非贪婪匹配任意字符（包括换行），这就是我们要的代码内容
     *   ```            -> 匹配结束的标记
     */
    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    /**
     * 【实现接口方法】解析代码
     *
     * 逻辑流程：
     * 1. 创建一个空的 HtmlCodeResult 对象，用来装结果。
     * 2. 调用 extractHtmlCode 方法尝试提取代码块。
     * 3. 如果提取到了（不为空），就存进去。
     * 4. 如果没提取到（比如 AI 没按格式返回），就把整个文本都当作 HTML 代码存进去（兜底策略）。
     */
    @Override
    public HtmlCodeResult parseCode(String codeContent) {
        HtmlCodeResult result = new HtmlCodeResult();
        // 提取 HTML 代码
        String htmlCode = extractHtmlCode(codeContent);
        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            result.setHtmlCode(htmlCode.trim());
        } else {
            // 如果没有找到代码块，将整个内容作为HTML（防止AI不按套路出牌）
            result.setHtmlCode(codeContent.trim());
        }
        return result;
    }

    /**
     * 【私有工具方法】执行正则提取
     *
     * @param content 原始内容（AI返回的整段文本）
     * @return 提取出的纯净代码，如果没找到则返回 null
     */
    private String extractHtmlCode(String content) {
        Matcher matcher = HTML_CODE_PATTERN.matcher(content);
        if (matcher.find()) {
            // group(1) 对应正则中括号 ([\\s\\S]*?) 里的内容
            return matcher.group(1);
        }
        return null;
    }
}