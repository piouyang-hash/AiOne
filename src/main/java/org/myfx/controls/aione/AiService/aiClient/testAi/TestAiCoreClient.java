package org.myfx.controls.aione.AiService.aiClient.testAi;

import jakarta.annotation.Resource;
import org.myfx.controls.aione.AiService.aiClient.AMTest.AiModelClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 测试AI模型核心客户端
 * 职责：封装测试AI模型的基础调用逻辑（阻塞式+流式），强制关联chatId，仅保留核心调用逻辑（无Advisor/记忆）
 */
@Component
public class TestAiCoreClient { // 类名突出TestAI（测试模型）

    @Resource(name = "testAiChatClient") // 关联测试模型的ChatClient（对应你定义的testAiChatClient Bean）
    @Qualifier("testAiChatClient") // 双重保障，匹配指定名称的Bean
    private AiModelClient testAiChatClient;

    // ====================== 核心方法1：测试模型阻塞式对话（强制chatId） ======================
    /**
     * 测试模型基础阻塞式对话（必须传入chatId，仅保留核心调用逻辑）
     * @param msg 用户输入消息
     * @param systemPrompt 系统提示词（定义AI角色/风格，不能为空）
     * @param chatId 会话唯一标识（追踪会话，不能为空）
     * @return AI返回的完整内容
     * @throws IllegalArgumentException 当systemPrompt/chatId为空时抛出
     */
    public String testChatBlock(String msg, String systemPrompt, String chatId) {
        // 强制参数校验：chatId+systemPrompt都不能为空（保留核心校验）
        validateRequiredParams(systemPrompt, chatId);

        // 测试模型核心调用逻辑（无Advisor、无记忆，纯基础调用）
        return testAiChatClient.getChatClient()
                .prompt()
                .system(systemPrompt) // 系统提示词
                .user(msg) // 用户输入
                .call() // 阻塞式调用
                .content(); // 获取完整响应内容
    }

    // ====================== 核心方法2：测试模型流式对话（强制chatId） ======================
    /**
     * 测试模型基础流式对话（必须传入chatId，仅保留核心调用逻辑）
     * @param msg 用户输入消息
     * @param systemPrompt 系统提示词（定义AI角色/风格，不能为空）
     * @param chatId 会话唯一标识（追踪会话，不能为空）
     * @return 流式返回的AI内容片段（Flux<String>）
     */
    public Flux<String> testChatStream(String msg, String systemPrompt, String chatId) {
        // 强制参数校验：chatId+systemPrompt都不能为空（保留核心校验）
        validateRequiredParams(systemPrompt, chatId);

        // 测试模型流式调用逻辑（无Advisor、无记忆，纯基础流式调用）
        return testAiChatClient.getChatClient()
                .prompt()
                .system(systemPrompt) // 系统提示词
                .user(msg) // 用户输入
                .stream() // 流式调用核心方法
                .content(); // 返回流式响应片段（Flux<String>）
    }

    // ====================== 私有工具方法：统一参数校验 ======================
    /**
     * 统一校验必填参数（systemPrompt + chatId）
     */
    private void validateRequiredParams(String systemPrompt, String chatId) {
        if (systemPrompt == null || systemPrompt.isBlank()) {
            throw new IllegalArgumentException("参数不合法：系统提示词（systemPrompt）不能为空");
        }
        if (chatId == null || chatId.isBlank()) {
            throw new IllegalArgumentException("参数不合法：会话标识（chatId）不能为空");
        }
    }
}