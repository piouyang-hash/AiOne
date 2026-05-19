package org.myfx.controls.aione.AiService.service.facade.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.aiClient.AiCoreClient;
import org.myfx.controls.aione.AiService.aiClient.openAi.spark.SparkChatBizService;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiChatSessionService;
import org.myfx.controls.aione.AiService.service.base.status.user_behavior_db.upper.UserBehaviorImpactScoreService;
import org.myfx.controls.aione.AiService.service.facade.AiService;
import org.myfx.controls.aione.AiService.service.upper.ChatMessageStoreService;
import org.myfx.controls.aione.AiService.service.upper.UpperChatPreCheckService;
import org.myfx.controls.aione.AiService.service.upper.UpperPromptBuildingService;
import org.myfx.controls.aione.ServiceCommon.context.UserContext;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * AI业务服务实现类
 * 职责：处理AI相关的业务逻辑，调用基础核心层能力，可注入其他Service扩展业务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceImpl implements AiService {

    private final AiCoreClient aiCoreClient;
    // 示例：可注入其他业务Service（符合"单方面注入其他service"的要求）
    private final UpperPromptBuildingService upperPromptBuildingService;
    private final AiChatSessionService aiChatSessionService;
    private final UpperChatPreCheckService upperChatPreCheckService;
    private final ChatMessageStoreService chatMessageStoreService;
    private final UserBehaviorImpactScoreService userBehaviorImpactScoreService;
    private final SparkChatBizService sparkChatBizService;

    // 公共AI对话：调用时传入公共场景的系统提示词
    @Override
    public String handlePublicAiChat(String msg) {
        // 公共场景的提示词由业务层传入（不再由基础方法硬编码）
        String publicSystemPrompt = "你是一个公共智能AI，回复需中立、通用，适合所有用户，语言简洁易懂。";
        return aiCoreClient.basicPublicChatBlock(msg, publicSystemPrompt);
    }

    /**
     * 触发系统发起的AI主动聊天（系统触发主动聊天机制专用）
     * @param userId 目标用户ID（AI主动聊天的接收用户，不能为空）
     * @param sessionId 会话ID（关联用户记忆/聊天链路，必须大于0）
     * @param triggerContext 系统触发上下文（如主动聊天的触发原因/场景，不能为空）
     * @return AI主动生成的聊天回复内容
     */
    @Override
    public String triggerSystemInitiatedAiChat(Integer userId, long sessionId, String triggerContext) {
        // 1. 严格参数校验（逻辑不变，保留核心校验）
        Assert.notNull(userId, "系统触发AI主动聊天时，目标用户ID（userId）不能为空");
        Assert.isTrue(sessionId > 0, "系统触发AI主动聊天时，会话ID（sessionId）必须大于0");
        Assert.hasText(triggerContext, "系统触发AI主动聊天时，触发上下文（triggerContext）不能为空");

        // 2. 检查会话是否激活（保留核心逻辑，仅保留错误日志）
        boolean isSessionActive = aiChatSessionService.checkSessionIsActive(userId, sessionId);
        if (!isSessionActive) {
            // 调用会话激活方法
            boolean activateSuccess = aiChatSessionService.activateSessionByUserAndSession(userId, sessionId);
            if (!activateSuccess) {
                String errorMsg = String.format("[系统主动聊天] 会话%s（用户%s）激活失败，无法发起主动聊天", sessionId, userId);
                log.error(errorMsg); // 仅保留错误日志
                throw new RuntimeException(errorMsg);
            }
        }

        // 3. 调用上层提示词构建服务，生成完整提示词（替换原4步手动拼接逻辑）
        String fullPrompt = upperPromptBuildingService.buildFullSystemPrompt(userId, sessionId);

        // 4. 调用AI核心客户端（仅保留AI回复结果日志）
        String aiActiveResponse = aiCoreClient.basicUserChatBlock(triggerContext, fullPrompt);
        log.info("[系统主动聊天] 用户{}会话{} - AI主动回复结果：{}", userId, sessionId, aiActiveResponse); // 仅保留结果日志

        return aiActiveResponse;
    }

    @Override
    public String respondToUserPureChatWithMemory(String msg, Long sessionId) {
        // 1. 获取当前登录用户ID（保留上下文获取逻辑）
        Integer userId = UserContext.getUserId();

        // 2. 调用SparkChatBizService做文本总结（内部已自动500字截断）
        String summaryResult = sparkChatBizService.summarizeTextReturnString(msg);
        // 打印总结结果（日志+控制台双打印，测试阶段更直观）
        log.info("用户[{}]会话[{}]消息总结结果：{}", userId, sessionId, summaryResult);

        // 3. 调用上层对话前置检验服务（传入userId）
        Long validSessionId = upperChatPreCheckService.chatPreCheck(userId, sessionId.toString(), 1);

        // 4. 调用上层提示词构建服务，生成完整提示词（传入userId）
        String fullPrompt = upperPromptBuildingService.buildFullSystemPrompt(userId, validSessionId);

        // 5. 调用AI核心客户端，获取AI回复（核心返回值）
        String aiResponse = aiCoreClient.basicUserChatBlock(msg, fullPrompt);

        // 6. 调用用户行为影响服务，处理聊天反馈（传入userId）
        userBehaviorImpactScoreService.handleChatBehavior(userId);

        // 7. 执行聊天后置处理逻辑（收回异步，直接同步调用，无线程等待）
        try {
            log.info("【同步后置处理】用户[{}]会话{}开始执行后置逻辑...", userId, validSessionId);
            // 调用上层后置处理服务，执行消息入库、更新会话最后消息等逻辑（传入userId）
            // Long parentId = upperChatPostProcessService.chatPostProcess(userId, validSessionId, msg, aiResponse, ChatRoleEnum.USER);
            //log.info("【同步后置处理】用户[{}]会话{}的聊天后置逻辑执行完成，关联parentId：{}", userId, validSessionId, parentId);
        } catch (Exception e) {
            // 后置处理异常捕获，不影响AI回复返回，仅打印错误日志
            log.error("【同步后置处理】用户[{}]会话{}的聊天后置逻辑执行失败", userId, validSessionId, e);
        }

        // 8. 返回AI回复
        return aiResponse;
    }

    @Override
    public String chatWithMainModel(String msg, Long sessionId) {
        return "";
    }

    @Override
    public String chatWithEDModel(String msg, Long sessionId) {
        return "";
    }

    @Override
    public String eventEndActiveMessage(Integer userId, String locationDesc, String eventDesc, Integer eventDuration) {
        return "";
    }


    /**
     * 私有工具方法：构建用户消息（UserMessage）
     * @param userMsg 用户输入的文本内容
     * @return Spring AI标准的UserMessage对象
     */
    private UserMessage buildUserMessage(String userMsg) {
        // 基础参数校验（避免空消息）
        Assert.hasText(userMsg, "用户消息内容不能为空");

        // 构建并返回UserMessage（如需自定义metadata可扩展，当前仅传文本）
        return new UserMessage(userMsg);
    }

    /**
     * 私有方法：根据用户ID和会话ID生成唯一的ChatID
     * @param userId 目标用户ID
     * @param sessionId 会话ID
     * @return 拼接后的唯一ChatID字符串（格式：chatId_用户ID_会话ID）
     */
    private String generateChatId(Integer userId, long sessionId) {
        // 核心逻辑：添加chatId前缀，格式为「chatId_用户ID_会话ID」，保证唯一性和可读性
        return String.format("chatId_%d_%d", userId, sessionId);
    }
}