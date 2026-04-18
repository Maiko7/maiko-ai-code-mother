package com.maiko.maikoaicodemother.model.enums;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户角色枚举
 */
public enum UserRoleEnum {

    USER("普通用户", "user"),
    ADMIN("管理员", "admin"),
    VIP("会员用户", "vip"),
    // todo 后续这个用户层这里可以加EDITOR和PARTNER功能逻辑
    /**
     * 一、editor 是干嘛的？（超级通俗）
     * 你做 AI 零代码平台，平台大了以后，内容审核、用户管理、日常运营不能全靠你自己！
     * 你需要：
     * 有人帮你审核违规内容、有人帮你管理用户、有人帮你处理反馈、有人帮你运营平台
     * 这些人就是 editor（运营审核 / 客服 / 管理员员工），他们负责日常平台维护，但不能碰核心高危操作！
     *
     * 二、这个角色有什么用？
     * 负责内容审核（防止违规生成）、日常用户管理、处理客服问题、维护平台模板、查看运营数据
     * 企业级项目必备（正规系统都要区分【老板】和【员工】权限）
     * 比如：公司运营、客服人员、内容审核员、实习管理员
     * 他们都用 editor 角色。
     *
     * 三、加了这个角色你能实现什么功能？
     * 内容审核、用户管理、模板管理、查看数据统计、回复用户反馈、公告发布
     * 最重要：可以分配子账号，不让员工接触核心高危权限（删库、改权限、看密码）
     */
    EDITOR("运营审核", "editor"),
    BAN("被封号", "ban"),
    /**
     * 一、partner 是干嘛的？（超级通俗）
     * 你做 AI 零代码平台，以后想赚钱、想做大，光靠你自己推广太慢了！
     * 你需要：
     * 有人帮你发视频、有人帮你写文章、有人帮你推你的软件、有人帮你卖会员
     * 这些人就是 partner（代理商 / 合作伙伴），他们拉来的用户付费了，你自动给他们分佣金！
     * 二、这个角色有什么用？
     * 帮你赚钱（你不用亲自推广）、帮你扩大用户、裂变式增长（一个带十个）、企业级 SaaS 必备（所有正规平台都有）
     * 比如：抖音达人、博主、代理商、线下销售、你的朋友帮你推
     * 他们都用 partner 角色。
     * 三、加了这个角色你能实现什么功能？
     * 推广链接、邀请码、分销系统、佣金统计、提现功能、下级用户管理、分成比例配置
     */
    PARTNER("合作伙伴", "partner");

    private final String text;

    private final String value;

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static UserRoleEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (UserRoleEnum anEnum : UserRoleEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
