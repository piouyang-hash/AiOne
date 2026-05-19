package org.myfx.controls.aione.AiService.aiClient.business;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.aiClient.DeepSeekTokenizer;
import org.myfx.controls.aione.AiService.aiClient.advisor.dto.ChatInformationDTO;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.token.upper.AiTokenTransactionService;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * Token 计数顾问：流式从fullOriginalText统计输出Token，非流式从响应直接统计
 * order：50 → 提示词渲染后执行，切分前执行
 */
@Slf4j
@Component
// 构造器注入所有依赖
@RequiredArgsConstructor
public class TokenCountingAdvisor implements BaseAdvisor {

    private final DeepSeekTokenizer tokenizer;
    // 🔥 注入Token计费事务服务
    private final AiTokenTransactionService tokenTransactionService;

    /**
     * 前置逻辑：统计输入Token → 记录用户聊天消耗
     */
    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        ChatInformationDTO chatInfoDTO = ChatInformationDTO.getFromContext(chatClientRequest.context());
        int inputTokens = 0;

        if (chatInfoDTO != null) {
            String fullPromptText = chatInfoDTO.getFullPromptText();
            if (fullPromptText != null && !fullPromptText.isBlank()) {
                inputTokens = tokenizer.countTokens(fullPromptText);
                log.info("输入Token数：{}", inputTokens);
            } else {
                log.warn("输入提示词为空/不存在，输入Token数记为0");
            }
            // 赋值输入Token数
            chatInfoDTO.setInputTokens(inputTokens);

            // ===================== 🔥 核心：用户输入Token消耗 =====================
            Integer userId = chatInfoDTO.getUserId();
            Long inputTypeId = chatInfoDTO.getInputTypeId();
            if (userId != null && inputTypeId != null && inputTokens > 0) {
                boolean success = tokenTransactionService.consumeTokenByUserChat(userId, inputTypeId, (long) inputTokens);
                log.info("用户[{}]输入Token消耗完成，类型ID[{}]，数量[{}]，结果：{}",
                        userId, inputTypeId, inputTokens, success);
            }
        }
        return chatClientRequest;
    }

    /**
     * 非流式后置：统计输出Token → 记录AI回复消耗
     */
    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        ChatInformationDTO chatInfoDTO = ChatInformationDTO.getFromContext(response.context());
        int outputTokens = 0;

        if (response.chatResponse() != null && response.chatResponse().getResult() != null) {
            String outputContent = response.chatResponse().getResult().getOutput().getText();
            if (outputContent != null && !outputContent.isEmpty()) {
                outputTokens = tokenizer.countTokens(outputContent);
                log.info("非流式输出Token数：{}", outputTokens);
            } else {
                log.warn("非流式输出内容为空，输出Token数记为0");
            }
        } else {
            log.warn("非流式响应为空，输出Token数记为0");
        }

        // 赋值输出Token数
        if (chatInfoDTO != null) {
            chatInfoDTO.setOutputTokens(outputTokens);

            // ===================== 🔥 核心：AI输出Token消耗 =====================
            Integer userId = chatInfoDTO.getUserId();
            Long outputTypeId = chatInfoDTO.getOutputTypeId();
            if (userId != null && outputTypeId != null && outputTokens > 0) {
                boolean success = tokenTransactionService.consumeTokenByAiReply(userId, outputTypeId, (long) outputTokens);
                log.info("用户[{}]AI回复Token消耗完成，类型ID[{}]，数量[{}]，结果：{}",
                        userId, outputTypeId, outputTokens, success);
            }
        }
        return response;
    }

    /**
     * 流式处理
     */
    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        ChatClientRequest processedRequest = before(request, chain);
        return chain.nextStream(processedRequest)
                .doOnTerminate(() -> handleStreamTokenStats(processedRequest, "流终止"))
                .doOnCancel(() -> handleStreamTokenStats(processedRequest, "流取消"));
    }

    /**
     * 流式Token统计：【最终修复】从 DTO 取 aiReplyContent 计算
     */
    private void handleStreamTokenStats(ChatClientRequest request, String signalType) {
        ChatInformationDTO chatInfoDTO = ChatInformationDTO.getFromContext(request.context());
        int outputTokens = 0;

        if (chatInfoDTO != null) {
            // 【修改】使用AI最终回复内容统计Token
            String aiReplyContent = chatInfoDTO.getAiReplyContent();
            if (aiReplyContent != null && !aiReplyContent.isBlank()) {
                outputTokens = tokenizer.countTokens(aiReplyContent);
                log.info("流式（{}）输出Token数：{}", signalType, outputTokens);
            } else {
                log.warn("流式（{}）AI回复内容为空，输出Token数记为0", signalType);
            }
            // 赋值到 DTO
            chatInfoDTO.setOutputTokens(outputTokens);

            // ===================== 🔥 流式输出Token计费（新增核心逻辑） =====================
            Integer userId = chatInfoDTO.getUserId();
            Long outputTypeId = chatInfoDTO.getOutputTypeId();
            // 判空：用户ID、类型ID、Token数量都合法才计费
            if (userId != null && outputTypeId != null && outputTokens > 0) {
                boolean success = tokenTransactionService.consumeTokenByAiReply(userId, outputTypeId, (long) outputTokens);
                log.info("用户[{}]【流式】AI回复Token消耗完成，类型ID[{}]，数量[{}]，结果：{}",
                        userId, outputTypeId, outputTokens, success);
            }
        }
    }

    @Override
    public int getOrder() {
        return 40;
    }

}