package org.myfx.controls.aione.AiService.service.facade;

import org.myfx.controls.aione.AiService.dto.AiChatDTO;
import org.myfx.controls.aione.AiService.dto.ChatChunkDTO;
import reactor.core.publisher.Flux;

/**
 * 流式聊天服务核心接口（Flux Chat Service）
 * 包含多种类型的AI流式聊天交互方法，底层统一调用API完成流式交互
 * 所有方法返回Flux<String>，每一个元素代表AI返回的一段流式文本
 */
public interface FluxChatService {

    /**
     * 【新增】主动向前端发送流式对话的取消/结束帧
     * 用于在外部触发取消时，告知前端流已中断
     * @param uniqueKey 会话唯一标识 (格式: sessionUuid:taskId)
     * @param userId 用户ID（用于获取WebSocket通道推送消息）
     */
    void sendCancellationFrame(String uniqueKey, Integer userId);

    /**
     * 核心接口流式聊天（Main Interface Flux Chat）
     * @param msg 用户输入的对话消息
     * @param sessionUuid 对话会话UUID（用于日志追踪/会话上下文关联）
     * @return AI返回的流式内容（Flux<String>，逐段返回）
     */
    Flux<String> mainInterfaceChat(String msg, String sessionUuid);

    /**
     * 滑动窗口流式聊天（Sliding Window Flux Chat）
     * @param msg 用户输入的对话消息
     * @param sessionUuid 对话会话UUID（用于日志追踪/会话上下文关联）
     * @return AI返回的流式内容（Flux<String>，逐段返回）
     */
    Flux<String> slidingWindowChat(String msg, String sessionUuid);

    /**
     * 【Netty新版】AI流式输出 + Redis缓存 + 基于userId主动推送WebSocket消息
     */
    Flux<String> newStreamChatWithStorageAndPush(AiChatDTO aiChatDTO);

    /**
     * 总结型滑动窗口流式聊天（Summary Sliding Window Flux Chat）
     * @param aiChatDTO 对话请求DTO（包含消息、会话UUID、角色ID）
     * @return AI返回的流式内容
     */
    Flux<ChatChunkDTO> summarySlidingWindowChat(AiChatDTO aiChatDTO);

    /**
     * 总结型滑动窗口流式聊天【AI主动消息专用】
     * 无UserContext上下文依赖，直接使用DTO中**已预填充**的userId
     * 适用于系统/AI主动触发消息的场景（非用户前端请求）
     * @param aiChatDTO 对话请求DTO（必须提前填充userId/会话UUID/消息/角色ID）
     * @return AI返回的流式内容
     */
    Flux<ChatChunkDTO> summarySlidingWindowChatForAiActive(AiChatDTO aiChatDTO);

    /**
     * 近追加式流式聊天（Append Only Flux Chat）
     * @param msg 用户输入的对话消息
     * @param sessionUuid 对话会话UUID（用于日志追踪/会话上下文关联）
     * @return AI返回的流式内容（Flux<String>，逐段返回）
     */
    Flux<String> appendOnlyChat(String msg, String sessionUuid);

    /**
     * 事件驱动型核心接口流式聊天（Event Driven Main Interface Flux Chat）
     * @param msg 用户输入的对话消息
     * @param sessionUuid 对话会话UUID（用于日志追踪/会话上下文关联）
     * @return AI返回的流式内容（Flux<String>，逐段返回）
     */
    Flux<String> eventDrivenMainInterfaceChat(String msg, String sessionUuid);

    // ==================== 调用API的核心方法（唯一有重载的接口） ====================
    /**
     * 流式调用AI API（带会话UUID，用于上下文/日志追踪）
     * @param msg 用户输入的对话消息
     * @param sessionUuid 对话会话UUID
     * @return AI返回的流式内容（Flux<String>，逐段返回）
     */
    Flux<String> callApi(String msg, String sessionUuid);

    /**
     * 流式调用AI API（重载：无会话UUID，适用于无需上下文追踪的场景）
     * @param msg 用户输入的对话消息
     * @return AI返回的流式内容（Flux<String>，逐段返回）
     */
    Flux<String> callApi(String msg);
}