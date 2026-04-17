package com.maiko.maikoaicodemother.langgraph4j.tools;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片搜索工具（根据关键词搜索内容图片）
 *
 * 该工具用于调用 Pexels 图片搜索 API，根据用户提供的关键词获取相关图片资源，
 * 并将其转换为系统内部的 ImageResource 格式，用于后续的网站内容展示。
 *
 * @author Maiko7
 */
@Slf4j
@Component
public class ImageSearchTool {

    // Pexels 图片搜索 API 的端点地址
    private static final String PEXELS_API_URL = "https://api.pexels.com/v1/search";

    /**
     * 从配置文件中注入 Pexels API 密钥
     * 用于在请求头中进行身份验证
     */
    @Value("${pexels.api-key}")
    private String pexelsApiKey;

    /**
     * 搜索内容相关的图片
     *
     * 此方法被标记为 LangChain4j 工具，可供 AI Agent 调用。
     * 它会向 Pexels API 发送 GET 请求，解析返回的 JSON 数据，
     * 并提取图片的 URL 和描述信息。
     *
     * @param query 搜索关键词（例如："科技感背景"、"商务办公"）
     * @return 包含图片资源信息的列表
     */
    @Tool("搜索内容相关的图片，用于网站内容展示")
    public List<ImageResource> searchContentImages(@P("搜索关键词") String query) {
        List<ImageResource> imageList = new ArrayList<>();
        int searchCount = 12; // 每页请求的图片数量

        // 使用 try-with-resources 确保 HTTP 响应资源在使用后自动关闭
        try (HttpResponse response = HttpRequest.get(PEXELS_API_URL)
                // 添加认证头
                .header("Authorization", pexelsApiKey)
                // 构建查询参数
                .form("query", query)       // 搜索关键词
                .form("per_page", searchCount) // 每页数量
                .form("page", 1)            // 页码
                .execute()) { // 发送请求

            // 检查 HTTP 响应状态码是否为 200 (OK)
            if (response.isOk()) {
                // 解析响应体为 JSON 对象
                // 示例结构参考：
                // {
                //   "photos": [
                //     {
                //       "id": 7661138,
                //       "url": "...",
                //       "src": { "medium": "https://images.../medium.jpg" },
                //       "alt": "Inspiring text..."
                //     }
                //   ]
                // }
                JSONObject result = JSONUtil.parseObj(response.body());
                JSONArray photos = result.getJSONArray("photos");

                // 遍历图片数组
                for (int i = 0; i < photos.size(); i++) {
                    JSONObject photo = photos.getJSONObject(i);
                    JSONObject src = photo.getJSONObject("src");

                    // 构建 ImageResource 对象并添加到列表
                    // 使用 'medium' 尺寸的图片链接，平衡画质与加载速度
                    // alt 文本作为描述，若无则使用搜索关键词代替
                    imageList.add(ImageResource.builder()
                            .category(ImageCategoryEnum.CONTENT) // 分类标记为内容图
                            .description(photo.getStr("alt", query)) // 描述
                            .url(src.getStr("medium")) // 图片地址
                            .build());
                }
            } else {
                log.warn("Pexels API 请求未成功，状态码: {}", response.getStatus());
            }

        } catch (Exception e) {
            log.error("Pexels API 调用失败: {}", e.getMessage(), e);
        }

        return imageList;
    }
}