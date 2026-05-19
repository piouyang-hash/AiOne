package org.myfx.controls.aione.AiService.aiClient;

import jakarta.annotation.Resource;
import org.myfx.controls.aione.AiService.aiClient.AMTest.AiModelClient;
import org.myfx.controls.aione.AiService.aiClient.advisor.CustomMemoryAdvisor;
import org.myfx.controls.aione.AiService.aiClient.advisor.MySimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * AI基础核心客户端
 * 职责：封装ChatClient的基础调用逻辑，提供最底层的AI对话能力，与业务逻辑解耦
 */
@Component
public class AiCoreClient {

    @Resource(name = "qwenClient")
    private AiModelClient chatClient;

    @Resource
    private ChatMemory chatMemory;

    // ====================== 原阻塞式方法（保留，兼容原有调用） ======================
    /**
     * 基础AI对话方法（强制传入系统提示词，无兜底）
     * @param msg 用户输入的对话消息
     * @param systemPrompt 系统提示词（定义AI角色/说话风格，不能为空）
     * @return AI返回的内容
     * @throws IllegalArgumentException 当systemPrompt为null/空字符串时抛出
     */
    public String basicChatBlock(String msg, String systemPrompt) {
        if (systemPrompt == null || systemPrompt.isBlank()) {
            throw new IllegalArgumentException("参数不合法：系统提示词（systemPrompt）不能为空");
        }

        // 测试临时修改：阻塞式返回
//         return chatClient
//                 .prompt()
//                 .system(systemPrompt)
//                 .user(msg)
//                 .call()
//                 .content();

        return systemPrompt;
    }

    /**
     * 基础AI对话方法（带chatId，用于会话关联/追踪）
     * @param msg 用户输入的对话消息
     * @param systemPrompt 系统提示词（定义AI角色/说话风格，不能为空）
     * @param chatId 会话唯一标识（如chatId_1001_123456，不能为空）
     * @return AI返回的内容
     * @throws IllegalArgumentException 当systemPrompt/chatId为null/空字符串时抛出
     */
    public String basicChatBlock(String msg, String systemPrompt, String chatId) {
        // 1. 增强参数校验：新增chatId非空校验
        if (systemPrompt == null || systemPrompt.isBlank()) {
            throw new IllegalArgumentException("参数不合法：系统提示词（systemPrompt）不能为空");
        }
        if (chatId == null || chatId.isBlank()) {
            throw new IllegalArgumentException("参数不合法：会话标识（chatId）不能为空");
        }

        // 2. 核心对话逻辑（和原方法一致，测试阶段返回systemPrompt）
        // 真实场景可替换为：
         String aiResponse = chatClient.getChatClient()
                 .prompt()
                 .system(systemPrompt)
                 .user(msg)
                 .advisors(new MySimpleLoggerAdvisor())
                 .advisors(new CustomMemoryAdvisor(chatMemory))
                 .call()
                 .content();
         return aiResponse;

        // 测试临时返回：保留原逻辑，同时关联chatId（仅示意）
//        return String.format("[chatId:%s] %s", chatId, systemPrompt);
    }

    /**
     * 公共AI对话基础方法（阻塞式）
     */
    public String basicPublicChatBlock(String msg, String systemPrompt) {
        return basicChatBlock(msg, systemPrompt);
    }

    /**
     * 用户AI对话基础方法（阻塞式）
     */
    public String basicUserChatBlock(String msg, String systemPrompt) {
        return basicChatBlock(msg, systemPrompt);
    }

    // ====================== 新增流式输出核心方法 ======================
    /**
     * 基础AI对话流式方法（强制传入系统提示词，无兜底）
     * @param msg 用户输入的对话消息
     * @param systemPrompt 系统提示词（定义AI角色/说话风格，不能为空）
     * @return 流式返回的AI内容片段（Flux<String>）
     */
    public Flux<String> basicChatStream(String msg, String systemPrompt) {
        // 1. 响应式参数校验：不合法则返回错误信号
        if (systemPrompt == null || systemPrompt.isBlank()) {
            return Flux.error(new IllegalArgumentException("参数不合法：系统提示词（systemPrompt）不能为空"));
        }

        // ====================== 测试临时修改 ======================
        // 注释掉真实AI流式调用逻辑，测试时返回单片段的Flux
//         return chatClient
//                 .prompt()
//                 .system(systemPrompt)
//                 .user(msg)
//                 .stream() // 替换为流式调用方法（关键：阻塞call() → 流式stream()）
//                 .content();    // 直接返回 Flux<String>，每个元素就是 AI 蹦出的新字
        // ====================== 测试临时修改 ======================

        // 测试期间：流式返回系统提示词（模拟分段输出）
        // 模拟流式：将字符串拆成字符，并且每 100ms 发送一个
        return Flux.fromArray(systemPrompt.split("")) // 将 "你好" 拆成 ["你", "好"]
                .delayElements(Duration.ofMillis(100)); // 每个元素延迟 100 毫秒发送
    }

    // ====================== 新增公共场景流式方法 ======================
    /**
     * 公共AI对话流式方法（强制传入系统提示词，无兜底）
     * @param msg 用户输入的对话消息
     * @param systemPrompt 公共场景的系统提示词（不能为空）
     * @return 流式返回的AI内容片段
     */
    public Flux<String> basicPublicChatStream(String msg, String systemPrompt) {
        // 复用核心流式方法的校验和逻辑，无硬编码提示词
        return basicChatStream(msg, systemPrompt);
    }

    // ====================== 新增用户场景流式方法 ======================
    /**
     * 用户AI对话流式方法（强制传入系统提示词，无兜底）
     * @param msg 用户输入的对话消息
     * @param systemPrompt 用户场景的系统提示词（不能为空）
     * @return 流式返回的AI内容片段
     */
    public Flux<String> basicUserChatStream(String msg, String systemPrompt) {
        // 复用核心流式方法的校验和逻辑，无硬编码提示词
        return basicChatStream(msg, systemPrompt);
    }
}