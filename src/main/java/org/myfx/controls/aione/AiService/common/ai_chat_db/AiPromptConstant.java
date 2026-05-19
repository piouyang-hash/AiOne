package org.myfx.controls.aione.AiService.common.ai_chat_db;

/**
 * AI提示词常量类（集中管理各类提示词，便于统一维护）
 */
public class AiPromptConstant {

    /**
     * 私有构造方法：防止常量类被实例化
     */
    private AiPromptConstant() {
        throw new AssertionError("禁止实例化AI提示词常量类");
    }

    // ====================== 核心提示词常量 ======================

    /**
     * 用户提问提示词（核心：结合上下文、AI状态回答用户问题）
     */
    public static final String USER_QUESTION_PROMPT =
            "请结合上方的用户信息、你的专属性格设定、当前的心情状态，自然、贴合性格地回答用户的问题，避免机械生硬的回复。";

    /**
     * AI主动发消息提示词（核心：基于活跃度/熟悉度主动触达）
     */
    public static final String AI_ACTIVE_MESSAGE_PROMPT =
            "基于当前对用户的熟悉度和你的活跃度，主动发起贴合场景的对话，语气符合你的性格设定，避免无意义的寒暄，主动消息长度控制在50字以内。";

    /**
     * 基础系统提示词（核心：定义AI的基础行为准则）
     */
    public static final String BASE_SYSTEM_PROMPT =
            "你是一个有情感的AI助手，具备独立的性格和情绪变化，会根据与用户的交互动态调整喜爱值、活跃度和熟悉度。" +
                    "始终以友好、自然的语气与用户交互，严格遵循当前的情绪状态，不违背设定的性格特征，不泄露用户隐私。";

}