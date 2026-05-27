package org.myfx.controls.aione.AiService.aiClient.advisor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聊天信息核心DTO
 * 【全Advisor链路流通实体】统一承载会话、消息、Token、提示词、流式切分全量数据

 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "聊天信息核心传输DTO（全Advisor链路共享）")
@Slf4j
public class ChatInformationDTO {

    /**
     * 上下文存储KEY（固定值，全工程统一使用）
     */
    public static final String CHAT_INFORMATION_DTO_KEY = "chatInformationDTO";

    // ==================== 1. 会话核心标识 ====================
    @Schema(description = "前端传入会话UUID", example = "a1b2c3d4-e5f6-7890-abcd-1234567890ab")
    private String sessionUuid;

    @Schema(description = "雪花ID-会话主键（UUID转换后）", example = "1789234567890123456")
    private Long sessionId;

    @Schema(description = "归属用户ID（0=匿名，>0=登录用户）", example = "1")
    private Integer userId;

    @Schema(description = "角色ID（不填写的话，会默认使用1）", example = "1")
    private Integer roleId;

    @Schema(description = "流式任务ID（传给前端，让前端可以正确的编排消息）")
    private String taskId;

    @Schema(description = "用户发送消息的时间戳（毫秒级）",
            example = "1775566794837",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long userSendTimestamp;

    @Schema(description = "AI开始回复消息的时间戳（毫秒级，收到第一个响应字符时生成）",
            example = "1775566801123",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long aiReplyStartTimestamp;

    // ==================== 2. 消息关联信息（拆分用户/AI） ====================
    @Schema(description = "雪花ID-用户提问消息ID", example = "1789234567890123456")
    private Long userMessageId;

    @Schema(description = "雪花ID-AI回复消息ID", example = "1789234567890123457")
    private Long aiMessageId;

    @Schema(description = "父消息ID（AI回复关联用户消息）", example = "1789234567890123457")
    private Long parentMsgId;

    @Schema(description = "消息切分结构化JSON", example = "{\"total\":2,\"segments\":{\"1\":\"第一段\",\"2\":\"第二段\"}}")
    private String splitContentJson;

    // ==================== 3. 对话内容（必填） ====================
    @NotBlank(message = "用户消息内容不能为空")
    @Schema(description = "用户消息内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "你好，AI能做什么？")
    private String userMessage;

    @Schema(description = "AI回复切分最后一段", example = "[DONE]")
    private String lastSegment;

    // ==================== 4. AI 提示词与业务开关 ====================
    @Schema(description = "提示词模板名称（文件名称）")
    private String promptTemplateName;

    @Schema(description = "提示词模板对象")
    private PromptTemplate OldPromptTemplate;

    @Schema(description = "动态提示词参数（键值对格式）", example = "{\"age\":\"30\",\"gender\":\"男\"}")
    private Map<String, Object> promptTemplate;

    @Schema(description = "是否为AI主动消息", example = "true")
    private Boolean isActiveMessage;

    // ==================== 5. Token 统计 ====================
    @Schema(description = "输入Token类型ID（关联ai_token_type表，用于计费统计）", example = "1")
    private Long inputTypeId;

    @Schema(description = "输出Token类型ID（关联ai_token_type表，用于计费统计）", example = "2")
    private Long outputTypeId;

    @Schema(description = "输入Token数量", example = "50")
    private Integer inputTokens;

    @Schema(description = "输出Token数量", example = "80")
    private Integer outputTokens;

    // ==================== 6. 完整提示词/原文（流式+非流式） ====================
    @Schema(description = "完整输入提示词（系统+用户）", example = "系统提示词+用户问题全文")
    private String fullPromptText;

    @Schema(description = "AI完整原始输出文本（流式拼接）", example = "AI完整回复内容")
    private String aiReplyContent;

    // 新增：模板对应字段
    @Schema(description = "历史对话背景摘要", example = "用户之前咨询过Java编程相关问题")
    private String historySummary;

    @Schema(description = "历史对话上下文", example = "用户：xxx\nAI：xxx\n用户：yyy\nAI：yyy")
    private List<Message> historyMessagesFormatted;

    @Schema(description = "用户基本信息", example = "年龄25岁，职业Java开发工程师")
    private String userBasicInfo;

    @Schema(description = "用户爱好", example = "阅读技术书籍、户外运动、编程学习")
    private String userHobby;

    @Schema(description = "AI人物设定", example = "专业、耐心、简洁的技术答疑助手")
    private String persona;

    @Schema(description = "核心长期记忆", example = "用户偏好通俗易懂的解释，不喜欢复杂术语")
    private String longTermMemories;

    @Schema(description = "当前正在执行的事件", example = "为用户解答Java编程问题")
    private String currentExecutingEvent;

    // ==================== 静态工具方法（健壮版） ====================
    /**
     * 从Advisor上下文Map中安全获取DTO
     * @param context Spring AI 上下文Map
     * @return 存在则返回DTO，不存在/空则返回null
     */
    public static ChatInformationDTO getFromContext(Map<String, Object> context) {
        if (context == null) {
            return null;
        }
        Object dtoObj = context.get(CHAT_INFORMATION_DTO_KEY);
        return dtoObj instanceof ChatInformationDTO dto ? dto : null;
    }

    /**
     * 填充动态提示词模板参数（仅核心模板字段，非空校验后放入）
     */
    public void fillPromptTemplateParams() {
        // 初始化Map，避免空指针
        if (this.promptTemplate == null) {
            this.promptTemplate = new HashMap<>();
        }

        // 历史对话背景摘要 - 非空才放入
        if (StringUtils.isNotBlank(this.historySummary)) {
            this.promptTemplate.put("historySummary", this.historySummary);
        }
        // 用户基本信息 - 非空才放入
        if (StringUtils.isNotBlank(this.userBasicInfo)) {
            this.promptTemplate.put("userBasicInfo", this.userBasicInfo);
        }
        // 用户爱好 - 非空才放入
        if (StringUtils.isNotBlank(this.userHobby)) {
            this.promptTemplate.put("userHobby", this.userHobby);
        }
        // AI人物设定 - 非空才放入
        if (StringUtils.isNotBlank(this.persona)) {
            this.promptTemplate.put("persona", this.persona);
        }
        // 核心长期记忆 - 非空才放入
        if (StringUtils.isNotBlank(this.longTermMemories)) {
            this.promptTemplate.put("longTermMemories", longTermMemories);
        }
        // 当前执行事件 - 非空才放入
        if (StringUtils.isNotBlank(this.currentExecutingEvent)) {
            this.promptTemplate.put("currentExecutingEvent", this.currentExecutingEvent);
        }
    }
}