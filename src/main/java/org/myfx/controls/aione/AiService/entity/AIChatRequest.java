package org.myfx.controls.aione.AiService.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.myfx.controls.aione.AiService.aiClient.SpringAiPromptConvertUtil;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiChatMessage;
import org.myfx.controls.aione.AiService.utils.ConversationIdUtil;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * AI对话请求实体
 * 整合系统提示词、历史对话、用户输入等核心参数，用于AI模型调用
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI对话请求实体") // 类级别的Swagger注解
public class AIChatRequest {

    /**
     * 用户ID（关联会话/消息的用户标识）
     */
    @Schema(description = "用户ID", example = "10086")
    private Integer userId;

    /**
     * 会话ID（关联AI对话会话的唯一标识）
     */
    @Schema(description = "会话ID", example = "1789012345678901234")
    private Long sessionId;

    /**
     * 对话唯一标识（由userId+sessionId自动生成）
     */
    @Schema(description = "对话唯一标识（自动生成）", example = "conv_10086_1789012345678901234")
    private String conversationId;

    /**
     * 系统提示词（包含序列号、提示词内容等业务信息，转换为 Spring AI 标准 SystemMessage）
     */
    @Schema(description = "系统提示词实体（Spring AI 标准格式）")
    private SystemMessage systemPrompt;

    /**
     * 历史对话摘要（仅总结型滑动窗口策略填写，其他上下文策略无需赋值，String 类型方便传入）
     */
    @Schema(description = "历史对话摘要（仅总结型滑动窗口策略填写，其他上下文策略无需赋值）")
    private String historySummary; // 改回 String 类型，贴合传入便捷性要求

    /**
     * 历史对话列表（按时间排序的用户/助手消息）
     */
    @Schema(description = "历史对话列表")
    private List<Message> historyMessages; // 已完成修改，保留

    /**
     * 用户当前输入消息（转换为 Spring AI 标准 UserMessage）
     */
    @Schema(description = "用户当前输入消息", example = "总结一下刚才的对话内容")
    private UserMessage userInput;

    /**
     * 模型温度（控制生成内容的随机性）
     */
    @Schema(description = "模型温度", example = "0.7")
    private Float temperature; // 去掉默认值

    /**
     * 最大令牌数（控制生成内容的长度）
     */
    @Schema(description = "最大令牌数", example = "2000")
    private Integer maxTokens; // 去掉默认值

    /**
     * 是否流式调用（true=流式，false=同步阻塞）
     */
    @Schema(description = "是否流式调用", example = "false")
    private Boolean stream; // 去掉默认值


    // ========== 核心：新增双参数构造方法（仅userId + sessionId），自动生成conversationId ==========
    /**
     * 简易构造方法：传入用户ID和会话ID，自动生成对话唯一标识conversationId
     * @param userId 用户ID
     * @param sessionId 会话ID
     */
    public AIChatRequest(Integer userId, Long sessionId) {
        // 1. 给当前对象的userId和sessionId赋值
        this.userId = userId;
        this.sessionId = sessionId;

        // 2. 自动生成conversationId（非空判断，避免工具类报空指针）
        if (userId != null && sessionId != null) {
            this.conversationId = ConversationIdUtil.generateConversationId(userId, sessionId);
        }
    }

    // ========== 核心：特殊拼接 set 方法（使用 SystemMessage 源码方法实现拼接） ==========
    /**
     * 特殊拼接 set 方法：传入历史摘要字符串，拼接至 systemPrompt 中（使用 SystemMessage 自身 Builder/mutate）
     * @param historySummaryStr 待拼接的历史摘要字符串（格式化后的 <聊天历史摘要>：xxx\n）
     */
    public void setHistorySummaryAndAppendToSystemPrompt(String historySummaryStr) {
        // 1. 处理传入字符串为空的情况（避免拼接空内容）
        if (!StringUtils.hasText(historySummaryStr)) {
            this.historySummary = null;
            return;
        }

        // 2. 给 historySummary 字段赋值（存储原始传入的摘要字符串）
        this.historySummary = historySummaryStr;

        // 3. 拼接 systemPrompt：分两种场景（原有 systemPrompt 为 null / 不为 null）
        String newSystemPromptText;
        if (this.systemPrompt == null || !StringUtils.hasText(this.systemPrompt.getText())) {
            // 场景1：原有 systemPrompt 为空，直接使用历史摘要作为新内容
            throw new RuntimeException("系统提示词不能为空！");
        } else {
            // 场景2：原有 systemPrompt 有内容，拼接（原有内容 + 历史摘要）
            newSystemPromptText = this.systemPrompt.getText() + historySummaryStr;
        }

        // 4. 使用 SystemMessage 源码方法构建/更新 SystemMessage
        if (this.systemPrompt == null) {
            // 场景1：原有 systemPrompt 为 null，使用 Builder 构建新的 SystemMessage
            this.systemPrompt = SystemMessage.builder()
                    .text(newSystemPromptText)
                    .build();
        } else {
            // 场景2：原有 systemPrompt 不为 null，使用 mutate() 获取 Builder，更新 text 后重建
            this.systemPrompt = this.systemPrompt.mutate()
                    .text(newSystemPromptText)
                    .build();
        }
    }


    // ========== 核心：实现转换 set 方法 ==========
    /**
     * 接收 List<AiChatMessage> 并转换为 List<Message> 后设置给 historyMessages
     * @param aiChatMessages 原始 AiChatMessage 列表
     */
    public void setHistoryMessages(List<AiChatMessage> aiChatMessages) {
        // 调用工具类完成转换，工具类方法为 static，直接通过类名调用
        this.historyMessages = SpringAiPromptConvertUtil.convertToMessageList(aiChatMessages);
    }

    // ========== systemPrompt 的转换 set 方法（接收 String，转换为 SystemMessage） ==========
    /**
     * 接收 String 类型系统提示词，转换为 Spring AI SystemMessage 后设置
     * @param systemPromptStr 原始字符串类型系统提示词
     */
    public void setSystemPrompt(String systemPromptStr) {
        // 调用工具类静态方法完成转换
        this.systemPrompt = SpringAiPromptConvertUtil.buildSystemMessage(systemPromptStr);
    }

    // ========== userInput 的转换 set 方法（接收 String，转换为 UserMessage） ==========
    /**
     * 接收 String 类型用户输入，转换为 Spring AI UserMessage 后设置
     * @param userInputStr 原始字符串类型用户输入
     */
    public void setUserInput(String userInputStr) {
        // 调用工具类静态方法完成转换
        this.userInput = SpringAiPromptConvertUtil.buildUserMessage(userInputStr);
    }

}