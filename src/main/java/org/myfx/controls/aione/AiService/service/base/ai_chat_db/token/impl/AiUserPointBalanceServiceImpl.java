package org.myfx.controls.aione.AiService.service.base.ai_chat_db.token.impl;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.token.AiUserPointBalance;
import org.myfx.controls.aione.AiService.mapper.ai_chat_db.token.AiUserPointBalanceMapper;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.token.AiUserPointBalanceService;
import org.myfx.controls.aione.ServiceCommon.context.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * AI用户统一算力积分 业务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiUserPointBalanceServiceImpl implements AiUserPointBalanceService {

    @Resource
    private AiUserPointBalanceMapper aiUserPointBalanceMapper;

    @Override
    public int addAiUserPointBalance(AiUserPointBalance balance) {
        Assert.notNull(balance, "积分余额实体不能为空");
        Assert.notNull(balance.getUserId(), "用户ID不能为空");
        return aiUserPointBalanceMapper.insert(balance);
    }

    @Override
    public void initAiUserPointBalance(Integer userId) {
        // 参数非空校验
        Assert.notNull(userId, "用户ID不能为空");

        // 1. 构建积分余额实体对象
        AiUserPointBalance balance = new AiUserPointBalance();
        // 2. 设置用户ID
        balance.setUserId(userId);
        // 3. 设置默认积分：0L
        balance.setTotalPoint(0L);

        // 4. 直接复用已有的 add 方法
        addAiUserPointBalance(balance);
    }

    @Override
    public AiUserPointBalance getAiUserPointBalance(Integer userId) {
        // 完全对标你的校验格式
        Assert.notNull(userId, "用户ID不能为空");
        // 调用Mapper：根据用户ID查询唯一积分余额
        return aiUserPointBalanceMapper.selectByUserId(userId);
    }

    @Override
    public AiUserPointBalance getAiUserPointBalance() {
        // 从登录上下文获取当前用户ID
        Integer userId = UserContext.getUserId();

        // 调用已有的有参方法，复用逻辑
        return this.getAiUserPointBalance(userId);
    }

    @Override
    public int deductTotalPointByUserId(Integer userId, Long deductPoint) {
        // 1. 基础参数非空校验
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(deductPoint, "扣减的积分数量不能为空");
        // 额外校验：扣减数量不能为负数
        Assert.isTrue(deductPoint > 0, "扣减的积分数量必须大于0");

        // 2. 根据用户ID查询当前积分余额
        AiUserPointBalance userBalance = aiUserPointBalanceMapper.selectByUserId(userId);
        // 校验：用户积分记录不存在
        Assert.notNull(userBalance, "该用户积分余额记录不存在");

        // 3. 获取当前总积分
        Long currentTotalPoint = userBalance.getTotalPoint();
        // 校验：积分不足，无法扣减
        Assert.isTrue(currentTotalPoint >= deductPoint, "用户积分余额不足，扣减失败");

        // 4. 计算扣减后的最终积分
        Long newTotalPoint = currentTotalPoint - deductPoint;

        // 5. 调用原Mapper更新方法，写入新的积分
        return aiUserPointBalanceMapper.updateTotalPointByUserId(userId, newTotalPoint);
    }

    @Override
    public int addTotalPointByUserId(Integer userId, Long addPoint) {
        // 1. 基础参数非空校验
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(addPoint, "增加的积分数量不能为空");
        // 额外校验：增加数量不能为负数
        Assert.isTrue(addPoint > 0, "增加的积分数量必须大于0");

        // 2. 根据用户ID查询当前积分余额
        AiUserPointBalance userBalance = aiUserPointBalanceMapper.selectByUserId(userId);
        // 校验：用户积分记录不存在
        Assert.notNull(userBalance, "该用户积分余额记录不存在");

        // 3. 获取当前总积分
        Long currentTotalPoint = userBalance.getTotalPoint();

        // 4. 计算增加后的最终积分
        Long newTotalPoint = currentTotalPoint + addPoint;

        // 5. 调用Mapper更新方法，写入新的积分
        return aiUserPointBalanceMapper.updateTotalPointByUserId(userId, newTotalPoint);
    }

    @Override
    public int deleteAiUserPointBalance(Integer userId) {
        Assert.notNull(userId, "用户ID不能为空");
        return aiUserPointBalanceMapper.deleteByUserId(userId);
    }

}