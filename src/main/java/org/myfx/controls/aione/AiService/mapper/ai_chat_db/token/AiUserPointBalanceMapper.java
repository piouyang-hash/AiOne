package org.myfx.controls.aione.AiService.mapper.ai_chat_db.token;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.token.AiUserPointBalance;

/**
 * AI用户统一算力积分表 Mapper
 */
@Mapper
public interface AiUserPointBalanceMapper {

    /**
     * 新增用户积分余额记录
     * @param balance 积分余额实体
     * @return 影响行数
     */
    int insert(AiUserPointBalance balance);

    /**
     * 底层原子操作：根据用户ID 直接更新总可用积分（赋值为最终余额）
     * @param userId        用户ID
     * @param totalPoint    更新后的最终可用积分余额
     * @return 影响行数
     */
    int updateTotalPointByUserId(
            @Param("userId") Integer userId,
            @Param("totalPoint") Long totalPoint
    );

    /**
     * 根据用户ID查询积分余额（唯一）
     * @param userId 用户ID
     * @return 积分余额实体
     */
    AiUserPointBalance selectByUserId(@Param("userId") Integer userId);

    /**
     * 根据用户ID删除积分余额记录
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteByUserId(@Param("userId") Integer userId);

}