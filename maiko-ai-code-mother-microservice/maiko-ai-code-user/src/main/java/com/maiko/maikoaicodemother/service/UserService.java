package com.maiko.maikoaicodemother.service;

import com.maiko.maikoaicodemother.model.dto.user.UserQueryRequest;
import com.maiko.maikoaicodemother.model.entity.User;
import com.maiko.maikoaicodemother.model.vo.LoginUserVO;
import com.maiko.maikoaicodemother.model.vo.UserVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 用户服务层
 *
 * @author 代码卡壳Maiko7
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取脱敏后的用户信息
     *
     * @param user 用户信息
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏后的用户信息（分页）
     *
     * @param userList 用户列表
     * @return
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 用户注销
     *
     * @param request
     * @return 退出登录是否成功
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 根据查询条件构造数据查询参数
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 加密
     *
     * @param userPassword 用户密码
     * @return 加密后的用户密码
     */
    String getEncryptPassword(String userPassword);

    /**
     * 微信登录
     *
     * @param code 微信授权code
     * @param request HTTP请求对象
     * @return 脱敏后的用户信息
     */
    LoginUserVO wxLogin(String code, HttpServletRequest request);

    /**
     * 发送手机验证码
     *
     * @param phone 手机号
     * @return 是否发送成功
     */
    boolean sendVerificationCode(String phone);

    /**
     * 手机号验证码登录
     *
     * @param phone 手机号
     * @param code 验证码
     * @param request HTTP请求对象
     * @return 脱敏后的用户信息
     */
    LoginUserVO phoneLogin(String phone, String code, HttpServletRequest request);
}
