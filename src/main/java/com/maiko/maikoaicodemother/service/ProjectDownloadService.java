package com.maiko.maikoaicodemother.service;

import com.maiko.maikoaicodemother.exception.BusinessException;
import jakarta.servlet.http.HttpServletResponse;

public interface ProjectDownloadService {

    /**
     * 将指定项目目录打包为 ZIP 文件并通过 HTTP 响应下载
     * <p>
     * 该方法会过滤掉预设的冗余目录（如 node_modules、.git）和文件类型（如 .log、.tmp），
     * 仅保留核心源代码和资源文件，确保下载的压缩包体积精简且安全。
     * </p>
     *
     * @param projectPath       项目目录的绝对路径，必须指向一个存在的目录
     * @param downloadFileName  下载文件的名称（不含 .zip 扩展名），将作为浏览器下载时的默认文件名
     * @param response          HTTP 响应对象，用于向客户端返回 ZIP 文件流
     * @throws BusinessException 当参数校验失败（路径为空、目录不存在）或打包过程发生 IO 异常时抛出
     */
    void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response);
}
