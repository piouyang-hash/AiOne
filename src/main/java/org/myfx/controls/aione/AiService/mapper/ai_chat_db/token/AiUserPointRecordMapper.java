package org.myfx.controls.aione.AiService.mapper.ai_chat_db.token;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.token.AiUserPointRecord;

import java.util.List;

/**
 * AI用户算力积分变动流水表 Mapper
 */
@Mapper
public interface AiUserPointRecordMapper {

    /**
     * 新增积分流水记录
     * @param record 积分流水实体
     * @return 影响行数
     */
    int insert(AiUserPointRecord record);

    /**
     * 根据用户ID查询积分流水
     * @param userId 用户ID
     * @return 流水列表
     */
    List<AiUserPointRecord> selectByUserId(@Param("userId") Integer userId);

    /**
     * 根据用户ID+变动类型查询积分流水
     */
    List<AiUserPointRecord> selectByUserIdAndType(
            @Param("userId") Integer userId,
            @Param("changeType") Integer changeType
    );

    /**
     * 根据用户ID+变动方式查询积分流水
     */
    List<AiUserPointRecord> selectByUserIdAndWay(
            @Param("userId") Integer userId,
            @Param("changeWay") String changeWay
    );

    /**
     * 根据用户ID+变动类型+变动方式联合查询
     */
    List<AiUserPointRecord> selectByUserIdAndTypeAndWay(
            @Param("userId") Integer userId,
            @Param("changeType") Integer changeType,
            @Param("changeWay") String changeWay
    );

    /**
     * 根据用户ID删除积分流水
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteByUserId(@Param("userId") Integer userId);

}