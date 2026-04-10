package com.maiko.maikoaicodemother.controlller;

import cn.hutool.core.bean.BeanUtil;
import com.maiko.maikoaicodemother.annotation.AuthCheck;
import com.maiko.maikoaicodemother.common.BaseResponse;
import com.maiko.maikoaicodemother.common.DeleteRequest;
import com.maiko.maikoaicodemother.common.ResultUtils;
import com.maiko.maikoaicodemother.constant.UserConstant;
import com.maiko.maikoaicodemother.exception.BusinessException;
import com.maiko.maikoaicodemother.exception.ErrorCode;
import com.maiko.maikoaicodemother.exception.ThrowUtils;
import com.maiko.maikoaicodemother.model.dto.*;
import com.maiko.maikoaicodemother.model.entity.User;
import com.maiko.maikoaicodemother.model.vo.LoginUserVO;
import com.maiko.maikoaicodemother.model.vo.UserVO;
import com.maiko.maikoaicodemother.service.UserService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制层
 *
 * @author 代码卡壳Maiko7
 */
@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户注册、登录、注销等接口")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求
     * @return 注册结果
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "使用账号密码进行注册")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录请求
     * @param request          请求对象
     * @return 脱敏后的用户登录信息
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用账号密码进行登录")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    @GetMapping("/get/login")
    @Operation(summary = "获取当前登录用户", description = "获取当前已登录用户的脱敏信息")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }

    /**
     * 用户注销
     *
     * @param request 请求对象
     * @return
     */
    @PostMapping("/logout")
    @Operation(summary = "用户注销", description = "退出登录，清除登录态")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 创建用户
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "创建用户", description = "管理员创建新用户，默认密码123456")
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtil.copyProperties(userAddRequest, user);
        // 默认密码 123456
        final String DEFAULT_PASSWORD = "123456";
        String encryptPassword = userService.getEncryptPassword(DEFAULT_PASSWORD);
        user.setUserPassword(encryptPassword);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 根据 id 获取用户（仅管理员）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "根据ID获取用户", description = "管理员根据ID获取用户完整信息")
    public BaseResponse<User> getUserById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     */
    @GetMapping("/get/vo")
    @Operation(summary = "根据ID获取用户VO", description = "根据ID获取脱敏后的用户信息")
    public BaseResponse<UserVO> getUserVOById(long id) {
        BaseResponse<User> response = getUserById(id);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 删除用户
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "删除用户", description = "管理员根据ID删除用户")
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "更新用户", description = "管理员更新用户信息")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 分页获取用户封装列表（仅管理员）
     *
     * @param userQueryRequest 查询请求参数
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "分页获取用户列表", description = "管理员分页查询脱敏后的用户列表")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = userQueryRequest.getPageNum();
        long pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(Page.of(pageNum, pageSize),
                userService.getQueryWrapper(userQueryRequest));
        // 数据脱敏
        Page<UserVO> userVOPage = new Page<>(pageNum, pageSize, userPage.getTotalRow());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }

    /**
     * 微信登录
     *
     * @param wxLoginRequest 微信登录请求
     * @param request HTTP请求对象
     * @return 脱敏后的用户信息
     */
    @PostMapping("/login/wx")
    @Operation(summary = "微信登录", description = "使用微信公众号OAuth2.0授权码进行登录")
    public BaseResponse<LoginUserVO> wxLogin(@RequestBody WxLoginRequest wxLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(wxLoginRequest == null, ErrorCode.PARAMS_ERROR);
        LoginUserVO loginUserVO = userService.wxLogin(wxLoginRequest.getCode(), request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 发送手机验证码
     *
     * @param sendCodeRequest 发送验证码请求
     * @return 是否发送成功
     */
    @PostMapping("/send/code")
    @Operation(summary = "发送验证码", description = "向指定手机号发送验证码（测试模式仅在日志中显示）")
    public BaseResponse<Boolean> sendCode(@RequestBody SendCodeRequest sendCodeRequest) {
        ThrowUtils.throwIf(sendCodeRequest == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.sendVerificationCode(sendCodeRequest.getPhone());
        return ResultUtils.success(result);
    }

    /**
     * 手机号验证码登录
     *
     * @param phoneLoginRequest 手机号登录请求
     * @param request HTTP请求对象
     * @return 脱敏后的用户信息
     */
    @PostMapping("/login/phone")
    @Operation(summary = "手机号验证码登录", description = "使用手机号和验证码进行登录，不存在则自动注册")
    public BaseResponse<LoginUserVO> phoneLogin(@RequestBody PhoneLoginRequest phoneLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(phoneLoginRequest == null, ErrorCode.PARAMS_ERROR);
        LoginUserVO loginUserVO = userService.phoneLogin(
            phoneLoginRequest.getPhone(),
            phoneLoginRequest.getVerificationCode(),
            request
        );
        return ResultUtils.success(loginUserVO);
    }

}
