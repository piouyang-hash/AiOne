package org.myfx.controls.aione.AiService.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.service.upper.IAiChatInitiateService;
import org.myfx.controls.aione.ServiceCommon.entity.event.UserStatusChangeNotifyEvent;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.AppTypeEnum;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 用户状态变更事件监听器
 * 替代原Kafka消费者，处理用户上线/离线通知
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserStatusChangeListener {

    // 注入AI主动聊天触发服务（构造器注入，替代@Resource）
    private final IAiChatInitiateService aiChatInitiateService;

    /**
     * 监听用户状态变更事件（统一处理上线 + 离线）
     */
    @EventListener
    public void handleUserStatusChange(UserStatusChangeNotifyEvent event) {
        // 1. 事件空值直接返回
        if (event == null) {
            return;
        }

        // 2. 仅处理 AI_CHAT 应用类型
        AppTypeEnum appType = event.getAppType();
        if (appType != AppTypeEnum.AI_CHAT) {
            return;
        }

        // 3. 获取用户ID，为空则打印警告并返回
        Integer userId = event.getUserId();
        if (userId == null) {
            log.warn("【事件监听】AI_CHAT应用状态消息缺少用户ID");
            return;
        }

        // 4. 根据状态执行对应业务逻辑（上线/离线）
        String status = event.getStatus();
        String appTypeDesc = event.getAppTypeDesc() == null ? "AI_CHAT" : event.getAppTypeDesc().trim();
        Integer returnedUserId;

        if ("上线".equals(status)) {
            // 处理上线逻辑
            returnedUserId = aiChatInitiateService.handleUserOnlineClearActivity(userId);
            log.info("【事件监听】AI_CHAT应用用户{}上线，触发活跃度清除逻辑完成，返回用户ID：{}", userId, returnedUserId);
            log.info("【事件监听】收到用户{}在{}应用上线了！", userId, appTypeDesc);
        } else if ("离线".equals(status)) {
            // 处理离线逻辑
            returnedUserId = aiChatInitiateService.handleUserOfflineAndInitActivity(userId);
            log.info("【事件监听】AI_CHAT应用用户{}下线，触发活跃度递减逻辑完成，返回用户ID：{}", userId, returnedUserId);
            log.info("【事件监听】收到用户{}在{}应用下线了！", userId, appTypeDesc);
        }
    }
}
