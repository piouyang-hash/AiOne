package org.myfx.controls.aione.AiService.service.base.ai_chat_db.token;

import org.myfx.controls.aione.AiService.entity.ai_chat_db.token.AiUserTokenBalance;

import java.util.List;

/**
 * AI用户Token余额 业务接口
 */
public interface AiUserTokenBalanceService {

    /**
     * 新增用户Token余额
     * @param aiUserTokenBalance 余额实体
     * @return 影响行数
     */
    int addAiUserTokenBalance(AiUserTokenBalance aiUserTokenBalance);

    /**
     * 根据用户ID + Token类型ID 查询Token余额（单个）
     * @param userId 用户ID
     * @param typeId Token类型ID
     * @return 余额实体
     */
    AiUserTokenBalance getAiUserTokenBalance(Integer userId, Long typeId);

    /**
     * 根据用户ID查询所有类型的Token余额
     * @param userId 用户ID
     * @return 余额实体集合
     */
    List<AiUserTokenBalance> getAiUserTokenBalanceList(Integer userId);

    /**
     * 根据【当前登录用户ID】查询所有类型的Token余额
     * 从UserContext获取用户ID，调用重载方法
     * @return 余额实体集合
     */
    List<AiUserTokenBalance> getAiUserTokenBalanceList();

    /**
     * 增量累加Token消耗 + 扣减可用余额
     * @param userId        用户ID
     * @param typeId        Token类型ID
     * @param consumeAmount 本次消耗数量
     * @return 影响行数
     */
    int incrConsume(Integer userId, Long typeId, Long consumeAmount);

    /**
     * 根据用户ID删除Token余额
     * @param userId 用户ID
     * @return 影响行数
     */
    int removeAiUserTokenBalance(Integer userId);
}