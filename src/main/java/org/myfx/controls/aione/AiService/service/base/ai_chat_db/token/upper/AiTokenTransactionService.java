package org.myfx.controls.aione.AiService.service.base.ai_chat_db.token.upper;

/**
 * AI Token 事务服务
 * 职责：统一封装所有Token消耗/增加的事务操作（余额+流水）
 */
public interface AiTokenTransactionService {

    /**
     * 用户主动聊天 Token 消耗
     * @return boolean true=消耗成功 false=Token余额不足
     */
    boolean consumeTokenByUserChat(Integer userId, Long typeId, Long consumeAmount);

    /**
     * AI回复输出 Token 消耗
     * @return boolean true=消耗成功 false=Token余额不足
     */
    boolean consumeTokenByAiReply(Integer userId, Long typeId, Long consumeAmount);

    /**
     * 初始化用户Token余额
     * @param userId 用户ID
     */
    void initUserTokenBalance(Integer userId);

}