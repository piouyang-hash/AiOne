package org.myfx.controls.aione.AiService.service.base.status.ai_behavior_db;

import org.myfx.controls.aione.AiService.common.ai_chat_db.AiBehaviorEnum;
import org.myfx.controls.aione.AiService.common.ai_chat_db.EmotionTypeEnum;
import org.myfx.controls.aione.AiService.dto.AiBehaviorScoreAddDTO;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiBehaviorScore;

import java.util.List;

/**
 * AI行为分值配置 业务接口
 * 对应表：ai_behavior_score（行为-多维度分值关联表）
 */
public interface AiBehaviorScoreService {

    // ========== 增删改（核心操作） ==========
    /**
     * 新增AI行为分值配置
     */
    boolean addAiBehaviorScore(AiBehaviorScoreAddDTO addDTO);

    /**
     * 根据ID更新AI行为分值配置
     */
    boolean updateAiBehaviorScoreById(AiBehaviorScore score);

    /**
     * 根据ID删除单条分值配置
     */
    boolean deleteAiBehaviorScoreById(Integer id);

    /**
     * 根据行为编码删除所有分值配置
     */
    boolean deleteAiBehaviorScoreByCode(AiBehaviorEnum behaviorCode);

    // ========== 查询操作 ==========
    /**
     * 根据ID查询单条分值配置
     */
    AiBehaviorScore getAiBehaviorScoreById(Integer id);

    /**
     * 根据行为编码查询所有分值配置
     */
    List<AiBehaviorScore> getScoreListByBehaviorCode(AiBehaviorEnum behaviorCode);

    /**
     * 根据行为编码 + 分值类型 精准查询
     */
    AiBehaviorScore getScoreByCodeAndType(AiBehaviorEnum behaviorCode, EmotionTypeEnum scoreType);
}