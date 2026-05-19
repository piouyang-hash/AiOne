package org.myfx.controls.aione.AiService.service.schedule;

/**
 * AI主动消息聚合调度服务接口（核心：供定时任务调度，执行AI主动发送消息的完整流程）
 */
public interface AiActiveChatAggregateService {

    /**
     * 执行AI主动消息调度（供任务调度调用，触发AI向指定用户主动发消息）
     * @param userId 目标用户ID（必填，AI主动消息的接收用户）
     * @return AI主动生成的回复内容（已存入数据库）
     * @throws IllegalArgumentException 用户ID为空时抛出
     * @throws RuntimeException 会话创建/消息触发失败时抛出
     */
    String executeAiActiveMessageDispatch(Integer userId);
}