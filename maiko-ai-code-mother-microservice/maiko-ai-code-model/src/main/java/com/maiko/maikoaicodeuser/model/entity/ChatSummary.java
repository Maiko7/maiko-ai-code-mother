package com.maiko.maikoaicodeuser.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话总结实体类
 *
 * @author 代码卡壳Maiko7
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("chat_summary")
public class ChatSummary implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 应用id
     */
    @Column("appId")
    private Long appId;

    /**
     * 总结内容（AI生成的会议纪要）
     */
    @Column("summaryContent")
    private String summaryContent;

    /**
     * 本次总结涵盖的对话轮数
     */
    @Column("summarizedRounds")
    private Integer summarizedRounds;

    /**
     * 起始对话ID
     */
    @Column("startRoundId")
    private Long startRoundId;

    /**
     * 结束对话ID
     */
    @Column("endRoundId")
    private Long endRoundId;

    /**
     * 执行总结的用户id
     */
    @Column("userId")
    private Long userId;

    /**
     * 创建时间
     */
    @Column("createTime")
    private LocalDateTime createTime;
}
