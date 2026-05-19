package org.myfx.controls.aione.ServiceCommon.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiChatSessionService;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiConfigService;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.UserAiRoleBindService;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.token.AiUserPointBalanceService;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.token.upper.AiTokenTransactionService;
import org.myfx.controls.aione.AiService.service.base.status.ai_behavior_db.AiEmotionRealStateService;
import org.myfx.controls.aione.AiService.service.base.status.user_behavior_db.UserFeatureScoreService;
import org.myfx.controls.aione.ServiceCommon.event.eventDTO.UserCanceledEvent;
import org.myfx.controls.aione.ServiceCommon.event.eventDTO.UserCancellationCompletedEvent;
import org.myfx.controls.aione.ServiceCommon.event.eventDTO.UserRegisteredEvent;
import org.myfx.controls.aione.ServiceCommon.event.eventDTO.UserRegistrationCompletedEvent;
import org.myfx.controls.aione.ServiceCommon.event.publisher.UserEventPublisher;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.AppTypeEnum;
import org.myfx.controls.aione.UserService.model.entity.UserProfile;
import org.myfx.controls.aione.UserService.service.UserProfileService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 用户事件监听器
 * 监听用户注册、注销事件，处理AI会话初始化/删除 + 发布完成事件
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventListener {

    // 会话业务层
    private final AiChatSessionService aiChatSessionService;
    // 注入用户事件发布器（Component）
    private final UserEventPublisher userEventPublisher;

    private final UserFeatureScoreService userFeatureScoreService;
    private final AiEmotionRealStateService aiEmotionRealStateService;
    private final UserAiRoleBindService userAiRoleBindService;
    private final AiConfigService aiConfigService;
    private final AiTokenTransactionService aiTokenTransactionService;
    private final AiUserPointBalanceService aiUserPointBalanceService;

    private final UserProfileService userProfileService;

    // ====================== 1. 主事务提交前：创建会话（强一致，失败全回滚） ======================
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onUserRegisteredBeforeCommit(UserRegisteredEvent event) {
        Integer userId = event.getUserId();
        AppTypeEnum appType = event.getAppType();

        // 仅AI_CHAT创建会话（会话失败 → 主事务回滚，用户也不创建）
        if (AppTypeEnum.AI_CHAT == appType) {
            log.info("【主事务内】创建AI聊天会话，用户ID：{}", userId);
            aiChatSessionService.initChatSessionForUserId(userId);
        }
    }

    // ====================== 2. 主事务提交后：发布注册完成事件 ======================
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegisteredAfterCommit(UserRegisteredEvent event) {
        userEventPublisher.publishUserRegistrationCompletedEvent(
                event.getUserId(),
                event.getAppType()
        );
        log.info("主事务提交成功，发布注册完成事件，用户ID：{}", event.getUserId());
    }

    // ====================== ✅ 最终版：普通监听器 + 独立新事务（简化后） ======================
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onUserRegistrationCompleted(UserRegistrationCompletedEvent event) {
        Integer userId = event.getUserId();
        AppTypeEnum appType = event.getAppType();
        log.info("【独立子事务】开始初始化用户子业务，userId={}", userId);

        // ================ 仅需一行调用：Service内部已做幂等校验 ================
        userProfileService.addUserProfile(userId, appType);

        // ================ 原有6个子业务初始化 ================
        userFeatureScoreService.initUserFeatureScore(userId);
        aiEmotionRealStateService.initAiEmotion(userId);
        userAiRoleBindService.initUserRoleBind(userId);
        aiConfigService.initConfig(userId);
        aiTokenTransactionService.initUserTokenBalance(userId);
        aiUserPointBalanceService.initAiUserPointBalance(userId);

        log.info("【独立子事务】初始化全部成功，userId={}", userId);
    }

    // ====================== 1. 主事务提交前：删除会话（强一致，失败全回滚） ======================
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onUserCanceledBeforeCommit(UserCanceledEvent event) {
        Integer userId = event.getUserId();
        log.info("【主事务内】删除用户所有AI聊天会话，用户ID：{}", userId);
        // 会话删除失败 → 主事务回滚（用户注销操作也会撤销）
        aiChatSessionService.logicDeleteAllForAccountCancel(userId);
    }

    // ====================== 2. 主事务提交后：发布注销完成事件 ======================
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserCanceledAfterCommit(UserCanceledEvent event) {
        Integer userId = event.getUserId();
        // 仅主事务完全成功后，才发布注销完成事件
        userEventPublisher.publishUserCancellationCompletedEvent(userId);
        log.info("主事务提交成功，发布注销完成事件，用户ID：{}", userId);
    }

    // ====================== 3. 监听【用户注销完成】事件（最终收尾，已修改） ======================
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onUserCancellationCompleted(UserCancellationCompletedEvent event) {
        Integer userId = event.getUserId();

        // 执行用户资料删除（独立事务，报错仅回滚当前清理操作）
        userProfileService.deleteByUserId(userId);
        log.info("【最终事件】用户资料已删除，用户ID：{}", userId);

        log.info("【最终事件】用户注销流程全部完成，用户ID：{}", userId);
    }
}