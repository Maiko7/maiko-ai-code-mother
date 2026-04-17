package com.maiko.maikoaicodemother.langgraph4j.model;

import com.maiko.maikoaicodemother.langgraph4j.model.enums.ImageCategoryEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 图片资源对象
 * <p>
 * 用于封装图片的相关信息，包括图片类别、描述和访问地址。
 * 该类实现了 {@link Serializable} 接口，支持序列化操作，适用于网络传输或持久化存储。
 * </p>
 *
 * @author Maikoi7
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageResource implements Serializable {

    /**
     * 图片类别
     * <p>
     * 用于标识图片的分类，例如：头像、背景、图标等。
     * 使用枚举类型 {@link ImageCategoryEnum} 进行约束，确保类别值的规范性。
     * </p>
     */
    private ImageCategoryEnum category;

    /**
     * 图片描述
     * <p>
     * 对图片内容的简要文字说明，可用于展示或搜索。
     * 例如：“一只可爱的猫咪在草地上玩耍”。
     * </p>
     */
    private String description;

    /**
     * 图片地址
     * <p>
     * 图片的可访问 URL 地址，用于前端加载或下载图片资源。
     * 通常为公网可访问的 HTTP/HTTPS 链接。
     * </p>
     */
    private String url;

    /**
     * 序列化版本号
     * <p>
     * 用于在反序列化时验证发送者和接收者之间类的版本一致性。
     * 如果版本号不匹配，将抛出 {@link java.io.InvalidClassException}。
     * </p>
     */
    @Serial
    private static final long serialVersionUID = 1L;
}