package org.myfx.controls.aione.AiService.service.facade.impl;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.aiClient.advisor.*;
import org.myfx.controls.aione.AiService.aiClient.advisor.score.ChatScoreAdvisor;
import org.myfx.controls.aione.AiService.aiClient.llmModels.MainLlmClient;
import org.myfx.controls.aione.AiService.service.facade.SyncChatService;
import org.myfx.controls.aione.AiService.utils.PromptTemplateReader;
import org.myfx.controls.aione.ServiceCommon.context.UserContext;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

/**
 * 聊天服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SyncChatServiceImpl implements SyncChatService {

    // 所有的advisor
    private final ConversationStoreAdvisor conversationStoreAdvisor;

    private final SummarySlidingWindowAdvisor summarySlidingWindowAdvisor;

    private final SingleActiveSessionAdvisor singleActiveSessionAdvisor;

    private final ChatScoreAdvisor chatScoreAdvisor;

    private final StructuredDataAdvisor structureDataAdvisor;

    private final PersonaFillAdvisor personaFillAdvisor;

    private final PromptTemplateRenderAdvisor promptTemplateRenderAdvisor;

    private final AppendOnlyAdvisor appendOnlyAdvisor;

    private final GetLatestChatSessionAdvisor getLatestChatSessionAdvisor;

    private final GetConversationContextAdvisor getConversationContextAdvisor;

    private final CurrentExecutingEventAdvisor currentExecutingEventAdvisor;

    // 1. 注入模板读取工具类（已修改为返回PromptTemplate）
    private final PromptTemplateReader promptTemplateReader;

    // 2. 注入AI模型客户端（全部方法使用该模型）
    @Resource
    private MainLlmClient mainLlmClient;

    // ========== 以下是业务方法实现（按模板文件区分，逻辑完全复刻） ==========
    @Override
    public String mainInterfaceChat(String msg, Long sessionId) {
        // 核心：传入对应参数调用统一执行器
        return executeChat(
                msg,
                sessionId,
                "MI.txt", // 模板文件
                // 数组 → List 适配
                Arrays.asList(
                        singleActiveSessionAdvisor,
                        summarySlidingWindowAdvisor,
                        conversationStoreAdvisor,
                        chatScoreAdvisor,
                        structureDataAdvisor,
                        personaFillAdvisor,
                        promptTemplateRenderAdvisor
                ),
                "核心接口" // 日志标识
        );
    }

    @Override
    public String slidingWindowChat(String msg, Long sessionId) {
        // 核心：传入对应参数调用统一执行器
        return executeChat(
                msg,
                sessionId,
                "SW.txt", // 模板文件
                // 数组 → List 适配
                Arrays.asList(
                        singleActiveSessionAdvisor,
                        conversationStoreAdvisor,
                        chatScoreAdvisor,
                        promptTemplateRenderAdvisor
                ),
                "滑动窗口" // 日志标识
        );
    }

    @Override
    public String summarySlidingWindowChat(String msg, Long sessionId) {
        // 核心：传入对应参数调用统一执行器
        return executeChat(
                msg,
                sessionId,
                "SSW.txt", // 模板文件
                // 数组 → List 适配
                Arrays.asList(
                        singleActiveSessionAdvisor,
                        summarySlidingWindowAdvisor,
                        conversationStoreAdvisor,
                        chatScoreAdvisor,
                        promptTemplateRenderAdvisor
                ),
                "总结型滑动窗口" // 日志标识
        );
    }

    @Override
    public String appendOnlyChat(String msg, Long sessionId) {
        // 核心：传入专属参数调用统一执行器，替换指定Advisor
        return executeChat(
                msg,
                sessionId,
                "AO.txt", // 模板文件（与原方法一致）
                // 数组 → List 适配
                Arrays.asList(
                        singleActiveSessionAdvisor,
                        appendOnlyAdvisor, // 核心替换：总结型滑动窗口 → 近追加Advisor
                        conversationStoreAdvisor,
                        chatScoreAdvisor,
                        promptTemplateRenderAdvisor
                ),
                "近追加" // 日志标识（与原日志一致）
        );
    }

    @Override
    public String eventDrivenMainInterfaceChat(String msg, Long sessionId) {
        // 核心：传入专属参数调用统一执行器，无冗余逻辑
        return executeChat(
                msg,
                sessionId,
                "EDMI.txt", // 模板文件
                // 🔥 核心修改：数组 → List<Advisor>（使用 Arrays.asList 转换）
                Arrays.asList(
                        singleActiveSessionAdvisor,
                        summarySlidingWindowAdvisor,
                        conversationStoreAdvisor,
                        chatScoreAdvisor,
                        currentExecutingEventAdvisor,
                        promptTemplateRenderAdvisor
                ),
                "事件驱动型核心接口" // 日志标识（与原日志一致）
        );
    }

    /**
     * 统一的 AI 调用执行器（确定好的唯一方案）
     */
    private String executeChat(String msg, Long sessionId, String templateFile, List<Advisor> advisors, String logTag) {
        try {
            // 1. 公共逻辑：读取模板和获取上下文
            PromptTemplate promptTemplate = promptTemplateReader.readTemplateFile(templateFile);
            Integer userId = UserContext.getUserId();
            Long finalSessionId = (sessionId == null) ? -1L : sessionId;

            log.debug("开始调用AI模型[{}]，用户ID：{}，会话ID：{}，消息：{}", logTag, userId, finalSessionId, msg);

            // 2. 链式调用
            return mainLlmClient.getClient()
                    .prompt()
                    .user(msg)
                    .advisors(advisorSpec -> {
                        advisorSpec.advisors(advisors); // 直接传入List，无报错
                        advisorSpec.param("userId", userId);
                        advisorSpec.param("sessionId", finalSessionId);
                        advisorSpec.param("promptTemplate", promptTemplate);
                    })
                    .call()
                    .content();

        } catch (Exception e) {
            log.error("调用AI模型[{}]失败，会话ID：{}，消息：{}", logTag, sessionId, msg, e);
            throw new RuntimeException("AI聊天接口[" + logTag + "]调用失败", e);
        }
    }

    /**
     * 流式 AI 调用执行器（和同步版本逻辑完全对齐，仅返回流式响应）
     */
    private Flux<String> executeChatStream(String msg, Long sessionId, String templateFile, List<Advisor> advisors, String logTag) {
        try {
            // 1. 公共逻辑：读取模板和获取上下文（和同步版本完全一致）
            PromptTemplate promptTemplate = promptTemplateReader.readTemplateFile(templateFile);
            Integer userId = UserContext.getUserId();
            Long finalSessionId = (sessionId == null) ? -1L : sessionId;

            log.debug("开始流式调用AI模型[{}]，用户ID：{}，会话ID：{}，消息：{}", logTag, userId, finalSessionId, msg);

            // 2. 链式调用：替换同步call()为流式stream()
            return mainLlmClient.getClient()
                    .prompt()
                    .user(msg)
                    .advisors(advisorSpec -> {
                        advisorSpec.advisors(advisors); // 直接传入List，无报错
                        advisorSpec.param("userId", userId);
                        advisorSpec.param("sessionId", finalSessionId);
                        advisorSpec.param("promptTemplate", promptTemplate);
                    })
                    .stream() // 核心修改：同步call() → 流式stream()
                    .content() // 提取流式的文本内容，返回Flux<String>
                    .doOnError(e -> log.error("流式调用AI模型[{}]失败，会话ID：{}，消息：{}", logTag, sessionId, msg, e))
                    .onErrorMap(e -> new RuntimeException("AI流式聊天接口[" + logTag + "]调用失败", e));

        } catch (Exception e) {
            // 捕获初始化阶段的异常（如模板读取失败），转为Flux错误信号
            log.error("初始化流式AI调用[{}]失败，会话ID：{}，消息：{}", logTag, sessionId, msg, e);
            return Flux.error(new RuntimeException("AI流式聊天接口[" + logTag + "]初始化失败", e));
        }
    }

    // ========== 调用API方法（已改为正常返回） ==========
    @Override
    public String callApi(String msg, Long sessionId) {
        System.out.println("调用AI API（会话ID：" + sessionId + "），用户消息：" + msg);

        // 直接返回一段正常、通顺、约100字的回复
        return "你好呀！很高兴为你解答问题。生活中无论遇到学习上的困惑还是日常小烦恼，都可以慢慢说出来。保持积极的心态，一步一个脚印去解决问题，很多看似复杂的事情都会慢慢变清晰。愿你每天都有好心情，做事顺利，心中有光，眼里有希望，稳步朝着自己的目标前进。";
    }

    @Override
    public String callApi(String msg) {
        // 重载：无sessionId的API调用逻辑
        System.out.println("调用AI API（无会话ID）：msg=" + msg);
        return "AI响应（无会话ID）：" + msg; // 模拟返回
    }
}