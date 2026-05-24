package org.myfx.controls.aione.AiService.aiClient.advisor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.aiClient.advisor.dto.ChatInformationDTO;
import org.myfx.controls.aione.AiService.service.upper.ChatMessageStoreService;
import org.myfx.controls.aione.AiService.utils.SplitContentUtils;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.Objects;

/**
 * 对话存储Advisor（简化版：仅打印用户消息和AI回复）
 * 核心：before存用户问题到context → after取并打印
 * order（越小执行最早）：40
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConversationStoreAdvisor implements BaseAdvisor {

    private final ChatMessageStoreService chatMessageStoreService;

    /**
     * 前置逻辑：提取用户问题 → 存入DTO → 执行前置存储
     */
    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        // 1. 获取核心DTO（强非空校验）
        ChatInformationDTO chatInfoDTO = ChatInformationDTO.getFromContext(chatClientRequest.context());
        Assert.notNull(chatInfoDTO, "ChatInformationDTO 不能为空");

        // 2. 执行【前置存储】：保存用户消息 + 创建AI流式占位符
        chatMessageStoreService.saveUserMessageAndCreateAiPlaceholder(chatInfoDTO);

        return chatClientRequest;
    }

    /**
     * 同步模式后置：更新AI最终消息
     */
    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        // 1. 获取AI完整回复
        String aiReply = "【AI未返回有效回复】";
        if (chatClientResponse.chatResponse() != null && !CollectionUtils.isEmpty(chatClientResponse.chatResponse().getResults())) {
            aiReply = chatClientResponse.chatResponse().getResults().get(0).getOutput().getText();
        }

        // 2. 执行统一存库逻辑
        executeSave(chatClientResponse.context(), aiReply);
        return chatClientResponse;
    }

    /**
     * 流式模式：流终止/取消后执行存库
     */
    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        ChatClientRequest processedRequest = before(request, chain);
        StringBuilder fallbackBuffer = new StringBuilder();

        // 获取DTO
        ChatInformationDTO chatInfoDTO = ChatInformationDTO.getFromContext(processedRequest.context());
        Assert.notNull(chatInfoDTO, "ChatInformationDTO 不能为空");

        return chain.nextStream(processedRequest)
                .doOnNext(response -> {
                    // 收到第一个字符 → 记录开始时间戳
                    if (chatInfoDTO.getAiReplyStartTimestamp() == null) {
                        long startTime = System.currentTimeMillis();
                        chatInfoDTO.setAiReplyStartTimestamp(startTime);
                        log.info("【流式模式】AI开始回复时间戳已记录：{}", startTime);
                    }

                    String text = response.chatResponse().getResults().getFirst().getOutput().getText();
                    if (text != null) {
                        fallbackBuffer.append(text);
                    }
                })
                .doOnTerminate(() -> handleFinalLogic(processedRequest, fallbackBuffer))
                .doOnCancel(() -> handleFinalLogic(processedRequest, fallbackBuffer));
    }

    /**
     * 流式统一收尾（空指针安全版）
     */
    private void handleFinalLogic(ChatClientRequest request, StringBuilder fallbackBuffer) {
        // 从DTO获取完整原文（优先）
        ChatInformationDTO chatInfoDTO = ChatInformationDTO.getFromContext(request.context());

        String dtoAiReply = "";
        if (chatInfoDTO != null) {
            dtoAiReply = Objects.requireNonNullElse(chatInfoDTO.getAiReplyContent(), "");
        }

        // ==================== 【核心修复1】空安全赋值：null 自动转为空字符串 "" ====================
        String finalAiReply = "";
        if (chatInfoDTO != null) {
            finalAiReply = dtoAiReply;
       } else {
            finalAiReply = fallbackBuffer.toString();
        }

        // ==================== 【核心修复2】安全判断：只有非空内容才执行保存 ====================
        if (StringUtils.hasText(finalAiReply)) {
            executeSave(request.context(), finalAiReply);
        } else {
            // 【友好日志】适配你的场景：用户快速发消息，AI未输出任何内容就被取消
            log.warn("【ConversationStoreAdvisor】AI未输出任何回复，取消保存（触发场景：新消息打断/取消任务）");
        }
    }

    /**
     * 核心存库：后置更新AI消息（全DTO模式）
     */
    private void executeSave(Map<String, Object> context, String aiReply) {
        // 1. 获取DTO
        ChatInformationDTO chatInfoDTO = ChatInformationDTO.getFromContext(context);
        Assert.notNull(chatInfoDTO, "ChatInformationDTO 不能为空，存库中止");

        try {
            // 2. 赋值AI回复相关字段
            chatInfoDTO.setAiReplyContent(aiReply);

            // ========== 新增调试打印：排查最后一段为空问题 ==========
            String splitContentJson = chatInfoDTO.getSplitContentJson();
            String lastSegment = SplitContentUtils.getLastSegment(splitContentJson);

            chatInfoDTO.setLastSegment(lastSegment);

            // 3. 执行【后置存储】：更新AI消息 + 更新会话
            chatMessageStoreService.updateAiMessageAndSession(chatInfoDTO);

            log.info("【ConversationStoreAdvisor】后置存储成功 | AI回复长度: {} | 最后一段内容: {}",
                    aiReply.length(), lastSegment);
        } catch (Exception e) {
            log.error("【ConversationStoreAdvisor】后置存储失败", e);
        }
    }

    @Override
    public String getName() { return "ConversationStoreAdvisor"; }

    @Override
    public int getOrder() { return 50; }

}