package org.myfx.controls.aione.AiService.mapper.ai_chat_db;

import org.apache.ibatis.annotations.Param;
import org.myfx.controls.aione.AiService.common.ai_chat_db.EmotionTypeEnum;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiEmotionLogDetail;

import java.util.List;

/**
 * AI情绪变动流水表 Mapper 接口（基础CRUD）
 */
public interface AiEmotionLogDetailMapper {

    /**
     * 新增情绪变动日志
     * @param logDetail 情绪变动日志实体
     * @return 影响行数
     */
    int insert(AiEmotionLogDetail logDetail);

    /**
     * 根据ID删除日志（物理删除）
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据ID更新日志
     * @param logDetail 情绪变动日志实体（含更新值）
     * @return 影响行数
     */
    int updateById(AiEmotionLogDetail logDetail);

    /**
     * 根据ID查询单条日志
     * @param id 主键ID
     * @return 情绪变动日志实体
     */
    AiEmotionLogDetail selectById(@Param("id") Long id);

    /**
     * 条件查询日志列表（基础通用查询）
     * @param logDetail 查询条件（非空字段均为筛选条件）
     * @return 日志列表
     */
    List<AiEmotionLogDetail> selectList(AiEmotionLogDetail logDetail);

    /**
     * 按用户ID+情绪类型查询日志（高频业务场景）
     * @param userId 用户ID
     * @param emotionType 情绪类型（LIKE/ACTIVITY/FAMILIAR）
     * @return 该用户对应情绪类型的变动日志列表
     */
    List<AiEmotionLogDetail> selectByUserIdAndEmotionType(
            @Param("userId") Integer userId,
            @Param("emotionType") EmotionTypeEnum emotionType
    );

    /**
     * 按用户ID+情绪类型查询最新N条日志（支持指定数量）
     * @param userId 用户ID
     * @param emotionType 情绪类型
     * @param limitNum 查询数量（如2表示查最新2条）
     * @return 最新N条日志列表（按创建时间降序）
     */
    List<AiEmotionLogDetail> selectLatestNByUserIdAndEmotionType(
            @Param("userId") Integer userId,
            @Param("emotionType") EmotionTypeEnum emotionType,
            @Param("limitNum") Integer limitNum
    );
}