package com.maiko.maikoaicodemother.langgraph4j.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.maiko.maikoaicodemother.langgraph4j.model.ImageResource;
import com.maiko.maikoaicodemother.langgraph4j.model.enums.ImageCategoryEnum;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * UnDraw 插画图片搜索工具
 * <p>
 * 该工具通过调用 UnDraw 网站的内部搜索接口，根据关键词获取高质量的 SVG 插画资源。
 * 注意：UnDraw 的接口地址包含动态生成的哈希值（如 mMWmJSt23qpgo8cLTD_pB），在实际生产环境中建议通过动态抓包获取最新值。
 * </p>
 *
 * @author Maiko7
 */
@Slf4j
@Component
public class UndrawIllustrationTool {

    /**
     * UnDraw 搜索接口地址模板
     * 格式化占位符说明：
     * 第一个 %s：用于填充搜索路径（通常与关键词相同）
     * 第二个 %s：用于填充查询参数 term
     * <p>
     * 注意：URL 中的哈希值 (zQu8qSOkNP9BWenWh3roE) 是 Next.js 应用的构建 ID，
     * 如果该值过期，接口将无法访问，届时需要重新抓包更新此常量。
     */
    private static final String UNDRAW_API_URL = "https://undraw.co/_next/data/zQu8qSOkNP9BWenWh3roE/search/%s.json?term=%s";

    /**
     * 搜索插画图片
     * <p>
     * 该方法向 UnDraw 发起 HTTP GET 请求，解析返回的 JSON 数据，
     * 提取图片的 URL 和标题，并封装为 ImageResource 对象列表。
     * </p>
     *
     * @param query 搜索关键词（英文）
     * @return 包含插画资源信息的列表
     */
    @Tool("搜索插画图片，用于网站美化和装饰")
    public List<ImageResource> searchIllustrations(@P("搜索关键词") String query) {
        List<ImageResource> imageList = new ArrayList<>();
        int searchCount = 12; // 默认获取 12 张图片

        // 构建请求 URL，将关键词填充到路径和查询参数中
        String apiUrl = String.format(UNDRAW_API_URL, query, query);

        // 使用 try-with-resources 确保 HTTP 响应资源被正确关闭，防止连接泄露
        try (HttpResponse response = HttpRequest.get(apiUrl).timeout(10000).execute()) {
            // 检查 HTTP 状态码是否为 200 (OK)
            if (!response.isOk()) {
                log.warn("UnDraw API 请求失败，状态码: {}", response.getStatus());
                return imageList;
            }

            // 解析 JSON 响应体
            JSONObject result = JSONUtil.parseObj(response.body());
            // UnDraw 的数据通常封装在 pageProps 字段下
            JSONObject pageProps = result.getJSONObject("pageProps");
            if (pageProps == null) {
                log.warn("响应中未找到 pageProps 字段");
                return imageList;
            }

            // 获取初始搜索结果数组
            JSONArray initialResults = pageProps.getJSONArray("initialResults");
            if (initialResults == null || initialResults.isEmpty()) {
                log.info("关键词 [{}] 未搜索到相关插画", query);
                return imageList;
            }

            // 计算实际需要处理的数量（防止数组越界）
            int actualCount = Math.min(searchCount, initialResults.size());

            // 遍历搜索结果
            for (int i = 0; i < actualCount; i++) {
                JSONObject illustration = initialResults.getJSONObject(i);
                // 获取插画标题，若为空则使用默认值
                String title = illustration.getStr("title", "插画");
                // 获取图片媒体地址 (media 字段通常包含 SVG 的 URL)
                String media = illustration.getStr("media", "");

                // 确保 URL 有效后再添加到结果列表
                if (StrUtil.isNotBlank(media)) {
                    imageList.add(ImageResource.builder()
                            .category(ImageCategoryEnum.ILLUSTRATION) // 标记为插画类别
                            .description(title) // 使用插画原标题作为描述
                            .url(media) // 填充图片地址
                            .build());
                }
            }
        } catch (Exception e) {
            log.error("搜索插画失败：{}", e.getMessage(), e);
        }
        return imageList;
    }
}