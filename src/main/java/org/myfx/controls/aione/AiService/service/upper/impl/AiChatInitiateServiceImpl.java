package org.myfx.controls.aione.AiService.service.upper.impl;

import cn.hutool.core.lang.Assert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.dto.redis.UserActivityScoreRedisDTO;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiChatSession;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiChatSessionService;
import org.myfx.controls.aione.AiService.service.base.status.user_behavior_db.upper.IUserFeatureScoreUpperQueryService;
import org.myfx.controls.aione.AiService.service.base.status.user_behavior_db.upper.UserBehaviorImpactScoreService;
import org.myfx.controls.aione.AiService.service.upper.IAiChatInitiateService;
import org.myfx.controls.aione.AiService.utils.UserActivityScoreRedisUtil;
import org.springframework.stereotype.Service;

/**
 * AI主动聊天触发实现类（用户下线场景）
 */
@Slf4j
@Service
@RequiredArgsConstructor // 自动注入依赖，替代@Autowired
public class AiChatInitiateServiceImpl implements IAiChatInitiateService {

    // 注入会话服务
    private final AiChatSessionService aiChatSessionService;

    // 注入Redis工具类
    private final UserActivityScoreRedisUtil userActivityScoreRedisUtil;

    // 新增注入：活跃度查询服务
    private final IUserFeatureScoreUpperQueryService userFeatureScoreUpperQueryService;

    // 注入行为影响分数服务
    private final UserBehaviorImpactScoreService userBehaviorImpactScoreService;

    /**
     * 处理用户下线并初始化活跃度递减（返回Integer类型userId）
     * @param userId 用户ID
     * @return 用户ID（参数不合法抛异常；无活跃会话数据返回null；正常处理返回userId）
     */
    @Override
    public Integer handleUserOfflineAndInitActivity(Integer userId) {
        // 1. 参数校验（Assert简化，直接抛IllegalArgumentException）
        Assert.notNull(userId, "处理用户下线准备活跃度递减失败：用户ID为空");

        // 2. 移除sessionId相关逻辑（无需获取活跃会话ID）
        // 3. 调用查询服务获取最后一次活跃度变动信息DTO（仅传userId）
        UserActivityScoreRedisDTO activityDTO = userFeatureScoreUpperQueryService.queryLastActivityChangeInfo(userId);

        // 4. DTO空值检测（日志移除sessionId）
        if (activityDTO == null) {
            log.info("用户{}无活跃度变动DTO数据，无需存入Redis", userId);
            return userId;
        }

        // 5. 判断活跃度分值是否为0，为0则不存储（日志移除sessionId）
        Integer activityScore = activityDTO.getActivityScore();
        if (activityScore != null && activityScore == 0) {
            log.info("用户{}活跃度为0，无需存入Redis", userId);
            return userId;
        }

        // 6. 有DTO且活跃度非0时，存入Redis，作为递减基数（日志移除sessionId）
        userActivityScoreRedisUtil.saveUserActivityScore(activityDTO);
        log.info("用户{}活跃度信息已作为递减初始值存入Redis，分值：{}", userId, activityScore);

        // 7. 返回Integer类型的userId（替代原sessionId）
        return userId;
    }

    /**
     * 处理用户上线，删除Redis中该用户的活跃度Key，并调用离线分值处理方法
     * @param userId 用户ID（必传）
     * @return 用户ID（Integer类型，参数不合法抛异常；正常处理后返回userId）
     */
    @Override
    public Integer handleUserOnlineClearActivity(Integer userId) {
        // 1. 参数校验（Assert简化，直接抛IllegalArgumentException）
        Assert.notNull(userId, "处理用户上线清除活跃度失败：用户ID为空");

        // 2. 移除sessionId相关逻辑（无需获取/校验活跃会话ID）
        // 3. 删除Redis中活跃度Key，获取衰减具体分值（仅传userId）
        Integer decayValue = userActivityScoreRedisUtil.deleteUserActivityScore(userId);

        // 4. 分支处理衰减值（日志移除sessionId）
        // 4.1 无活跃度数据（decayValue=1）
        if (decayValue == 1) {
            log.info("用户{}无用户活跃度衰减数据，无需处理离线分值", userId);
            return userId;
        }
        // 4.2 离线时间较短，衰减值为0
        if (decayValue == 0) {
            log.info("用户{}离线时间较短，活跃度衰减值为0，不计入离线分值处理", userId);
            return userId;
        }

        // 5. 衰减值非1/0时，调用离线分值处理方法（仅传userId和decayValue）
        boolean handleResult = userBehaviorImpactScoreService.handleUserOffline(userId, decayValue);
        log.info("用户{}上线清除活跃度，衰减具体分值：{} | 离线分值处理结果：{}",
                userId, decayValue, handleResult);

        // 6. 返回Integer类型的userId（替代原sessionId）
        return userId;
    }

    /**
     * 私有方法：获取用户当前活跃会话ID（抽离复用逻辑）
     * @param userId 用户ID
     * @return 活跃会话ID（无则返回null）
     */
    private Long getUserActiveSessionId(Integer userId) {
        if (userId == null) {
            log.warn("获取用户活跃会话ID失败：用户ID为空");
            return null;
        }
        AiChatSession activeSession = aiChatSessionService.getUserCurrentActiveSession(userId);
        if (activeSession == null) {
            String noSessionTip = String.format("用户%s没有活跃对话", userId);
            log.info(noSessionTip);
            return null;
        }
        return activeSession.getSessionId();
    }

}