package org.myfx.controls.aione.AiService.service.base.ai_chat_db.token;

import org.myfx.controls.aione.AiService.entity.ai_chat_db.token.AiUserPointRecord;

import java.util.List;

/**
 * AI用户算力积分流水 业务接口
 */
public interface AiUserPointRecordService {

    /**
     * 新增积分流水记录
     * @param record 积分流水实体
     * @return 影响行数
     */
    int addAiUserPointRecord(AiUserPointRecord record);

    /**
     * 根据用户ID查询积分流水列表
     * @param userId 用户ID
     * @return 流水列表
     */
    List<AiUserPointRecord> getByUserId(Integer userId);

    /**
     * 根据用户ID+变动类型查询积分流水
     */
    List<AiUserPointRecord> getByUserIdAndType(Integer userId, Integer changeType);

    /**
     * 根据用户ID+变动方式查询积分流水
     */
    List<AiUserPointRecord> getByUserIdAndWay(Integer userId, String changeWay);

    /**
     * 根据用户ID+变动类型+变动方式联合查询
     */
    List<AiUserPointRecord> getByUserIdAndTypeAndWay(Integer userId, Integer changeType, String changeWay);

    /**
     * 根据用户ID删除积分流水
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteByUserId(Integer userId);
}