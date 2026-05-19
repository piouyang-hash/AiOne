package org.myfx.controls.aione.AiService.service.upper;

/**
 * AI主动聊天触发接口（用户下线场景）
 */
public interface IAiChatInitiateService {

    /**
     * 处理用户下线，准备活跃度递减初始数据
     * 在用户下线时被调用，用于查询用户的最后活跃度变动信息。
     * 若该信息存在且活跃度分值不为0，则将其存入Redis，为后续定时任务进行活跃度递减提供初始值。
     *
     * @param userId 用户ID
     * @return 用户ID（Integer类型，无可用数据时仍返回userId；参数不合法时抛出IllegalArgumentException）
     */
    Integer handleUserOfflineAndInitActivity(Integer userId);

    /**
     * 处理用户上线，删除Redis中该用户的活跃度Key，并触发离线分值处理逻辑
     * @param userId 用户ID（必传）
     * @return 用户ID（Integer类型，参数不合法时抛出IllegalArgumentException；正常处理后返回userId）
     */
    Integer handleUserOnlineClearActivity(Integer userId);

}