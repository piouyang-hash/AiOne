package org.myfx.controls.aione.UserService.service.impl;

import lombok.RequiredArgsConstructor;

import org.myfx.controls.aione.ServiceCommon.context.UserContext;
import org.myfx.controls.aione.UserService.mapper.UserFeedbackMapper;
import org.myfx.controls.aione.UserService.model.dto.FeedbackDTO;
import org.myfx.controls.aione.UserService.model.dto.FeedbackReplyDTO;
import org.myfx.controls.aione.UserService.model.entity.UserFeedback;
import org.myfx.controls.aione.UserService.service.UserFeedbackService;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;


/**
 * 用户意见反馈Service实现类
 */
@Service
@RequiredArgsConstructor
public class UserFeedbackServiceImpl implements UserFeedbackService {

    private final UserFeedbackMapper userFeedbackMapper;

    /**
     * 提交反馈：自动设置默认状态为「未处理」
     */
    @Override
    public int submitFeedback(FeedbackDTO feedbackDTO) {
        // 1. DTO转实体类UserFeedback
        UserFeedback userFeedback = new UserFeedback();
        userFeedback.setUserId(feedbackDTO.getUserId()); // 从DTO拿用户ID
        userFeedback.setFeedbackType(feedbackDTO.getFeedbackType()); // 反馈类型
        userFeedback.setContent(feedbackDTO.getContent()); // 反馈内容
        userFeedback.setContact(feedbackDTO.getContact()); // 联系方式
        userFeedback.setStatus(0); // 设置默认状态：未处理（也可引用常量）

        // 2. 调用Mapper插入数据库
        return userFeedbackMapper.insertFeedback(userFeedback);
    }

    /**
     * 根据ID删除反馈（管理员操作）
     */
    @Override
    public int deleteFeedbackById(Integer id) {
        // 校验ID非空
        Assert.notNull(id, "反馈ID不能为空");
        return userFeedbackMapper.deleteFeedbackById(id);
    }

    /**
     * 删除当前登录用户的所有反馈：从UserContext获取用户ID，无需传参
     */
    @Override
    public void deleteFeedbackByUserId() {
        // 1. 从UserContext获取当前登录用户ID
        Integer currentUserId = UserContext.getUserId();

        // 2. 调用Mapper删除该用户的所有反馈
        userFeedbackMapper.deleteFeedbackByUserId(currentUserId);
    }

    @Override
    public int replyFeedback(FeedbackReplyDTO replyDTO) {
        // 1. 根据反馈ID查数据库里的记录
        UserFeedback feedback = userFeedbackMapper.selectById(replyDTO.getFeedbackId());
        if (feedback == null) {
            throw new IllegalArgumentException("反馈不存在！");
        }
        // 2. 更新字段
        feedback.setReplyContent(replyDTO.getReplyContent()); // 填回复内容
        feedback.setStatus(replyDTO.getStatus()); // 改状态（1/2）
        feedback.setReplyTime(LocalDateTime.now()); // 自动生成回复时间
        // 3. 调用Mapper更新数据库
        return userFeedbackMapper.updateFeedback(feedback);
    }

}