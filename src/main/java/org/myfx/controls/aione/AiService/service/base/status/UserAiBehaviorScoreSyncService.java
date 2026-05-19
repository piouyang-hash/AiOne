package org.myfx.controls.aione.AiService.service.base.status;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.common.ai_chat_db.EmotionTypeEnum;
import org.myfx.controls.aione.AiService.dto.redis.AiActivityScoreRedisDTO;
import org.myfx.controls.aione.AiService.service.base.status.ai_behavior_db.upper.AiBehaviorImpactScoreService;
import org.myfx.controls.aione.AiService.service.base.status.ai_behavior_db.upper.impl.AiEmotionScoreUpperQueryServiceImpl;
import org.myfx.controls.aione.AiService.service.base.status.user_behavior_db.upper.UserBehaviorImpactScoreService;
import org.myfx.controls.aione.AiService.utils.AiActivityScoreRedisUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 用户 <-> AI 双向行为分值同步服务
 * 核心作用：保证【用户行为加分】和【AI行为响应】的分值操作事务一致性
 * 所有双向分值联动逻辑统一收口，原子性执行
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAiBehaviorScoreSyncService {

    // ======================== 依赖注入（构造器注入，Spring推荐最佳实践） ========================
    /**
     * 用户行为分值处理服务
     */
    private final UserBehaviorImpactScoreService userBehaviorImpactScoreService;

    /**
     * AI行为分值处理服务
     */
    private final AiBehaviorImpactScoreService aiBehaviorImpactScoreService;

    /**
     * AI活跃度Redis工具类
     */
    private final AiActivityScoreRedisUtil aiActivityScoreRedisUtil;

    /**
     * AI情感分值上层查询服务
     */
    private final AiEmotionScoreUpperQueryServiceImpl aiEmotionScoreUpperQueryService;

    // ======================== 核心事务方法 ========================

    /**
     * 【事务核心】用户发送消息 → 双向分值同步处理
     * 1. 用户聊天行为加分
     * 2. AI活跃度重置/响应
     * 3. AI接收消息行为加分
     * 4. 最新状态缓存同步
     * <p>
     * 事务保证：所有数据库/Redis操作原子性，任意异常全部回滚，确保用户&AI分值数据一致
     *
     * @param userId    用户ID
     */
    @Transactional(rollbackFor = Exception.class, timeout = 10)
    public void syncUserSendMsgScore(Integer userId) {
        // 非空校验
        Assert.notNull(userId, "用户ID不能为空");

        log.info("开始执行用户[{}]双向行为分值同步", userId);

        // 1. 用户聊天行为加分
        userBehaviorImpactScoreService.handleChatBehavior(userId);

        // 2. AI活跃度衰减处理
        Integer incrementScore = aiActivityScoreRedisUtil.deleteAiActivityScore(userId);
        incrementScore = (incrementScore == null || incrementScore == -1) ? 0 : incrementScore;

        // 3. AI等待行为分值处理
        if (incrementScore != 0) {
            aiBehaviorImpactScoreService.handleAiWaitBehavior(userId, incrementScore);
        }

        // 4. AI接收消息行为加分（双向核心）
        aiBehaviorImpactScoreService.handleAiUserSendMsgBehavior(userId);

        // 5. 同步最新活跃度到Redis
        AiActivityScoreRedisDTO latestActivity = aiEmotionScoreUpperQueryService.queryLastEmotionChangeInfo(userId, EmotionTypeEnum.ACTIVITY);
        if (latestActivity != null) {
            aiActivityScoreRedisUtil.saveAiActivityScore(latestActivity);
        }

        log.info("用户[{}]双向行为分值同步完成", userId);
    }
}