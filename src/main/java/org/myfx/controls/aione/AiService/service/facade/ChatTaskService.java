package org.myfx.controls.aione.AiService.service.facade;

import org.myfx.controls.aione.AiService.dto.AiChatDTO;
import reactor.core.publisher.Mono;

/**
 * AI聊天任务服务接口
 */
public interface ChatTaskService {

    /**
     * 新增方法：添加AI聊天队列任务（存入Redis）
     * @param aiChatDTO 聊天请求参数
     */
    void addAiChatTaskToQueue(AiChatDTO aiChatDTO);

    /**
     * 开启AI流式输出对话任务，并将流式结果分片存储到Redis
     * @param aiChatDTO AI聊天请求参数
     */
    Mono<Void> startAiStreamChatTask(AiChatDTO aiChatDTO);

    /**
     * 停止AI流式对话（核心：取消订阅 + 清理Redis资源）
     * @param sessionUuid 会话唯一标识
     * @param taskId 流式任务ID
     */
    void stopAiStreamChatTask(String sessionUuid, String taskId);

    // ====================== 【新增】AI主动消息专用流式任务 ======================
    /**
     * 【AI主动消息专用】开启流式输出对话任务
     * 无UserContext上下文依赖，调用主动消息专用聊天接口
     * @param aiChatDTO AI聊天请求参数（必须预填充userId）
     */
    void startAiStreamChatTaskForAiActive(AiChatDTO aiChatDTO);

}
