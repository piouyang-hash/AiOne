package org.myfx.controls.aione.UserService.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户意见反馈实体类（对应user_feedback表）
 */
@Data // 省略getter/setter，也可手动写
public class UserFeedback {
    /** 反馈主键ID */
    private Integer id;

    /** 关联用户ID（外键） */
    private Integer userId;

    /** 反馈类型（function/experience/bug/suggestion/other） */
    private String feedbackType;

    /** 反馈内容 */
    private String content;

    /** 联系方式（手机号/邮箱） */
    private String contact;

    /** 处理状态（0-未处理 1-已回复 2-已关闭） */
    private Integer status;

    /** 管理员回复内容 */
    private String replyContent;

    /** 管理员回复时间 */
    private LocalDateTime replyTime;

    /** 反馈提交时间（数据库自动生成） */
    private LocalDateTime createTime;

    /** 最后更新时间（数据库自动生成） */
    private LocalDateTime updateTime;
}