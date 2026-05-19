package org.myfx.controls.aione.AiService.aiClient.advisor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.aiClient.advisor.dto.ChatInformationDTO;
import org.myfx.controls.aione.AiService.service.upper.UpperChatPreCheckService;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * 单活跃会话Advisor（Single Active Session Advisor）
 * 核心能力：管控用户的单活跃会话，确保同一用户仅保留一个有效会话上下文
 * order（越小执行最早）：0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SingleActiveSessionAdvisor implements BaseAdvisor {

    // 1. 注入上层对话前置检验服务（构造器注入）
    private final UpperChatPreCheckService upperChatPreCheckService;

    /**
     * 前置逻辑：
     * 1. 管控用户单活跃会话（核心原有能力）：确保同一用户仅保留一个有效会话上下文；
     * 2. UUID 转换（新增能力）：提取用户ID和会话UUID，将UUID传入前置校验方法，转换为有效的雪花SessionId；
     * 最终将DTO与上下文中的sessionUuid替换为转换后的有效SessionId，供后续Advisor使用。
     */
    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        // 合并：一行代码从上下文获取DTO
        ChatInformationDTO chatInfoDTO = ChatInformationDTO.getFromContext(chatClientRequest.context());

        // 从DTO中直接获取参数
        Integer userId = chatInfoDTO.getUserId();
        String originalSessionUuid = chatInfoDTO.getSessionUuid();
        // 新增：获取角色ID
        Integer roleId = chatInfoDTO.getRoleId();

        // 执行会话前置校验：传入UUID + roleId
        Long validSessionId = upperChatPreCheckService.chatPreCheck(userId, originalSessionUuid, roleId);

        // 🔥 修复：必须赋值回DTO（原有核心逻辑，不能丢！）
        chatInfoDTO.setSessionId(validSessionId);

        // 🔥 修复：保留日志，并补充打印roleId（更完整）
        log.info("【SingleActiveSessionAdvisor】UUID转SessionId完成 | 用户ID：{} | 原始会话UUID：{} | 角色ID：{} | 转换后有效SessionId：{} | 返回的ChatClientRequest完整信息：{}",
                userId, originalSessionUuid, roleId, validSessionId, chatClientRequest);

        return chatClientRequest;
    }

    /**
     * 后置逻辑：暂不实现
     */
    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        // 无具体业务逻辑，直接返回原响应
        return chatClientResponse;
    }

    /**
     * 流式场景：手动调用before逻辑，先进行单会话激活
     */
    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        // 核心：手动调用before方法，执行人设填充逻辑
        ChatClientRequest processedRequest = before(request, chain);

        // 继续流式调用链，返回处理后的请求
        return chain.nextStream(processedRequest);
    }

    /**
     * 自定义Advisor名称（英文，便于日志/调试识别）
     */
    @Override
    public String getName() {
        return "SingleActiveSessionAdvisor";
    }

    /**
     * 执行顺序：优先级最高（order=0）
     */
    @Override
    public int getOrder() {
        return 0;
    }
}