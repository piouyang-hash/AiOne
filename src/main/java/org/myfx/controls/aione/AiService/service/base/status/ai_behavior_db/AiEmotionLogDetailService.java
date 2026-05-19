package org.myfx.controls.aione.AiService.service.base.status.ai_behavior_db;

import org.myfx.controls.aione.AiService.common.ai_chat_db.EmotionTypeEnum;
import org.myfx.controls.aione.AiService.dto.AiEmotionScoreOperateDTO;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiEmotionLogDetail;

import java.util.List;

/**
 * AI情绪变动流水表 业务接口
 * 核心能力：新增日志、查询日志、删除日志（无更新能力，符合流水记录不可改的业务规则）
 */
public interface AiEmotionLogDetailService {

    /**
     * 新增AI情绪变动日志明细
     * @param operateDTO AI心情分数操作DTO（含日志新增所需的核心参数）
     * @return 新增操作影响的行数（正常返回1，异常抛错）
     */
    int addEmotionLogDetail(AiEmotionScoreOperateDTO operateDTO);

    /**
     * 根据ID删除情绪变动日志（物理删除）
     * @param id 日志主键ID（非空）
     * @return 删除成功返回1，失败返回0
     */
    int removeEmotionLogDetailById(Long id);

    /**
     * 根据ID查询单条情绪变动日志
     * @param id 日志主键ID（非空）
     * @return 匹配的日志实体，无数据返回null
     */
    AiEmotionLogDetail getEmotionLogDetailById(Long id);

    /**
     * 条件查询情绪变动日志列表
     * @param logDetail 查询条件（非空字段为筛选条件，可传null表示查全部）
     * @return 日志列表（无数据返回空列表）
     */
    List<AiEmotionLogDetail> listEmotionLogDetailByCondition(AiEmotionLogDetail logDetail);

    /**
     * 高频业务：按用户ID+情绪类型查询日志列表
     * @param userId 用户ID（非空）
     * @param emotionType 情绪类型（非空，LIKE/ACTIVITY/FAMILIAR）
     * @return 该用户对应情绪类型的变动日志列表（无数据返回空列表）
     */
    List<AiEmotionLogDetail> listEmotionLogDetailByUserIdAndEmotionType(Integer userId, EmotionTypeEnum emotionType);

    /**
     * 查询指定用户最新的1条指定类型的情感日志
     * @param userId 用户ID（非空）
     * @param emotionType 情感类型（非空，如ACTIVITY/EMOTION等）
     * @return 最新的1条情感日志 | null（无记录时）
     */
    AiEmotionLogDetail getLatestEmotionLog(Integer userId, EmotionTypeEnum emotionType);

    /**
     * 查询指定用户最新的1条活跃度记录（专用方法，封装情感类型为ACTIVITY）
     * @param userId 用户ID（非空）
     * @return 最新的1条活跃度记录 | null（无记录时）
     */
    AiEmotionLogDetail getLatestActivityLog(Integer userId);

}