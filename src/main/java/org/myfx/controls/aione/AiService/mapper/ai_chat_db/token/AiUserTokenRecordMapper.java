package org.myfx.controls.aione.AiService.mapper.ai_chat_db.token;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.myfx.controls.aione.AiService.common.ai_chat_db.TokenChangeTypeEnum;
import org.myfx.controls.aione.AiService.common.ai_chat_db.TokenChangeWayEnum;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.token.AiUserTokenRecord;

import java.util.List;

/**
 * AI用户Token变动流水 Mapper接口
 */
@Mapper
public interface AiUserTokenRecordMapper {

    /**
     * 新增Token流水记录
     * recordId手动传入雪花ID，createTime数据库自动生成
     * @param record 流水实体
     * @return 影响行数
     */
    int insert(AiUserTokenRecord record);

    /**
     * 根据用户ID查询所有流水记录
     * @param userId 用户ID
     * @return 流水列表
     */
    List<AiUserTokenRecord> selectByUserId(Integer userId);

    /**
     * 根据用户ID+变动类型查询流水
     * @param userId 用户ID
     * @param changeType 变动类型枚举
     * @return 流水列表
     */
    List<AiUserTokenRecord> selectByUserIdAndType(
            @Param("userId") Integer userId,
            @Param("changeType") TokenChangeTypeEnum changeType
    );

    /**
     * 根据用户ID+变动方式查询流水
     * @param userId 用户ID
     * @param changeWay 变动方式枚举
     * @return 流水列表
     */
    List<AiUserTokenRecord> selectByUserIdAndWay(
            @Param("userId") Integer userId,
            @Param("changeWay") TokenChangeWayEnum changeWay
    );

    /**
     * 根据用户ID+变动类型+变动方式 联合查询流水
     * @param userId 用户ID
     * @param changeType 变动类型
     * @param changeWay 变动方式
     * @return 流水列表
     */
    List<AiUserTokenRecord> selectByUserIdAndTypeAndWay(
            @Param("userId") Integer userId,
            @Param("changeType") TokenChangeTypeEnum changeType,
            @Param("changeWay") TokenChangeWayEnum changeWay
    );

    /**
     * 根据用户ID删除所有流水记录
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteByUserId(Integer userId);
}