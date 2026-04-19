package com.maiko.maikoaicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.maiko.maikoaicodemother.constant.UserConstant;
import com.maiko.maikoaicodemother.exception.BusinessException;
import com.maiko.maikoaicodemother.exception.ErrorCode;
import com.maiko.maikoaicodemother.mapper.UserMapper;
import com.maiko.maikoaicodemother.model.dto.user.UserQueryRequest;
import com.maiko.maikoaicodemother.model.entity.User;
import com.maiko.maikoaicodemother.model.enums.UserRoleEnum;
import com.maiko.maikoaicodemother.model.vo.LoginUserVO;
import com.maiko.maikoaicodemother.model.vo.UserVO;
import com.maiko.maikoaicodemother.service.UserService;
import com.maiko.maikoaicodemother.utils.RegexUtils;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.maiko.maikoaicodemother.constant.UserConstant.SALT;
import static com.maiko.maikoaicodemother.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户 服务层实现。
 *
 * @author 代码卡壳Maiko7
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService{

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private WxMpService wxMpService;

//    private static final String SALT = "maiko";//    private static final String SALT = "maiko";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1.校验参数
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度过短");
        }
        if (userPassword.length() < 6 || checkPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        // 2.查询用户是否存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(User::getUserAccount, userAccount);
        long count = this.mapper.selectCountByQuery(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已存在");
        }
        // 3.加密密码
        String encryptPassword = getEncryptPassword(userPassword);
        // 4.创建用户，插入数据库
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }
        return user.getId();
    }

    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验参数
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度过短");
        }
        if (userPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度过短");
        }
        // 2. 加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 3. 查询用户是否存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.mapper.selectOneByQuery(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 4. 如果用户存在，记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        // 5. 返回脱敏的用户信息
        return this.getLoginUserVO(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断用户是否登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询当前用户信息
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream()
                .map(this::getUserVO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 先判断用户是否登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id) // where id = ${id}
                .eq("userRole", userRole) // and userRole = ${userRole}
                .like("userAccount", userAccount)
                .like("userName", userName)
                .like("userProfile", userProfile)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }


    public String getEncryptPassword(String userPassword) {
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes(StandardCharsets.UTF_8));
        return encryptPassword;
    }

    @Override
    public LoginUserVO wxLogin(String code, HttpServletRequest request)  {
        // 1. 参数校验
        if (StrUtil.isBlank(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "微信授权code不能为空");
        }

        try {
            // 2. 通过code获取access_token和openid
            // 注意：这里需要使用完整的OAuth2流程，包括redirect_uri
            // 实际使用时，前端应该先重定向到微信授权页面获取code
            WxOAuth2AccessToken accessToken = wxMpService.getOAuth2Service().getAccessToken(code);
            String openId = accessToken.getOpenId();

            // 3. 获取用户信息（需要snsapi_userinfo scope）
            WxOAuth2UserInfo userInfo = wxMpService.getOAuth2Service().getUserInfo(accessToken, openId);
            String openid = userInfo.getOpenid();
            String unionId = userInfo.getUnionId();
            String nickname = userInfo.getNickname();
            String headImgUrl = userInfo.getHeadImgUrl();

            // 4. 根据unionId或openId查询用户是否存在
            QueryWrapper queryWrapper = new QueryWrapper();
            if (StrUtil.isNotBlank(unionId)) {
                queryWrapper.eq(User::getUnionId, unionId);
            } else {
                queryWrapper.eq(User::getOpenId, openid);
            }
            User user = this.mapper.selectOneByQuery(queryWrapper);

            // 5. 如果用户不存在，创建新用户
            if (user == null) {
                user = new User();
                user.setUnionId(unionId);
                user.setOpenId(openId);
                // 使用微信昵称+随机数作为账号
                String account = "wx_" + (StrUtil.isNotBlank(nickname) ? nickname : openId.substring(0, 8));
                user.setUserAccount(account);
                user.setUserName(nickname);
                user.setUserAvatar(headImgUrl);
                user.setUserRole(UserRoleEnum.USER.getValue());
                // 微信登录用户无需密码
                user.setUserPassword("");

                boolean saveResult = this.save(user);
                if (!saveResult) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "微信登录失败，数据库错误");
                }
                log.info("新用户微信注册成功，userId: {}, openId: {}", user.getId(), openId);
            } else {
                // 更新用户信息
                user.setUserName(nickname);
                user.setUserAvatar(headImgUrl);
                if (StrUtil.isNotBlank(unionId) && StrUtil.isBlank(user.getUnionId())) {
                    user.setUnionId(unionId);
                }
                this.updateById(user);
                log.info("用户微信登录成功，userId: {}", user.getId());
            }

            // 6. 记录登录态
            request.getSession().setAttribute(USER_LOGIN_STATE, user);

            // 7. 返回脱敏的用户信息
            return this.getLoginUserVO(user);

        } catch (WxErrorException e) {
            log.error("微信登录失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "微信登录失败: " + e.getError().getErrorMsg());
        }
    }

    @Override
    public boolean sendVerificationCode(String phone) {
        // 1. 验证手机号格式
        if (StrUtil.isBlank(phone) || RegexUtils.isPhoneInvalid(phone)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号格式错误");
        }

        // 2. 检查发送频率限制（60秒内只能发送一次）
        String rateLimitKey = UserConstant.VERIFY_RATE_LIMIT_KEY + phone;
        Object existingCode = redisTemplate.opsForValue().get(rateLimitKey);
        if (existingCode != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请勿频繁发送验证码，请60秒后再试");
        }

        // 3. 生成6位随机验证码
        String code = RandomUtil.randomNumbers(6);

        // 4. 存储到Redis，有效期5分钟
        String redisKey = UserConstant.VERIFY_CODE_KEY + phone;
        redisTemplate.opsForValue().set(redisKey, code, UserConstant.VERIFY_CODE_TTL, TimeUnit.MINUTES);

        // 5. 设置发送频率限制（60秒）
        redisTemplate.opsForValue().set(rateLimitKey, UserConstant.VERIFY_LIMIT_FLAG, UserConstant.VERIFY_RATE_LIMIT_TTL, TimeUnit.SECONDS);

        // 6. 打印验证码到日志（实际生产环境应调用短信服务发送）
        log.info("手机号 {} 的验证码为: {}", phone, code);
        // todo 这里以后可以真的调用短信服务发送验证码
        log.info("【测试模式】验证码已生成，请查看日志。生产环境需要集成短信服务。");

        return true;
    }

    @Override
    public LoginUserVO phoneLogin(String phone, String code, HttpServletRequest request) {
        // 1. 参数校验
        if (StrUtil.hasBlank(phone, code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号和验证码不能为空");
        }

        // 2. 验证手机号格式
        if (RegexUtils.isPhoneInvalid(phone)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号格式错误");
        }

        // 3. 从Redis获取验证码并校验
        String redisKey = UserConstant.VERIFY_CODE_KEY + phone;
        String storedCode = (String) redisTemplate.opsForValue().get(redisKey);

        if (storedCode == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码已过期，请重新获取");
        }

        if (!storedCode.equals(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }

        // 4. 验证码正确，删除已使用的验证码
        redisTemplate.delete(redisKey);

        // 5. 查询用户是否存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(User::getPhone, phone);
        User user = this.mapper.selectOneByQuery(queryWrapper);

        // 6. 如果用户不存在，自动注册
        if (user == null) {
            user = new User();
            user.setPhone(phone);
            user.setUserAccount(phone);
            user.setUserName("用户" + phone.substring(7));
            user.setUserRole(UserRoleEnum.USER.getValue());
            // 手机号登录用户无需密码
            user.setUserPassword("");

            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败，数据库错误");
            }
            log.info("新用户手机注册成功，userId: {}, phone: {}", user.getId(), phone);
        } else {
            log.info("用户手机登录成功，userId: {}", user.getId());
        }

        // 7. 记录登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);

        // 8. 返回脱敏的用户信息
        return this.getLoginUserVO(user);
    }
}
