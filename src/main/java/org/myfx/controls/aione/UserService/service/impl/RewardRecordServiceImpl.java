package org.myfx.controls.aione.UserService.service.impl;

import lombok.RequiredArgsConstructor;
import org.myfx.controls.aione.ServiceCommon.annotation.CleanupThreadLocal;
import org.myfx.controls.aione.ServiceCommon.context.UserContext;
import org.myfx.controls.aione.UserService.mapper.RewardRecordMapper;
import org.myfx.controls.aione.UserService.model.entity.RewardRecord;
import org.myfx.controls.aione.UserService.model.entity.User;
import org.myfx.controls.aione.UserService.service.RewardRecordService;
import org.myfx.controls.aione.UserService.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 打赏记录 Service 实现类
 */
@Service // 标记为服务层组件，由 Spring 管理
@RequiredArgsConstructor
@CleanupThreadLocal
public class RewardRecordServiceImpl implements RewardRecordService {

    // 注入 Mapper 接口（MyBatis 会自动生成实现类）
    private final RewardRecordMapper rewardRecordMapper;
    private final UserService userService;

    @Override
    public boolean addRewardRecord(Integer amount, String message) {
        // 1. 从上下文获取当前用户ID
        Integer userId = UserContext.getUserId();

        // 2. 根据用户ID查询用户信息
        User user = userService.getUserById(userId); // 复用已有查询用户的方法

        // 3. 构建打赏记录实体
        RewardRecord rewardRecord = new RewardRecord();
        rewardRecord.setUserId(userId); // 设置用户ID
        rewardRecord.setUserEmail(user.getEmail()); // 从用户信息获取邮箱
        rewardRecord.setRewardAmount(amount); // 设置打赏金额
        rewardRecord.setRemark(message); // 设置留言（可为null）

        // 4. 调用mapper插入记录
        return rewardRecordMapper.insertRewardRecord(rewardRecord) > 0;
    }
}