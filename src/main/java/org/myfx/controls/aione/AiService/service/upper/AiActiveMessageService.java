package org.myfx.controls.aione.AiService.service.upper;

/**
 * AI主动消息服务（核心聚焦AI主动触达的主动性校验）
 */
public interface AiActiveMessageService {

    /**
     * 检查AI主动消息的主动性（核心：基于活跃度判断AI主动触达的意愿）
     * @param userId 用户ID
     * @return AI活跃度值（0=最低/无主动意愿，50=中等，100=最高/强主动意愿）
     */
    int checkActiveInitiative(Integer userId);


    /**
     * 检查AI熟悉度并生成对应的系统指令消息（向AI下达主动交互要求）
     * @param userId    用户ID
     * @return 系统指令字符串（AI需执行的主动交互要求，非AI回复内容）
     */
    String checkAiFamiliarity(Integer userId);
}