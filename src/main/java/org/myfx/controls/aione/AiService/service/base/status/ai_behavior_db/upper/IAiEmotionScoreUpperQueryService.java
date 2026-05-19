package org.myfx.controls.aione.AiService.service.base.status.ai_behavior_db.upper;

import org.myfx.controls.aione.AiService.common.ai_chat_db.EmotionTypeEnum;
import org.myfx.controls.aione.AiService.dto.redis.AiActivityScoreRedisDTO;

/**
 * AI情感分值日志上层查询服务接口
 * 核心：封装AI情感分值日志的上层查询逻辑，对外提供统一的查询入口
 */
public interface IAiEmotionScoreUpperQueryService {

    /**
     * 查询指定用户「上一次指定类型情感变动」的完整信息（无数据返回null，无默认值）
     * @param userId 用户ID（非空）
     * @param emotionType 情感类型（非空，如ACTIVITY/EMOTION等）
     * @return Redis适配的AI情感分值DTO（有数据时返回，无数据返回null）
     * @throws RuntimeException 参数为空时抛出运行时异常
     */
    AiActivityScoreRedisDTO queryLastEmotionChangeInfo(Integer userId, EmotionTypeEnum emotionType);
}