package org.myfx.controls.aione.AiService.service.base.ai_chat_db.token.upper.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.common.ai_chat_db.TokenChangeTypeEnum;
import org.myfx.controls.aione.AiService.common.ai_chat_db.TokenChangeWayEnum;
import org.myfx.controls.aione.AiService.common.exception.TokenInsufficientException;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.token.*;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.token.*;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.token.upper.AiTokenTransactionService;
import org.myfx.controls.aione.ServiceCommon.utils.SnowflakeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.List;

/**
 * AI Token 事务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiTokenTransactionServiceImpl implements AiTokenTransactionService {

    // 注入两个原子服务
    private final AiUserTokenBalanceService tokenBalanceService;
    private final AiUserTokenRecordService tokenRecordService;

    // 注入用户Token余额服务
    private final AiUserTokenBalanceService aiUserTokenBalanceService;

    private final AiUserPointRecordService aiUserPointRecordService;

    // 注入Token类型服务
    private final AiTokenTypeService aiTokenTypeService;

    private final AiUserPointBalanceService aiUserPointBalanceService;

    // 最大Token输出限制（固定2000）
    private static final Long MAX_TOKEN_OUTPUT = 2000L;

    // ====================== 1. 用户主动聊天消耗 ======================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean consumeTokenByUserChat(Integer userId, Long typeId, Long consumeAmount) {
        checkUserPointBalance(userId, typeId, consumeAmount);

        // 调用公共方法，返回执行结果
        return this.consumeTokenInternal(
                userId,
                typeId,
                consumeAmount,
                TokenChangeWayEnum.USER_CHAT,
                "用户主动聊天消耗Token"
        );
    }

    // ====================== 2. AI回复输出消耗 ======================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean consumeTokenByAiReply(Integer userId, Long typeId, Long consumeAmount) {
        // 调用公共方法，返回执行结果
        return this.consumeTokenInternal(
                userId,
                typeId,
                consumeAmount,
                TokenChangeWayEnum.AI_REPLY,
                "AI回复输出消耗Token"
        );
    }

    @Override
    public void initUserTokenBalance(Integer userId) {
        // 1. 参数合法性校验
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID不能为空且必须大于0");
        }

        // 2. 获取【默认AI Token类型列表】
        List<AiTokenType> defaultTokenTypeList = aiTokenTypeService.getDefaultAiTokenTypeList();
        if (defaultTokenTypeList == null || defaultTokenTypeList.isEmpty()) {
            throw new RuntimeException("未查询到默认AI Token类型，无法初始化用户余额");
        }

        // 3. 循环遍历默认类型，逐个插入用户Token余额记录
        for (AiTokenType tokenType : defaultTokenTypeList) {
            // 获取类型ID
            Long typeId = tokenType.getTypeId();

            // 4. 组装实体对象（适配新表结构：新增剩余可用余额字段）
            AiUserTokenBalance tokenBalance = new AiUserTokenBalance();
            tokenBalance.setUserId(userId);
            tokenBalance.setTypeId(typeId);
            tokenBalance.setTotalConsumed(0L); // 初始总消耗量=0

            // 5. 调用插入方法
            aiUserTokenBalanceService.addAiUserTokenBalance(tokenBalance);
        }
    }

    // ====================== 🔥 公共方法：返回布尔值 ======================
    /**
     * Token 消耗公共方法（仅累计消耗统计 + 新增积分扣减）
     * @return true=消耗记录成功
     */
    private boolean consumeTokenInternal(Integer userId,
                                         Long typeId,
                                         Long consumeAmount,
                                         TokenChangeWayEnum changeWay,
                                         String remark) {
        // ========== 1. 基础参数校验 ==========
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(typeId, "Token类型ID不能为空");
        Assert.notNull(consumeAmount, "消耗数量不能为空");
        Assert.isTrue(consumeAmount > 0, "消耗数量必须为正数");
        Assert.notNull(changeWay, "消耗类型不能为空");

        // ========== 2. 查询Token余额 ==========
        AiUserTokenBalance balance = tokenBalanceService.getAiUserTokenBalance(userId, typeId);
        Assert.notNull(balance, "用户Token记录不存在，请先初始化");
        Long totalConsumed = balance.getTotalConsumed();

        // ===================== 积分扣减 + 流水记录核心逻辑 =====================
        // 1. 计算需要扣减的积分
        Long deductPoint = calculateDeductPoint(typeId, consumeAmount);
        // 2. 查询【扣减前】积分余额
        Long beforePoint = aiUserPointBalanceService.getAiUserPointBalance(userId).getTotalPoint();
        // 3. 执行积分扣减
        calculateAndDeductPoint(userId, typeId, consumeAmount);
        // 4. 查询【扣减后】积分余额
        Long afterPoint = aiUserPointBalanceService.getAiUserPointBalance(userId).getTotalPoint();

        // ===================== 构建并保存 积分变动流水 =====================
        AiUserPointRecord pointRecord = new AiUserPointRecord();
        // 雪花ID
        pointRecord.setRecordId(SnowflakeGenerator.generateId());
        // 用户ID
        pointRecord.setUserId(userId);
        // 变动类型：1-消耗扣减（实体注释定义）
        pointRecord.setChangeType(1);
        // 扣减的积分数量（正数）
        pointRecord.setChangeAmount(deductPoint);
        // 变动方式：枚举转字符串
        pointRecord.setChangeWay(changeWay.name());
        // 变动前积分
        pointRecord.setBeforePoint(beforePoint);
        // 变动后积分
        pointRecord.setAfterPoint(afterPoint);
        // 备注
        pointRecord.setRemark(remark);
        // 保存积分流水
        aiUserPointRecordService.addAiUserPointRecord(pointRecord);

        // ========== 3. 更新Token消耗统计 ==========
        tokenBalanceService.incrConsume(userId, typeId, consumeAmount);

        // ========== 4. 记录Token流水（保留原有逻辑） ==========
        AiUserTokenRecord tokenRecord = new AiUserTokenRecord();
        tokenRecord.setRecordId(SnowflakeGenerator.generateId());
        tokenRecord.setUserId(userId);
        tokenRecord.setTypeId(typeId);
        tokenRecord.setChangeType(TokenChangeTypeEnum.CONSUME);
        tokenRecord.setChangeAmount(consumeAmount);
        tokenRecord.setChangeWay(changeWay);
        tokenRecord.setBeforeConsumed(totalConsumed);
        tokenRecord.setAfterConsumed(totalConsumed + consumeAmount);
        tokenRecord.setRemark(remark);
        tokenRecordService.addAiUserTokenRecord(tokenRecord);

        return true;
    }

    /**
     * 【纯计算】仅计算需要扣减的积分数量
     */
    private Long calculateDeductPoint(Long typeId, Long consumeAmount) {
        AiTokenType aiTokenType = aiTokenTypeService.getAiTokenTypeById(typeId);
        Assert.notNull(aiTokenType, "Token类型配置不存在");
        BigDecimal pricePerMillion = aiTokenType.getPricePerMillion();
        Assert.notNull(pricePerMillion, "Token单价不能为空");
        return pricePerMillion.multiply(new BigDecimal(consumeAmount)).longValue();
    }

    /**
     * 【计算并扣减】调用计算方法 + 执行积分扣减
     */
    private void calculateAndDeductPoint(Integer userId, Long typeId, Long consumeAmount) {
        Long deductPoint = calculateDeductPoint(typeId, consumeAmount);
        aiUserPointBalanceService.deductTotalPointByUserId(userId, deductPoint);
    }

    /**
     * 【预校验 - 仅对外接口使用】查询积分 + 计算扣减量 + 校验是否足够
     */
    private void checkUserPointBalance(Integer userId, Long typeId, Long consumeAmount) {
        // 1. 查询用户可用积分
        AiUserPointBalance balance = aiUserPointBalanceService.getAiUserPointBalance(userId);
        Assert.notNull(balance, "用户积分账户不存在");
        Long currentPoint = balance.getTotalPoint();

        // 2. 计算总消耗Token：实际消耗 + 最大输出Token限制
        Long totalConsumeToken = consumeAmount + MAX_TOKEN_OUTPUT;

        // 3. 根据【总消耗Token】计算需要扣减的积分
        Long needDeductPoint = calculateDeductPoint(typeId, totalConsumeToken);

        // 4. 积分不足，抛出自定义异常
        if (currentPoint < needDeductPoint) {
            throw new TokenInsufficientException("积分余额不足！当前可用积分：" + currentPoint + "，本次预估需消耗积分：" + needDeductPoint);
        }
    }
}