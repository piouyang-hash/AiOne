package org.myfx.controls.aione.AiService.service.base.ai_chat_db.token;

import org.myfx.controls.aione.AiService.common.ai_chat_db.TokenChangeTypeEnum;
import org.myfx.controls.aione.AiService.common.ai_chat_db.TokenChangeWayEnum;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.token.AiUserTokenRecord;

import java.util.List;

/**
 * AI用户Token变动流水 业务接口
 */
public interface AiUserTokenRecordService {

    /**
     * 新增Token变动流水记录
     * @param record 流水实体
     * @return 影响行数
     */
    int addAiUserTokenRecord(AiUserTokenRecord record);

    /**
     * 根据用户ID查询流水列表
     * @param userId 用户ID
     * @return 流水列表
     */
    List<AiUserTokenRecord> getAiUserTokenRecordListByUserId(Integer userId);

    /**
     * 根据用户ID+变动类型查询流水列表
     * @param userId 用户ID
     * @param changeType 变动类型
     * @return 流水列表
     */
    List<AiUserTokenRecord> getAiUserTokenRecordListByUserIdAndType(Integer userId, TokenChangeTypeEnum changeType);

    /**
     * 根据用户ID+变动方式查询流水列表
     * @param userId 用户ID
     * @param changeWay 变动方式
     * @return 流水列表
     */
    List<AiUserTokenRecord> getAiUserTokenRecordListByUserIdAndWay(Integer userId, TokenChangeWayEnum changeWay);

    /**
     * 根据用户ID+变动类型+变动方式查询流水列表
     * @param userId 用户ID
     * @param changeType 变动类型
     * @param changeWay 变动方式
     * @return 流水列表
     */
    List<AiUserTokenRecord> getAiUserTokenRecordListByUserIdAndTypeAndWay(Integer userId, TokenChangeTypeEnum changeType, TokenChangeWayEnum changeWay);

    /**
     * 根据用户ID删除所有流水记录
     * @param userId 用户ID
     * @return 影响行数
     */
    int removeAiUserTokenRecordByUserId(Integer userId);
}