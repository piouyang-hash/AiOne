package org.myfx.controls.aione.AiService.mapper.ai_chat_db;

import org.apache.ibatis.annotations.*;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiEmotionRealState;

import java.util.List;

/**
 * AI情绪实时状态Mapper接口
 */
@Mapper
public interface AiEmotionRealStateMapper {

    /**
     * 新增AI情绪实时状态
     *
     * @param aiEmotionRealState 情绪状态对象（已移除sessionId字段）
     * @return 影响行数
     */
    int insert(AiEmotionRealState aiEmotionRealState);

    /**
     * 核心：根据用户ID更新情绪字段（喜爱值/活跃度/熟悉度）
     *
     * @param aiEmotionRealState 情绪状态对象（需包含userId及要更新的情绪字段）
     * @return 影响行数
     */
    int updateEmotionByUserId(AiEmotionRealState aiEmotionRealState);

    /**
     * 单独切换is_valid状态（用户AI情绪状态有效性）
     *
     * @param aiEmotionRealState 情绪状态对象（需包含userId和目标isValid值）
     * @return 影响行数
     */
    int updateIsValidByUserId(AiEmotionRealState aiEmotionRealState);

    /**
     * 根据ID查询情绪状态
     *
     * @param id 主键ID
     * @return 情绪状态对象
     */
    AiEmotionRealState selectById(@Param("id") Long id);

    /**
     * 根据用户ID查询情绪状态（核心查询场景）
     *
     * @param userId 用户ID（适配数据库BIGINT类型，改为Long）
     * @return 情绪状态对象
     */
    AiEmotionRealState selectByUserId(@Param("userId") Integer userId);

    /**
     * 查询所有活跃度为100的用户ID列表
     * @return 活跃度=100的用户ID列表（无数据返回空列表，非null）
     */
    List<Integer> selectUserIdsByActivityValue100();

    /**
     * 查询喜爱度大于指定阈值的用户ID列表
     * @param likeThreshold 喜爱度阈值
     * @return like_value > 阈值的用户ID列表（无数据返回空列表，非null）
     */
    List<Integer> selectUserIdsByLikeValueGtThreshold(Integer likeThreshold);

    /**
     * 查询所有有效且未删除的情绪状态数据
     * （is_deleted=0：未删除；is_valid=1：有效；返回结果排除is_valid/is_deleted字段）
     *
     * @return 有效且未删除的情绪状态列表
     */
    List<AiEmotionRealState> selectAllValidNonDeletedAiEmotionRealState();

}