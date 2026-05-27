package org.myfx.controls.aione.AiService.aiClient;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Component;

/**
 * AI客户端通用工具类
 * 职责：封装底层构建、记忆初始化等通用逻辑，提供可复用方法
 */
@Component // 标记为Spring组件，支持依赖注入
public class AiClientHelper {

    /**
     * 通用ChatClient构建方法（底层核心封装）
     * @param chatModel 模型实例（OllamaChatModel/OpenAiChatModel）
     * @param modelName 模型名称（如qwen3:0.6b/functiongemma）
     * @return 构建好的ChatClient
     */
    public ChatClient buildChatClient(ChatModel chatModel, String modelName) {

        // 1. 用官方静态builder创建配置（你源码里的builder()方法）
        ChatOptions.Builder<?> optionsBuilder = ChatOptions.builder()
                .model(modelName);

        // 2. 直接传入builder，完美匹配defaultOptions参数
        return ChatClient.builder(chatModel)
                .defaultOptions(optionsBuilder)
                .build();
    }

    /**
     * 通用记忆初始化方法（底层封装）
     * @param chatMemoryRepository 持久化存储库
     * @param maxMessages 单会话最大消息数
     * @return 初始化好的ChatMemory
     */
    public ChatMemory initChatMemory(ChatMemoryRepository chatMemoryRepository, int maxMessages) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(maxMessages)
                .build();
    }
}