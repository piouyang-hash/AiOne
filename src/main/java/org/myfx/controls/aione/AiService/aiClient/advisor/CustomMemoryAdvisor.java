package org.myfx.controls.aione.AiService.aiClient.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;
import java.util.Optional;

public class CustomMemoryAdvisor implements CallAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(CustomMemoryAdvisor.class);
    private final ChatMemory chatMemory;

    public CustomMemoryAdvisor(ChatMemory chatMemory) {
        this.chatMemory = chatMemory;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        // ========== 步骤1：获取conversationId（从Advisor参数中取） ==========
        // 直接获取，不使用getOrDefault，避免默认值
        String conversationId = (String) request.context().get(ChatMemory.CONVERSATION_ID);

        // 校验：conversationId为空则抛出非法参数异常
        if (conversationId == null || conversationId.isBlank()) {
            String errorMsg = "[CustomMemoryAdvisor] 错误：请求上下文中未找到必填的对话ID（" + ChatMemory.CONVERSATION_ID + "），禁止继续执行";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        // 校验通过后打印日志
        logger.info("[CustomMemoryAdvisor] 执行步骤1：获取到对话ID -> {}", conversationId);

        // ========== 步骤2：提取用户消息，直接添加到ChatMemory（移除防重复逻辑） ==========
        Prompt originalPrompt = request.prompt();
        List<UserMessage> userMessages = originalPrompt.getUserMessages();
        if (userMessages != null && !userMessages.isEmpty()) {
            UserMessage currentUserMsg = userMessages.get(0);
            // 直接添加用户消息到内存（不再检查重复）
            chatMemory.add(conversationId, currentUserMsg);
            logger.info("[CustomMemoryAdvisor] 执行步骤2：添加用户消息到内存 -> {}", currentUserMsg.getText());
        } else {
            logger.info("[CustomMemoryAdvisor] 执行步骤2：未获取到有效用户消息");
        }

        // ========== 步骤3：构建新Prompt（对标官方mutate写法，保留原请求所有属性） ==========
        // 从内存获取完整上下文
        List<Message> memoryMessages = chatMemory.get(conversationId);
        // 基于原Prompt构建新Prompt（保留系统消息等原有内容，仅替换messages）
        Prompt newPrompt = originalPrompt.mutate()
                .messages(memoryMessages)
                .build();
        // 核心修复：用mutate()修改请求，保留原请求的options/model/context等所有属性（对标Re2的mutate写法）
        ChatClientRequest newRequest = request.mutate()
                .prompt(newPrompt)
                .build();

        System.out.println(newPrompt);
        System.out.println(newRequest);
        logger.info("[CustomMemoryAdvisor] 执行步骤3：构建新Prompt，上下文消息数 -> {}", memoryMessages.size());

        // ========== 步骤4：调用AI接口（执行原chain逻辑） ==========
        logger.info("[CustomMemoryAdvisor] 执行步骤4：开始调用AI接口");
        ChatClientResponse response = chain.nextCall(newRequest);
        logger.info("[CustomMemoryAdvisor] 执行步骤4：AI接口调用完成");

        // ========== 步骤5：提取AI回复，压缩后添加到ChatMemory（修正消息类型） ==========
        Optional<Message> assistantMessage = Optional.ofNullable(response.chatResponse())
                .map(chatResp -> chatResp.getResult().getOutput());
        assistantMessage.ifPresent(message -> {
            Message compressedMessage = compressAssistantMessage(message);

            chatMemory.add(conversationId, compressedMessage);
            logger.info("[CustomMemoryAdvisor] 执行步骤5：添加压缩后的AI回复到内存 -> {}", compressedMessage.getText());
        });

        // ========== 步骤6：返回响应 ==========
        logger.info("[CustomMemoryAdvisor] 执行步骤6：完成所有逻辑，返回响应");
        return response;
    }

    /**
     * 修复：AI回复必须创建AssistantMessage，而非UserMessage
     */
    private Message compressAssistantMessage(Message originalMessage) {
        String originalContent = originalMessage.getText();
        // 简单压缩：超过100字符截断
        String compressedContent = originalContent.length() > 100
                ? originalContent.substring(0, 100) + "..."
                : originalContent;

        // 对标框架规范：AI回复用AssistantMessage
        return new AssistantMessage(compressedContent);
    }

    // ========== 必须实现的接口方法（对标官方Advisor规范） ==========
    @Override
    public String getName() {
        return this.getClass().getSimpleName(); // 非空名称，避免advisorName null错误
    }

    @Override
    public int getOrder() {
        return 1; // 可根据需要调整执行顺序（比如Re2设为0，这里设为1）
    }
}