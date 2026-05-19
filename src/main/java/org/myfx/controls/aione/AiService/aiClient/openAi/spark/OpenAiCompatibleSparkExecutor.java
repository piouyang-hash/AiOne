package org.myfx.controls.aione.AiService.aiClient.openAi.spark;

import jakarta.annotation.Resource;
import org.myfx.controls.aione.AiService.aiClient.AMTest.AiModelClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
/**
 * OpenAI兼容的讯飞星火(Spark)模型执行器
 * 职责：仅提供自定义Prompt的基础调用能力（流式/阻塞），无默认提示词，聚焦通用调用逻辑
 */
@Component
public class OpenAiCompatibleSparkExecutor { // 类名突出OpenAI兼容+Spark

    @Resource(name = "sparkChatClient")
    private AiModelClient sparkChatClient; // 变量名明确关联Spark模型

    // ========== 核心方法1：自定义Prompt的阻塞式调用（OpenAI兼容） ==========
    /**
     * 讯飞星火模型自定义Prompt阻塞式调用
     * @param systemPrompt 系统提示词（定义AI角色/规则，不能为空）
     * @param userInput 用户输入消息（不能为空）
     * @return AI返回的完整内容
     * @throws IllegalArgumentException 当systemPrompt/userInput为空时抛出
     */
    public String chatWithCustomPromptSync(String systemPrompt, String userInput) {
        // 补充参数校验（保证调用合法性，和之前封装风格一致）
        validatePromptParams(systemPrompt, userInput);

        return sparkChatClient.getChatClient()
                .prompt()
                .system(systemPrompt)
                .user(userInput)
                .call()
                .content();
    }

    // ========== 核心方法2：自定义Prompt的流式调用（OpenAI兼容） ==========
    /**
     * 讯飞星火模型自定义Prompt流式调用
     * @param systemPrompt 系统提示词（定义AI角色/规则，不能为空）
     * @param userInput 用户输入消息（不能为空）
     * @return 流式返回的AI内容片段（Flux<String>）
     * @throws IllegalArgumentException 当systemPrompt/userInput为空时抛出
     */
    public Flux<String> chatWithCustomPrompt(String systemPrompt, String userInput) {
        // 统一参数校验
        validatePromptParams(systemPrompt, userInput);

        return sparkChatClient.getChatClient()
                .prompt()
                .system(systemPrompt)
                .user(userInput)
                .stream()
                .content();
    }

    // ========== 私有工具方法：参数合法性校验 ==========
    /**
     * 校验系统提示词和用户输入非空（避免无效调用）
     */
    private void validatePromptParams(String systemPrompt, String userInput) {
        if (systemPrompt == null || systemPrompt.isBlank()) {
            throw new IllegalArgumentException("参数不合法：系统提示词（systemPrompt）不能为空");
        }
        if (userInput == null || userInput.isBlank()) {
            throw new IllegalArgumentException("参数不合法：用户输入（userInput）不能为空");
        }
    }
}