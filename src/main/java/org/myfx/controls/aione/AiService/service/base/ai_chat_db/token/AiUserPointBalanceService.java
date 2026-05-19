package org.myfx.controls.aione.AiService.service.base.ai_chat_db.token;

import org.myfx.controls.aione.AiService.entity.ai_chat_db.token.AiUserPointBalance;

/**
 * AI用户统一算力积分 业务接口
 */
public interface AiUserPointBalanceService {

    /**
     * 新增用户积分余额记录
     * @param balance 积分余额实体
     * @return 影响行数
     */
    int addAiUserPointBalance(AiUserPointBalance balance);

    /**
     * 初始化用户积分余额（默认积分 0）
     * @param userId 用户ID
     */
    void initAiUserPointBalance(Integer userId);

    /**
     * 根据用户ID查询积分余额（唯一记录）
     * @param userId 用户ID
     * @return 积分余额实体
     */
    AiUserPointBalance getAiUserPointBalance(Integer userId);

    /**
     * 查询【当前登录用户】的积分余额（唯一记录）
     * 无参：自动从UserContext获取当前登录用户ID
     * @return 积分余额实体
     */
    AiUserPointBalance getAiUserPointBalance();

    /**
     * 根据用户ID 扣减总可用积分余额
     * @param userId 用户ID
     * @param deductPoint 需要扣减的积分数量
     * @return 影响行数
     */
    int deductTotalPointByUserId(Integer userId, Long deductPoint);

    /**
     * 根据用户ID 增加总可用积分余额
     * @param userId 用户ID
     * @param addPoint 需要增加的积分数量
     * @return 影响行数
     */
    int addTotalPointByUserId(Integer userId, Long addPoint);

    /**
     * 根据用户ID删除积分余额记录
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteAiUserPointBalance(Integer userId);

}