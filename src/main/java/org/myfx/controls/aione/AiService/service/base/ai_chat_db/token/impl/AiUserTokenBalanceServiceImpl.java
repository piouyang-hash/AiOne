package org.myfx.controls.aione.AiService.service.base.ai_chat_db.token.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.token.AiUserTokenBalance;
import org.myfx.controls.aione.AiService.mapper.ai_chat_db.token.AiUserTokenBalanceMapper;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.token.AiUserTokenBalanceService;
import org.myfx.controls.aione.ServiceCommon.context.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * AI用户Token余额 业务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiUserTokenBalanceServiceImpl implements AiUserTokenBalanceService {

    // 注入Mapper
    private final AiUserTokenBalanceMapper aiUserTokenBalanceMapper;

    @Override
    public int addAiUserTokenBalance(AiUserTokenBalance aiUserTokenBalance) {
        // 参数非空校验
        Assert.notNull(aiUserTokenBalance, "Token余额实体不能为空");
        Assert.notNull(aiUserTokenBalance.getUserId(), "用户ID不能为空");
        // 调用Mapper新增
        return aiUserTokenBalanceMapper.insert(aiUserTokenBalance);
    }

    @Override
    public AiUserTokenBalance getAiUserTokenBalance(Integer userId, Long typeId) {
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(typeId, "Token类型ID不能为空");
        // 调用Mapper：userId+typeId查询单个
        return aiUserTokenBalanceMapper.selectByUserIdAndTypeId(userId, typeId);
    }

    @Override
    public List<AiUserTokenBalance> getAiUserTokenBalanceList(Integer userId) {
        Assert.notNull(userId, "用户ID不能为空");
        // 调用Mapper：查询当前用户所有类型的余额
        return aiUserTokenBalanceMapper.selectByUserId(userId);
    }

    /**
     * 无参重载实现：从上下文获取用户ID，调用原有方法
     */
    @Override
    public List<AiUserTokenBalance> getAiUserTokenBalanceList() {
        // 从上下文获取当前登录用户ID
        Integer userId = UserContext.getUserId();
        // 调用已有的带参方法，复用逻辑
        return this.getAiUserTokenBalanceList(userId);
    }

    @Override
    public int incrConsume(Integer userId, Long typeId, Long consumeAmount) {
        // 参数非空校验
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(typeId, "Token类型ID不能为空");
        Assert.notNull(consumeAmount, "消耗数量不能为空");
        Assert.isTrue(consumeAmount > 0, "消耗数量必须为正数");
        // 调用底层Mapper
        return aiUserTokenBalanceMapper.incrementTotalConsumed(userId, typeId, consumeAmount);
    }

    @Override
    public int removeAiUserTokenBalance(Integer userId) {
        // 参数非空校验
        Assert.notNull(userId, "用户ID不能为空");
        // 调用Mapper删除
        return aiUserTokenBalanceMapper.deleteByUserId(userId);
    }
}
