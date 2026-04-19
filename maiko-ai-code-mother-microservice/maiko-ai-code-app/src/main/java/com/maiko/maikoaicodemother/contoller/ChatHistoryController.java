package com.maiko.maikoaicodemother.contoller;

import com.maiko.maikoaicodemother.annotation.AuthCheck;
import com.maiko.maikoaicodemother.common.BaseResponse;
import com.maiko.maikoaicodemother.common.ResultUtils;
import com.maiko.maikoaicodemother.constant.UserConstant;
import com.maiko.maikoaicodemother.exception.ErrorCode;
import com.maiko.maikoaicodemother.exception.ThrowUtils;
import com.maiko.maikoaicodemother.innerservice.InnerUserService;
import com.maiko.maikoaicodemother.model.dto.chathistory.ChatHistoryExportRequest;
import com.maiko.maikoaicodemother.model.dto.chathistory.ChatHistoryQueryRequest;
import com.maiko.maikoaicodemother.model.entity.ChatHistory;
import com.maiko.maikoaicodemother.model.entity.User;
import com.maiko.maikoaicodemother.service.ChatHistoryService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 对话历史控制层
 * @author: Maiko7
 * @create: 2026-04-12 21:26
 */
@RestController
@RequestMapping("/chatHistory")
@Tag(name = "对话历史管理", description = "对话历史的查询、删除等接口")
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    /**
     * 分页查询某个应用的对话历史（游标查询）
     *
     * @param appId          应用ID
     * @param pageSize       页面大小
     * @param lastCreateTime 最后一条记录的创建时间
     * @param request        请求
     * @return 对话历史分页
     */
    @GetMapping("/app/{appId}")
    @Operation(summary = "分页查询应用对话历史", description = "使用游标分页方式查询指定应用的对话历史记录，支持通过lastCreateTime参数实现增量查询")
    public BaseResponse<Page<ChatHistory>> listAppChatHistory(@PathVariable Long appId,
                                                              @RequestParam(defaultValue = "10") int pageSize,
                                                              @RequestParam(required = false) LocalDateTime lastCreateTime,
                                                              HttpServletRequest request) {
        User loginUser = InnerUserService.getLoginUser(request);
        Page<ChatHistory> result = chatHistoryService.listAppChatHistoryByPage(appId, pageSize, lastCreateTime, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 管理员分页查询所有对话历史
     *
     * @param chatHistoryQueryRequest 查询请求
     * @return 对话历史分页
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "管理员分页查询对话历史", description = "管理员权限接口，支持多条件组合查询和分页获取所有对话历史记录")
    public BaseResponse<Page<ChatHistory>> listAllChatHistoryByPageForAdmin(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest) {
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = chatHistoryQueryRequest.getPageNum();
        long pageSize = chatHistoryQueryRequest.getPageSize();
        // 查询数据
        QueryWrapper queryWrapper = chatHistoryService.getQueryWrapper(chatHistoryQueryRequest);
        Page<ChatHistory> result = chatHistoryService.page(Page.of(pageNum, pageSize), queryWrapper);
        return ResultUtils.success(result);
    }

    /**
     * 导出对话历史为Markdown文件
     *
     * @param exportRequest 导出请求（包含appId、时间范围）
     * @param request HTTP请求
     * @param response HTTP响应
     */
    @PostMapping("/export")
    @Operation(summary = "导出对话历史", description = "将指定应用的对话历史导出为Markdown文件，支持按时间范围筛选")
    public void exportChatHistory(@RequestBody ChatHistoryExportRequest exportRequest,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {
        // 1. 参数校验
        ThrowUtils.throwIf(exportRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(exportRequest.getAppId() == null || exportRequest.getAppId() <= 0, 
                ErrorCode.PARAMS_ERROR, "应用ID不能为空");

        // 2. 获取登录用户
        User loginUser = InnerUserService.getLoginUser(request);

        // 3. 调用Service生成Markdown内容
        String markdownContent = chatHistoryService.exportChatHistoryToMarkdown(
                exportRequest.getAppId(),
                exportRequest.getStartTime(),
                exportRequest.getEndTime(),
                loginUser
        );

        // 4. 设置响应头，让浏览器下载文件
        try {
            // 生成文件名：应用名_时间戳.md
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "chat_history_" + exportRequest.getAppId() + "_" + timestamp + ".md";
            
            // URL编码文件名，支持中文
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            // 设置响应头
            response.setContentType("text/markdown;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", 
                    "attachment; filename*=UTF-8''" + encodedFileName);
            
            // 写入响应内容
            response.getWriter().write(markdownContent);
            response.getWriter().flush();
        } catch (IOException e) {
            throw new RuntimeException("文件导出失败: " + e.getMessage(), e);
        }
    }


}
