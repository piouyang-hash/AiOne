package org.myfx.controls.aione.AiService.aiClient;

import org.myfx.controls.aione.AiService.common.ai_chat_db.ChatRoleEnum;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiChatMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.util.Assert;


import java.util.ArrayList;
import java.util.List;

/**
 * Spring AI 提示词转换工具类
 * 核心能力：
 * 1. 业务实体（AiChatMessage）→ Spring AI 标准Message列表
 * 2. 纯字符串 → Spring AI 标准UserMessage/SystemMessage
 */
public final class SpringAiPromptConvertUtil {

    // 私有构造器：禁止实例化工具类
    private SpringAiPromptConvertUtil() {
        throw new UnsupportedOperationException("工具类不可实例化");
    }

    /**
     * 公有工具方法：AiChatMessage（业务历史消息）→ Spring AI Message 列表转换
     * <p>
     * 关键说明：
     * 1. 过滤掉角色为 SPRINGBOOT 的消息（该角色为系统内部消息，无需传给AI模型）；
     * 2. 过滤后可能导致消息总数为「单数」（如原10条→9条），当前滑动窗口场景下使用无影响，后续若需严格轮数匹配需留意；
     * 3. 仅转换 USER/ASSISTANT 两种角色，其他角色会抛出异常。
     * </p>
     * @param historyMessages 业务层历史对话列表（正序：最早→最新）
     * @return Spring AI 标准Message列表（仅含USER/ASSISTANT角色）
     * @throws IllegalArgumentException 消息角色未知/内容为空时抛出
     */
    public static List<Message> convertToMessageList(List<AiChatMessage> historyMessages) {
        List<Message> messageList = new ArrayList<>();

        // 空列表处理：返回空列表（AI无历史上下文）
        if (historyMessages == null || historyMessages.isEmpty()) {
            return messageList;
        }

        // 遍历历史消息，按角色转换（过滤SPRINGBOOT类型）
        for (AiChatMessage msg : historyMessages) {
            ChatRoleEnum role = msg.getRole();
            String content = msg.getContent();

            // 校验消息内容非空
            Assert.notNull(content, "AiChatMessage内容不能为空，消息ID：" + msg.getMessageId());

            // 直接跳过SPRINGBOOT类型的消息（核心过滤逻辑）
            if (ChatRoleEnum.SPRINGBOOT.equals(role)) {
                continue;
            }

            // 仅处理USER/ASSISTANT类型，其他角色抛异常（避免非法数据）
            Message message = switch (role) {
                case USER -> new UserMessage(content);
                case ASSISTANT -> new AssistantMessage(content);
                default -> throw new IllegalArgumentException("未知的消息角色，不支持转换：" + role + "，消息ID：" + msg.getMessageId());
            };
            messageList.add(message);
        }

        return messageList;
    }

    /**
     * 公有工具方法：纯字符串 → Spring AI UserMessage（用户消息）转换
     * @param userInput 用户输入的纯文本内容（如"总结这段对话"）
     * @return Spring AI 标准UserMessage对象
     * @throws IllegalArgumentException 内容为空时抛出
     */
    public static UserMessage buildUserMessage(String userInput) {
        // 校验输入非空，避免传给AI空消息
        Assert.notNull(userInput, "用户消息内容不能为空");
        return new UserMessage(userInput);
    }

    /**
     * 【扩展工具方法】纯字符串 → Spring AI SystemMessage（系统提示词）转换
     * （补充方法，适配系统提示词场景，提升工具类通用性）
     * @param systemPrompt 系统提示词内容（如"你是一个专业的对话总结助手"）
     * @return Spring AI 标准SystemMessage对象
     * @throws IllegalArgumentException 内容为空时抛出
     */
    public static SystemMessage buildSystemMessage(String systemPrompt) {
        Assert.notNull(systemPrompt, "系统提示词内容不能为空");
        return new SystemMessage(systemPrompt);
    }
}