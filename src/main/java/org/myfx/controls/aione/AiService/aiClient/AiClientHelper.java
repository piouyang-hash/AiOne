package org.myfx.controls.aione.AiService.aiClient;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Component;

/**
 * AI客户端通用工具类
 * 职责：封装底层构建、记忆初始化等通用逻辑，提供可复用方法
 */
@Component // 标记为Spring组件，支持依赖注入
public class AiClientHelper {

    /**
     * 通用ChatClient构建方法（底层核心封装）
     * @param model 模型实例（OllamaChatModel/OpenAiChatModel）
     * @param modelName 模型名称（如qwen3:0.6b/functiongemma）
     * @return 构建好的ChatClient
     */
    public <T> ChatClient buildChatClient(T model, String modelName) {
        // 初始化构建器（兼容多模型类型）
        ChatClient.Builder builder = ChatClient.builder((ChatModel) model);

        // 通用配置：默认模型参数
        builder.defaultOptions(OpenAiChatOptions.builder().model(modelName));

        return builder.build();
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