package org.myfx.controls.aione.UserService.model.dto;

import lombok.Data;

/**
 * 意见反馈DTO（前后端交互专用）
 * 作用：仅接收前端提交的核心参数，和数据库实体类解耦
 */
@Data // 用lombok自动生成getter/setter/toString，不用手动写
public class FeedbackDTO {

    /**
     * 用户ID（后端从UserContext获取，前端无需传，可加@NotNull但仅后端赋值）
     */
    private Integer userId;

    /**
     * 反馈类型（必传：function/experience/bug/suggestion/other/add_book）
     */
    private String feedbackType;

    /**
     * 反馈内容（必传）
     */
    private String content;

    /**
     * 联系方式（选填：手机号/邮箱）
     */
    private String contact;

}