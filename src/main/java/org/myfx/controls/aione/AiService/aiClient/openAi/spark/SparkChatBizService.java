package org.myfx.controls.aione.AiService.aiClient.openAi.spark;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * 业务工具类：封装各类AI交互业务场景（基于OpenAI兼容的讯飞星火模型）
 * 职责：定义业务专属Prompt，调用底层执行器实现场景化能力，对外提供业务方法
 */
@Service
public class SparkChatBizService {
    // ========== 场景专属Prompt常量（优化+新增） ==========
    // 文本截断阈值：超过500字则截断
    private static final int TEXT_LENGTH_THRESHOLD = 500;
    // 文本总结Prompt（保留）
    private static final String SUMMARY_PROMPT = "你是一个专业的文本总结助手，要求：1. 总结内容简洁；2. 保留核心信息；3. 语言通俗易懂";

    // 翻译Prompt（优化：仅英文→中文，中文输入返回无翻译需求）
    private static final String TRANSLATE_PROMPT = "你是一个专业的翻译助手，仅处理英文到中文的翻译需求：1. 若输入内容为英文，准确翻译成通顺的中文；2. 若输入内容为中文/无英文内容，直接返回'无英文内容需翻译'；3. 翻译结果仅返回译文，无额外解释、标点或备注";

    // 新增：主题标签生成Prompt（精准匹配用户涉及内容，5字以内标签）
    // 通用标准版主题标签生成Prompt（先大类后具体，适配全场景）
    private static final String TOPIC_TAG_PROMPT = "你是一个专业的通用主题标签生成助手，需遵循以下通用标准规则：1. 先识别文本所属通用大类（如技术领域、生活爱好、饮食偏好、出行方式、日常闲聊等），再基于大类生成1~3个核心主题标签（中文，逗号分隔）；2. 每个标签需包含「大类+具体内容」（或仅大类，视内容复杂度），且整体简洁（5个字以内）、精准贴合用户涉及的核心内容；3. 若文本仅为无实质内容的日常寒暄（如'哈哈''你好''哈喽'等），直接返回空字符串；4. 仅返回标签/空字符串，无任何额外文字、标点、解释或备注；5. 大类需通用标准化，常见大类参考：技术领域、生活爱好、饮食偏好、出行方式、学习提升、工作相关、情感表达、日常闲聊（无实质内容）。";

    // 普通聊天默认系统提示词（保留）
    private static final String DEFAULT_CHAT_PROMPT = "你是一个智能助手，回答简洁友好，使用中文回复";

    // 底层执行器（保留）
    private final OpenAiCompatibleSparkExecutor sparkExecutor;

    // 构造器注入（保留）
    public SparkChatBizService(OpenAiCompatibleSparkExecutor sparkExecutor) {
        this.sparkExecutor = sparkExecutor;
    }

    // ========== 原有方法（保留，无修改） ==========
    // 普通聊天-流式
    public Flux<String> chatWithSpark(String userInput) {
        return sparkExecutor.chatWithCustomPrompt(DEFAULT_CHAT_PROMPT, userInput);
    }

    // 普通聊天-阻塞
    public String chatWithSparkReturnString(String userInput) {
        return sparkExecutor.chatWithCustomPromptSync(DEFAULT_CHAT_PROMPT, userInput);
    }

    // 文本总结-流式
    public Flux<String> summarizeText(String textToSummarize) {
        return sparkExecutor.chatWithCustomPrompt(SUMMARY_PROMPT, textToSummarize);
    }

    // 文本总结-阻塞
    public String summarizeTextReturnString(String textToSummarize) {
        return sparkExecutor.chatWithCustomPromptSync(SUMMARY_PROMPT, textToSummarize);
    }

    // ========== 翻译方法（优化：仅英文→中文） ==========
    // 翻译-流式
    public Flux<String> translateText(String textToTranslate) {
        return sparkExecutor.chatWithCustomPrompt(TRANSLATE_PROMPT, textToTranslate);
    }

    // 翻译-阻塞（核心优化：仅英文译中）
    public String translateTextReturnString(String textToTranslate) {
        return sparkExecutor.chatWithCustomPromptSync(TRANSLATE_PROMPT, textToTranslate);
    }

    // ========== 代码解释方法（保留，无修改） ==========
    // 代码解释-流式
    public Flux<String> explainCode(String code) {
        String codeExplainPrompt = "你是一个专业的编程助手，要求：1. 解释代码的核心逻辑；2. 说明关键语法；3. 语言简洁易懂";
        return sparkExecutor.chatWithCustomPrompt(codeExplainPrompt, code);
    }

    // 代码解释-阻塞
    public String explainCodeReturnString(String code) {
        String codeExplainPrompt = "你是一个专业的编程助手，要求：1. 解释代码的核心逻辑；2. 说明关键语法；3. 语言简洁易懂";
        return sparkExecutor.chatWithCustomPromptSync(codeExplainPrompt, code);
    }

    // ========== 主题标签生成功能（适配：调用截断方法） ==========
    /**
     * 生成文本的核心主题标签（流式）
     * @param text 待生成标签的文本
     * @return 流式返回1~3个主题标签（无主题则返回空）
     */
    public Flux<String> generateTopicTags(String text) {
        // 先截断文本，再传入模型
        String truncatedText = truncateTextIfOverLength(text);
        return sparkExecutor.chatWithCustomPrompt(TOPIC_TAG_PROMPT, truncatedText);
    }

    /**
     * 生成文本的核心主题标签（阻塞）
     * @param text 待生成标签的文本
     * @return 1~3个核心主题标签（中文逗号分隔），无主题则返回空字符串
     */
    public String generateTopicTagsReturnString(String text) {
        // 先截断文本，再传入模型
        String truncatedText = truncateTextIfOverLength(text);
        return sparkExecutor.chatWithCustomPromptSync(TOPIC_TAG_PROMPT, truncatedText);
    }

    // ========== 新增：私有文本截断方法（500字截断） ==========
    /**
     * 文本过长处理：超过500字则截断并标记，null/空文本直接返回
     * @param text 原始文本
     * @return 处理后的文本（≤500字，超长则加截断标记）
     */
    private String truncateTextIfOverLength(String text) {
        // 空值处理：避免NullPointerException
        if (text == null || text.isBlank()) {
            return text;
        }
        // 超过500字则截断，末尾加标记说明
        if (text.length() > TEXT_LENGTH_THRESHOLD) {
            return text.substring(0, TEXT_LENGTH_THRESHOLD) + "【文本过长，已截断至500字】";
        }
        // 未超长则返回原文本
        return text;
    }
}