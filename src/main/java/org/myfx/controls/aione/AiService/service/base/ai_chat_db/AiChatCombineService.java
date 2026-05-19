package org.myfx.controls.aione.AiService.service.base.ai_chat_db;

import org.myfx.controls.aione.AiService.vo.AiChatSessionVO;

import java.util.List;

/**
 * AI对话整合业务接口（核心：聚合会话+消息数据）
 * 仅提供“获取用户所有正常会话VO（含最新消息内容）”的能力
 */
public interface AiChatCombineService {

    /**
     * 获取当前登录用户所有正常会话VO（含最新消息content）
     * 【注意】仅允许控制器层（加@CheckJwt）调用，非控制器代码禁止调用
     * @return 带最新消息content的会话VO列表
     */
    List<AiChatSessionVO> getUserAllNormalSessionVOWithLatestContent();
}