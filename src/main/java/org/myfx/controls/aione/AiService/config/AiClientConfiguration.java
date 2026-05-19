package org.myfx.controls.aione.AiService.config;

import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.aiClient.AMTest.AiModelClient;
import org.myfx.controls.aione.AiService.aiClient.AiClientHelper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AI客户端核心配置类
 * 职责：注册ChatClient Bean，整合依赖，调用Helper完成底层构建
 */
@Configuration
@Slf4j
public class AiClientConfiguration {

    private final AiClientHelper aiClientHelper;

    // 注入工具类（Spring自动管理）
    public AiClientConfiguration(AiClientHelper aiClientHelper) {
        this.aiClientHelper = aiClientHelper;
    }

    /**
     * Qwen模型客户端（兜底模型）
     * 保留原Bean名称，硬编码绑定输入/输出Token类型ID
     */
    @Bean(name = "qwenClient")
    @Qualifier("qwenClient")
    public AiModelClient qwenChatClient(OllamaChatModel ollamaChatModel) {
        // 1. 构建原生ChatClient
        ChatClient chatClient = aiClientHelper.buildChatClient(ollamaChatModel, "qwen3:0.6b");
        // 2. 包装为AiModelClient，硬编码绑定typeId（临时）
        return new AiModelClient(
                chatClient,
                "qwen",          // 模型名称
                3L,              // 输入Token类型ID
                4L               // 输出Token类型ID
        );
    }

    /**
     * DeepSeek模型客户端（主模型）
     * 保留原Bean名称，硬编码绑定输入/输出Token类型ID
     */
    @Bean(name = "deepseekClient")
    @Qualifier("deepseekClient")
    public AiModelClient deepseekChatClient(OllamaChatModel ollamaChatModel) {
        // 1. 构建原生ChatClient
        ChatClient chatClient = aiClientHelper.buildChatClient(ollamaChatModel, "deepseek-v3.1:671b-cloud");
        // 2. 包装为AiModelClient，硬编码绑定typeId（临时）
        return new AiModelClient(
                chatClient,
                "deepseek",      // 模型名称
                1L,              // 输入Token类型ID
                2L               // 输出Token类型ID
        );
    }

    /**
     * Kimi模型客户端
     * 保留Bean名称规范，硬编码绑定输入/输出Token类型ID 11、12
     */
    @Bean(name = "kimiClient")
    @Qualifier("kimiClient")
    public AiModelClient kimiChatClient(OllamaChatModel ollamaChatModel) {
        // 1. 构建原生ChatClient，绑定Kimi模型名称
        ChatClient chatClient = aiClientHelper.buildChatClient(ollamaChatModel, "kimi-k2.5:cloud");
        // 2. 包装为AiModelClient，硬编码绑定typeId（按要求11、12）
        return new AiModelClient(
                chatClient,
                "Kimi",        // 模型名称
                3L,           // 输入Token类型ID（指定值）
                4L            // 输出Token类型ID（指定值）
        );
    }

    /**
     * 测试AI客户端（主AI）→ 统一返回 AiModelClient
     */
    @Bean(name = "testAiChatClient")
    @Qualifier("testAiChatClient")
    public AiModelClient mockChatClient() {
        AtomicInteger counter = new AtomicInteger(1);

        ChatModel mockChatModel = prompt -> {
            log.info("【主AI】拦截到的提示词: {}", prompt);
            String mockResponse = String.valueOf(counter.getAndIncrement());
            return new ChatResponse(List.of(new Generation(new AssistantMessage(mockResponse))));
        };

        ChatClient chatClient = ChatClient.builder(mockChatModel).build();
        // 测试模型：分配唯一 typeId（5、6）
        return new AiModelClient(chatClient, "test-model", 5L, 6L);
    }

    /**
     * 压力测试/流式测试客户端 → 统一返回 AiModelClient
     */
    @Bean(name = "streamTestChatClient")
    @Qualifier("streamTestChatClient")
    public AiModelClient streamTestChatClient() {
        ChatModel mockChatModel = new ChatModel() {
            private final String testText = "Flux是Reactor框架的核心组件，用于表示0到N个元素的异步流式序列，支持非阻塞式处理。它能逐元素发射数据、处理错误并完成流，还内置背压机制避免数据积压。在Spring WebFlux中，Flux常用来实现流式响应，比如SSE推送、批量数据异步返回，是响应式编程中处理多元素场景的首选。";

            @Override
            public ChatResponse call(Prompt prompt) {
                return new ChatResponse(List.of(new Generation(new AssistantMessage(testText))));
            }

            @Override
            public Flux<ChatResponse> stream(Prompt prompt) {
                // 修正点：将 fromIterable 改为 fromStream
                return Flux.fromStream(testText.chars().mapToObj(c -> String.valueOf((char) c)))
                        .delayElements(Duration.ofMillis(100))
                        .map(c -> new ChatResponse(List.of(new Generation(new AssistantMessage(c)))));
            }
        };

        ChatClient chatClient = ChatClient.builder(mockChatModel).build();
        // 流式测试模型：分配唯一 typeId（1、2）
        return new AiModelClient(chatClient, "stream-test-model", 1L, 2L);
    }

    // ====================== 功能模型（统一改造） ======================

    /**
     * FunctionGemma 模型 → 统一返回 AiModelClient
     */
    @Bean(name = "functionGemmaClient")
    @Qualifier("functionGemmaClient")
    public AiModelClient functionGemmaClient(OllamaChatModel ollamaChatModel) {
        ChatClient chatClient = aiClientHelper.buildChatClient(ollamaChatModel, "functiongemma");
        // 函数调用模型：分配唯一 typeId（9、10）
        return new AiModelClient(chatClient, "function-gemma", 9L, 10L);
    }

    /**
     * 讯飞星火（兜底模型）→ 统一返回 AiModelClient
     */
    @Bean(name = "sparkChatClient")
    @Qualifier("sparkChatClient")
    public AiModelClient sparkChatClient(OpenAiChatModel sparkModel) {
        ChatClient chatClient = aiClientHelper.buildChatClient(sparkModel, "Lite");
        // 兜底模型：分配唯一 typeId（11、12）
        return new AiModelClient(chatClient, "spark", 11L, 12L);
    }

    /**
     * 对外暴露记忆Bean（供其他组件复用）
     */
    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
        return aiClientHelper.initChatMemory(chatMemoryRepository, 5);
    }
}