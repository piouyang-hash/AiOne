package org.myfx.controls.aione.AiService.service.base.status.ai_behavior_db.upper.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.common.ai_chat_db.EmotionTypeEnum;
import org.myfx.controls.aione.AiService.dto.redis.AiActivityScoreRedisDTO;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiEmotionLogDetail;
import org.myfx.controls.aione.AiService.service.base.status.ai_behavior_db.AiEmotionLogDetailService;
import org.myfx.controls.aione.AiService.service.base.status.ai_behavior_db.upper.IAiEmotionScoreUpperQueryService;
import org.springframework.stereotype.Service;

/**
 * AI情感分值日志上层查询服务实现类
 * 适配AiEmotionLogDetailService，封装上层查询逻辑，统一返回Redis适配的DTO
 */
@Service
@Slf4j
// 构造器注入依赖（Spring最佳实践）
@RequiredArgsConstructor
public class AiEmotionScoreUpperQueryServiceImpl implements IAiEmotionScoreUpperQueryService {

    // 注入AI情感日志明细服务（底层数据查询）
    private final AiEmotionLogDetailService aiEmotionLogDetailService;

    /**
     * 查询指定用户「上一次指定类型情感变动」的完整信息（无数据返回null，无默认值）
     */
    @Override
    public AiActivityScoreRedisDTO queryLastEmotionChangeInfo(Integer userId, EmotionTypeEnum emotionType) {
        // 1. 参数非空校验：为空直接抛异常（参数空属于异常行为）
        if (userId == null || emotionType == null) {
            String errorMsg = String.format("查询上一次情感变动失败：用户ID/情感类型为空！userId=%s, emotionType=%s", userId, emotionType);
            log.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }

        // 2. 调用明细服务：查询最新指定类型的情感变动记录
        AiEmotionLogDetail latestDetail = aiEmotionLogDetailService.getLatestEmotionLog(userId, emotionType);

        // 3. 无记录打info日志（正常行为），直接返回null
        if (latestDetail == null) {
            String infoMsg = String.format("用户%d+情感类型%s无情感变动记录（正常行为）", userId, emotionType.name());
            log.info(infoMsg);
            return null;
        }

        // 4. 有记录时构建Redis适配DTO（无任何默认值，完全复用明细数据）
        AiActivityScoreRedisDTO redisDTO = new AiActivityScoreRedisDTO();
        redisDTO.setUserId(userId); // 赋值用户ID
        // 情感分值映射：使用日志明细中的分值作为AI活跃度分值
        redisDTO.setActivityScore(latestDetail.getValueAfter());
        // 时间戳映射：使用情感变动时间作为上次更新时间戳
        redisDTO.setLastUpdateTimestamp(latestDetail.getBehaviorTime());
        // 上次收到用户消息时间戳：就是调用的瞬间
        redisDTO.setLastReceiveUserMsgTimestamp(System.currentTimeMillis());

        log.info("用户{}+情感类型{}查询到最新情感变动记录，已构建RedisDTO | 分值：{} | 变动时间：{}",
                userId, emotionType.name(), redisDTO.getActivityScore(), redisDTO.getLastUpdateTimestamp());
        return redisDTO;
    }
}