package com.maiko.maikoaicodeuser.common;


import lombok.Data;

/**
 * 分页请求
 * PageRequest就是你之后需要分页请求的你直接继承它即可，就具备了这些参数。
 */
@Data
public class PageRequest {

    /**
     * 当前页号
     */
    private long pageNum = 1;

    /**
     * 页面大小
     */
    private long pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认升序）
     */
    private String sortOrder = "descend";
}
