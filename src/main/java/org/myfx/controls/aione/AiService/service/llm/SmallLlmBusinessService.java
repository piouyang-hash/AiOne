package org.myfx.controls.aione.AiService.service.llm;

import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * 小模型业务核心接口（聚焦总结类业务）
 */
public interface SmallLlmBusinessService {

    /**
     * 调用讯飞星火小模型完成文本总结
     * @param msg 待总结的文本内容
     * @return 精准总结后的文本
     */
    String summarizeText(String msg);

    /**
     * 调用讯飞星火小模型完成对话消息列表总结（多Message输入）
     * @param messages 待总结的对话消息列表（Spring AI的Message类型）
     * @return 对话消息的核心总结内容
     */
    String summarizeMessages(List<Message> messages);
}
