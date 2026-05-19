package org.myfx.controls.aione.AiService.mapper.ai_chat_db.token;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.token.AiUserTokenBalance;

import java.util.List;

/**
 * AI用户Token余额 Mapper接口
 */
@Mapper
public interface AiUserTokenBalanceMapper {

    /**
     * 新增用户Token余额记录
     * 主键/时间数据库自动生成，remainingTokens无值则使用数据库默认值0
     * @param aiUserTokenBalance 余额实体
     * @return 影响行数
     */
    int insert(AiUserTokenBalance aiUserTokenBalance);

    /**
     * 增量累加Token消耗 + 扣减可用余额（底层原子操作）
     * @param userId        用户ID
     * @param typeId        Token类型ID
     * @param consumeAmount 本次消耗的数量（增量值）
     * @return 影响行数
     */
    int incrementTotalConsumed(
            @Param("userId") Integer userId,
            @Param("typeId") Long typeId,
            @Param("consumeAmount") Long consumeAmount
    );

    /**
     * 根据用户ID查询所有Token余额（一个用户多类型）
     * @param userId 用户ID
     * @return 余额实体集合
     */
    List<AiUserTokenBalance> selectByUserId(@Param("userId") Integer userId);

    /**
     * 根据用户ID + Token类型ID 查询Token余额（唯一约束，返回单个）
     * @param userId 用户ID
     * @param typeId Token类型ID
     * @return 余额实体
     */
    AiUserTokenBalance selectByUserIdAndTypeId(
            @Param("userId") Integer userId,
            @Param("typeId") Long typeId
    );

    /**
     * 根据用户ID删除Token余额记录
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteByUserId(Integer userId);
}