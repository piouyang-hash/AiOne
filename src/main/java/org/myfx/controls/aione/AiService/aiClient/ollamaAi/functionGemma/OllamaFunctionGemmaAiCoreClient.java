package org.myfx.controls.aione.AiService.aiClient.ollamaAi.functionGemma;

import jakarta.annotation.Resource;
import org.myfx.controls.aione.AiService.Demo.FunctionCallDemo.DateTool;
import org.myfx.controls.aione.AiService.Demo.FunctionCallDemo.ThreadLocalTestHolder;
import org.myfx.controls.aione.AiService.aiClient.AMTest.AiModelClient;
import org.myfx.controls.aione.AiService.aiClient.advisor.ConversationStoreAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * FunctionGemma模型核心客户端
 * 职责：封装FunctionGemma模型的基础调用逻辑（阻塞式+流式），强制关联chatId，启用DateTool，不使用会话记忆
 */
@Component
public class OllamaFunctionGemmaAiCoreClient { // 类名突出FunctionGemma

    @Resource(name = "functionGemmaClient") // 关联FunctionGemma模型的ChatClient
    private AiModelClient functionGemmaChatClient;

    @Autowired
    private ConversationStoreAdvisor conversationStoreAdvisor;

    // ====================== 核心方法1：FunctionGemma模型阻塞式对话（强制chatId，启用DateTool，无记忆） ======================
    /**
     * FunctionGemma模型阻塞式对话（强制chatId，启用DateTool+MemoryTool，无记忆）
     * @param msg 用户输入消息
     * @param systemPrompt 系统提示词（不能为空）
     * @param chatId 会话唯一标识（不能为空）
     * @return AI返回的完整内容
     * @throws IllegalArgumentException 必传参数为空时抛出
     */
    public String functionGemmaChatBlock(String msg, String systemPrompt, String chatId) {
        validateRequiredParams(systemPrompt, chatId);

        // 1. 从ThreadLocal中获取用户ID（替换原threadLocalValue）
        Integer userId = ThreadLocalTestHolder.get();

        // 2. 构造ToolContext：存入userId（键名改为"userId"）
        Map<String, Object> contextMap = new HashMap<>();
        if (userId != null) {
            contextMap.put("userId", userId); // 键名改为userId，与业务语义匹配
        }

        // 3. 调用AI模型：传入ToolContext和记忆工具
        return functionGemmaChatClient.getChatClient()
                .prompt()

                .system(systemPrompt)
                .user(msg)
                .toolContext(contextMap) // 传入包含userId的上下文
//                .tools(new DateTool(), memoryTool) // 包含记忆工具
                .advisors(advisorSpec -> {                          // ← 改成 Consumer<AdvisorSpec>
                    // 这里可以注册多个 advisor，并为它们设置 per-call 参数
                    advisorSpec.advisors(conversationStoreAdvisor);  // 注册你的 advisor 实例

                    // 动态传参（关键！）
                    advisorSpec.param("userId", userId);                    // 自定义 key
                  })
        // 优先用标准 key（最推荐）
//        String userId = (String) adviseContext.get(ChatMemory.CONVERSATION_ID);
                .call()
                .content();
    }

    // ====================== 核心方法2：FunctionGemma模型流式对话（强制chatId，启用DateTool，无记忆） ======================
    /**
     * FunctionGemma模型基础流式对话（必须传入chatId，启用DateTool，无会话记忆）
     * @param msg 用户输入消息
     * @param systemPrompt 系统提示词（定义AI角色/风格，不能为空）
     * @param chatId 会话唯一标识（追踪会话，不能为空）
     * @return 流式返回的AI内容片段（Flux<String>，支持调用DateTool）
     */
    public Flux<String> functionGemmaChatStream(String msg, String systemPrompt, String chatId) {
        // 强制参数校验：systemPrompt+chatId不能为空（和Qwen客户端保持一致）
        validateRequiredParams(systemPrompt, chatId);

        // FunctionGemma流式调用逻辑：启用DateTool，无记忆，强制关联chatId
//        return functionGemmaChatClient
//                .prompt()
//                .system(systemPrompt)
//                .user(msg)
//                .defaultTools(new DateTool()) // 启用DateTool
//                .advisors(a -> a.param("chat_id", chatId)) // 传递chatId
//                .stream() // 流式调用核心方法
//                .content(); // 返回Flux<String>，逐段输出AI响应

        // 测试阶段模拟流式返回（注释上面真实逻辑，启用下面测试逻辑）
        String mockResponse = String.format("[FunctionGemma模型][chatId:%s] 模拟流式响应（已启用DateTool）：%s", chatId, systemPrompt);
        return Flux.fromArray(mockResponse.split("")) // 拆分为单个字符
                .delayElements(Duration.ofMillis(100)); // 模拟100ms/字符的流式输出
    }

    // ====================== 私有工具方法：统一参数校验（和Qwen客户端复用相同逻辑） ======================
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