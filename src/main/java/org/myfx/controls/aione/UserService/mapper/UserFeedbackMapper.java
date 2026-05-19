package org.myfx.controls.aione.UserService.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.myfx.controls.aione.UserService.model.entity.UserFeedback;

/**
 * 用户意见反馈Mapper接口（MyBatis）
 */
@Mapper // 标识为MyBatis Mapper，SpringBoot自动扫描
public interface UserFeedbackMapper {

    /**
     * 新增用户反馈
     * @param userFeedback 反馈实体（需传userId/feedbackType/content，status默认0）
     * @return 受影响行数
     */
    int insertFeedback(UserFeedback userFeedback);

    UserFeedback selectById(Integer id);

    /**
     * 根据反馈ID删除反馈
     * @param id 反馈主键ID
     * @return 受影响行数
     */
    int deleteFeedbackById(Integer id);

    /**
     * 根据用户ID批量删除反馈（用户注销时调用）
     * @param userId 用户ID
     * @return 受影响行数
     */
    int deleteFeedbackByUserId(Integer userId);


    int updateFeedback(UserFeedback userFeedback);

}