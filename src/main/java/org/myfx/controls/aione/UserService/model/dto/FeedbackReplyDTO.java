package org.myfx.controls.aione.UserService.model.dto;

import lombok.Data;

// 管理员回复反馈的DTO（只传需要的字段）
@Data
public class FeedbackReplyDTO {

    private Integer feedbackId; // 要回复的反馈ID

    private String replyContent; // 管理员回复内容

    private Integer status; // 1-已回复 / 2-已关闭
}