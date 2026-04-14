package com.maiko.maikoaicodemother.core.parser;

import com.maiko.maikoaicodemother.ai.model.MultiFileCodeResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 【类定义】多文件代码解析器
 *
 * 作用：专门处理包含 HTML、CSS、JS 三种语言的复杂代码块。
 * 场景：AI 返回了一大段包含多个代码块的文本，我们需要把它们拆解成三个独立的文件内容。
 */
public class MultiFileCodeParser implements CodeParser<MultiFileCodeResult> {

    // 【定义三种正则模式】
    // 1. 匹配 HTML
    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    // 2. 匹配 CSS
    private static final Pattern CSS_CODE_PATTERN = Pattern.compile("```css\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    // 3. 匹配 JS (支持 ```js 和 ```javascript 两种写法)
    private static final Pattern JS_CODE_PATTERN = Pattern.compile("```(?:js|javascript)\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    /**
     * 【实现接口方法】解析代码
     *
     * 逻辑流程：
     * 1. 创建一个空的 MultiFileCodeResult 对象。
     * 2. 分别用三个正则去匹配文本，提取出 html、css、js 代码。
     * 3. 判空处理：如果提取到了内容（非空），就存入 result 对象。
     * 4. 返回装好数据的 result。
     */
    @Override
    public MultiFileCodeResult parseCode(String codeContent) {
        MultiFileCodeResult result = new MultiFileCodeResult();

        // 提取各类代码
        String htmlCode = extractCodeByPattern(codeContent, HTML_CODE_PATTERN);
        String cssCode = extractCodeByPattern(codeContent, CSS_CODE_PATTERN);
        String jsCode = extractCodeByPattern(codeContent, JS_CODE_PATTERN);

        // 设置HTML代码
        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            result.setHtmlCode(htmlCode.trim());
        }
        // 设置CSS代码
        if (cssCode != null && !cssCode.trim().isEmpty()) {
            result.setCssCode(cssCode.trim());
        }
        // 设置JS代码
        if (jsCode != null && !jsCode.trim().isEmpty()) {
            result.setJsCode(jsCode.trim());
        }
        return result;
    }

    /**
     * 【私有工具方法】通用提取逻辑
     *
     * 作用：避免代码重复。不管是提取 HTML 还是 CSS，逻辑都是“用正则匹配 -> 找第一组”。
     *
     * @param content 原始内容
     * @param pattern 传入不同的正则模式
     * @return 提取到的代码内容，如果没找到返回 null
     */
    private String extractCodeByPattern(String content, Pattern pattern) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1); // 返回捕获组中的内容
        }
        return null;
    }
}