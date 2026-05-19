package org.myfx.controls.aione.AiService.service.base.status.ai_behavior_db;

import org.myfx.controls.aione.AiService.common.ai_chat_db.EmotionTypeEnum;
import org.myfx.controls.aione.AiService.dto.AiEmotionScoreOperateDTO;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiEmotionRealState;

import java.util.List;

/**
 * AI情绪实时状态业务接口
 */
public interface AiEmotionRealStateService {

    /**
     * 初始化AI感情
     * @param userId 用户ID
     * @return 初始化后的情绪状态实体
     */
    AiEmotionRealState initAiEmotion(Integer userId);

    /**
     * 查询高活跃度（activity_value=100）的用户ID列表
     * @return 高活跃度用户ID列表（无数据返回空列表）
     */
    List<Integer> listHighActivityUserIds();

    /**
     * 查询喜爱度较高的用户ID列表
     * @param likeThreshold 喜爱度阈值（必须在 -100 ~ 100 之间）
     * @return 喜爱度大于该阈值的用户ID列表（无数据返回空列表）
     */
    List<Integer> listHighLikeUserIds(Integer likeThreshold);

    /**
     * 开始AI活跃度递增
     * 逻辑：查询AI情绪真实状态 → 校验分值范围 → 构建DTO并保存到Redis
     * @param userId 用户ID
     */
    void startAiActivityScoreIncrement(Integer userId);

    /**
     * 获取当前AI感情状态（核心查询）
     * @param userId 用户ID
     * @return 情绪状态对象
     */
    AiEmotionRealState getCurrentAiEmotion(Integer userId);

    /**
     * 切换AI感情有效性（关闭/打开）
     * @param aiEmotionRealState 需包含：userId、isValid（1=打开，0=关闭）
     * @return 影响行数
     */
    int toggleAiEmotionValidStatus(AiEmotionRealState aiEmotionRealState);

    /**
     * 更新AI心情分数（活跃度/喜爱值/熟悉度）
     * @param operateDTO AI心情分数操作DTO
     * @return 更新前的旧分值（无记录/无变化返回null）
     */
    Integer updateAiEmotionScore(AiEmotionScoreOperateDTO operateDTO);

    /**
     * 补满指定用户的某类AI情绪分值（将该情绪分值置为100）
     * 核心逻辑：查询旧分值 → 计算加分值（100 - 旧分值）→ 调用更新方法完成补满
     * @param userId 用户ID（必传，正整数）
     * @param emotionType 情绪类型（必传，指定要补满的分值类型，如ACTIVITY=活跃度）
     * @return 包含补满操作信息的DTO：
     *         - scoreBefore：补满前的原始分值（无旧记录为0，分值无变化为100）
     *         - 其他字段：userId/emotionType/addScore/aiBehaviorId 均为本次操作的参数
     * @throws IllegalArgumentException 入参不合法时抛出
     */
    AiEmotionScoreOperateDTO fillFullAiEmotionScore(Integer userId, EmotionTypeEnum emotionType);

    /**
     * 简化接口：补满指定用户的「AI活跃度分值」（固定情绪类型为ACTIVITY）
     * 核心逻辑：直接调用fillFullAiEmotionScore，固定传入EmotionTypeEnum.ACTIVITY
     * @param userId 用户ID（必传，正整数，参数校验由底层fillFullAiEmotionScore方法负责）
     * @return 包含补满操作信息的DTO（字段：userId/aiBehaviorId/emotionType/addScore/scoreBefore）
     * @throws IllegalArgumentException 入参不合法时由底层方法抛出
     */
    AiEmotionScoreOperateDTO fillFullAiActivityScore(Integer userId);
}