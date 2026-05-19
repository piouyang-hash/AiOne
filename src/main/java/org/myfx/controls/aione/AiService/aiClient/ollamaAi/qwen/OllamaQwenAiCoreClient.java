package org.myfx.controls.aione.AiService.aiClient.ollamaAi.qwen;

import jakarta.annotation.Resource;
import org.myfx.controls.aione.AiService.aiClient.AMTest.AiModelClient;
import org.myfx.controls.aione.AiService.aiClient.advisor.CustomMemoryAdvisor;
import org.myfx.controls.aione.AiService.aiClient.advisor.MySimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * 千问(Qwen)模型核心客户端
 * 职责：封装千问模型的基础调用逻辑（阻塞式+流式），强制关联chatId，与业务逻辑解耦
 */
@Component
public class OllamaQwenAiCoreClient { // 类名突出Qwen（千问模型）

    @Resource(name = "qwenClient") // 关联千问模型的ChatClient
    private AiModelClient qwenChatClient;

    @Resource
    private ChatMemory chatMemory;

    // ====================== 核心方法1：千问模型阻塞式对话（强制chatId） ======================
    /**
     * 千问模型基础阻塞式对话（必须传入chatId，关联会话记忆）
     * @param msg 用户输入消息
     * @param systemPrompt 系统提示词（定义AI角色/风格，不能为空）
     * @param chatId 会话唯一标识（关联记忆/追踪会话，不能为空）
     * @return AI返回的完整内容
     * @throws IllegalArgumentException 当systemPrompt/chatId为空时抛出
     */
    public String qwenChatBlock(String msg, String systemPrompt, String chatId) {
        // 强制参数校验：chatId+systemPrompt都不能为空
        validateRequiredParams(systemPrompt, chatId);

        // 千问模型核心调用逻辑（关联chatId和记忆）
        // 保留日志Advisor（如有需要可删）
        // 关联会话记忆
        return qwenChatClient.getChatClient()
                .prompt()
                .system(systemPrompt)
                .user(msg)
                .advisors(new MySimpleLoggerAdvisor()) // 保留日志Advisor（如有需要可删）
                .advisors(new CustomMemoryAdvisor(chatMemory)) // 关联会话记忆
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .content();

        // 测试阶段可临时返回（注释上面真实逻辑，启用下面测试逻辑）
//        return String.format("[千问模型][chatId:%s] 模拟响应：%s", chatId, systemPrompt);
    }

    // ====================== 核心方法2：千问模型流式对话（强制chatId） ======================
    /**
     * 千问模型基础流式对话（必须传入chatId，关联会话记忆）
     * @param msg 用户输入消息
     * @param systemPrompt 系统提示词（定义AI角色/风格，不能为空）
     * @param chatId 会话唯一标识（关联记忆/追踪会话，不能为空）
     * @return 流式返回的AI内容片段（Flux<String>）
     */
    public Flux<String> qwenChatStream(String msg, String systemPrompt, String chatId) {
        // 强制参数校验：chatId+systemPrompt都不能为空
        validateRequiredParams(systemPrompt, chatId);

        // 千问模型流式调用逻辑（关联chatId和记忆）
//        return qwenChatClient
//                .prompt()
//                .system(systemPrompt)
//                .user(msg)
//                .advisors(new CustomMemoryAdvisor(chatMemory)) // 关联会话记忆
//                .stream() // 流式调用核心方法
//                .content(); // 返回Flux<String>，逐段输出AI响应

        // 测试阶段模拟流式返回（注释上面真实逻辑，启用下面测试逻辑）
        String mockResponse = String.format("[千问模型][chatId:%s] %s", chatId, systemPrompt);
        return Flux.fromArray(mockResponse.split("")) // 拆分为单个字符
                .delayElements(Duration.ofMillis(100)); // 模拟100ms/字符的流式输出
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