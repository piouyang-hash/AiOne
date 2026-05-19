package org.myfx.controls.aione.UserService.service;

import org.myfx.controls.aione.UserService.model.dto.FeedbackDTO;
import org.myfx.controls.aione.UserService.model.dto.FeedbackReplyDTO;

/**
 * 用户意见反馈Service接口
 */
public interface UserFeedbackService {

    /**
     * 提交用户意见反馈（自动设置默认状态：未处理）
     * @param feedbackDTO 反馈实体（需包含userId/feedbackType/content，status无需传）
     */
    int submitFeedback(FeedbackDTO feedbackDTO); // 入参改为DTO

    /**
     * 根据反馈ID删除反馈（仅管理员可用）
     * @param id 反馈主键ID
     * @return 受影响行数
     */
    int deleteFeedbackById(Integer id);

    /**
     * 删除当前登录用户的所有反馈（无参数，从UserContext获取当前用户ID）
     */
    void deleteFeedbackByUserId();


    int replyFeedback(FeedbackReplyDTO replyDTO);

}