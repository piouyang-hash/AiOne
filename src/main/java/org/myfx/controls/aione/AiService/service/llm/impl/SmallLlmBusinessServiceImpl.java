package org.myfx.controls.aione.AiService.service.llm.impl;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.aiClient.llmModels.SummaryLlmClient;
import org.myfx.controls.aione.AiService.service.llm.SmallLlmBusinessService;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

/**
 * 小模型业务实现类（讯飞星火客户端）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmallLlmBusinessServiceImpl implements SmallLlmBusinessService {

    // 注入动态总结小模型客户端（关键）
    @Resource
    private SummaryLlmClient summaryLlmClient;

    // ========== 抽离：单文本总结系统提示词 ==========
    private String getTextSummarySystemPrompt() {
        return """
                你的核心任务是精准、简洁地总结用户提供的文本内容，严格遵守以下规则：
                1. 仅总结文本核心信息，不添加任何额外的解释、评论、建议或无关内容；
                2. 语言简洁明了，逻辑清晰，无冗余表述；
                3. 严格基于用户提供的原文，不编造、不扩展任何未提及的信息；
                4. 输出仅保留总结内容本身，无前缀、后缀、引号或多余符号；
                5. 若文本无有效信息，直接返回"无有效内容可总结"。
                """;
    }

    // ========== 抽离：消息列表总结系统提示词 ==========
    private String getMessageListSummarySystemPrompt() {
        return """
                你的核心任务是精准、简洁地总结用户提供的对话消息列表内容，严格遵守以下规则：
                1. 仅总结消息列表中的核心信息，不添加任何额外的解释、评论、建议或无关内容；
                2. 语言简洁明了，逻辑清晰，无冗余表述，突出对话核心意图/结论；
                3. 严格基于消息列表原文，不编造、不扩展任何未提及的信息；
                4. 输出仅保留总结内容本身，无前缀、后缀、引号或多余符号；
                5. 若消息列表无有效信息，直接返回"无有效内容可总结"。
                """;
    }

    // ========== 单文本总结（重构后，使用动态总结小模型） ==========
    @Override
    public String summarizeText(String msg) {
        // 1. 空值校验
        if (msg == null || msg.trim().isEmpty()) {
            log.warn("【小模型总结】待总结文本为空，返回提示文案");
            return "无有效内容可总结";
        }

        try {
            log.info("【小模型总结】开始处理文本，长度：{}字符", msg.length());

            // 2. 构建系统消息+用户消息
            Message systemMsg = new SystemMessage(getTextSummarySystemPrompt());
            Message userMsg = new UserMessage(msg);

            // 3. 拼接消息列表
            List<Message> messageList = new ArrayList<>();
            messageList.add(systemMsg);
            messageList.add(userMsg);

            // 4. 核心修改：使用动态总结小模型（不再硬编码讯飞星火）
            Prompt prompt = new Prompt(messageList);
            String summaryResult = summaryLlmClient.getClient() // 动态获取当前激活的总结小模型
                    .prompt(prompt)
                    .call()
                    .content();

            log.info("【小模型总结】文本处理完成，总结结果长度：{}字符", summaryResult.length());
            return summaryResult;
        } catch (Exception e) {
            // 日志修改：移除“讯飞星火”，改为通用的“总结小模型”
            log.error("【小模型总结】调用总结小模型处理文本异常", e);
            return "总结失败：" + e.getMessage();
        }
    }

    // ========== 消息列表总结（重构后，使用动态总结小模型） ==========
    @Override
    public String summarizeMessages(List<Message> messages) {
        // 1. 空值校验
        if (messages == null || messages.isEmpty()) {
            log.warn("【小模型总结】待总结的Message列表为空，返回提示文案");
            return "无有效内容可总结";
        }

        try {
            log.info("【小模型总结】开始处理Message列表，消息数量：{}条", messages.size());

            // 2. 构建系统消息 + 拼接历史消息
            Message systemMsg = new SystemMessage(getMessageListSummarySystemPrompt());
            List<Message> messageList = new ArrayList<>();
            messageList.add(systemMsg);
            messageList.addAll(messages);

            // 3. 核心修改：使用动态总结小模型
            String summaryResult = summaryLlmClient.getClient() // 动态获取当前激活的总结小模型
                    .prompt(new Prompt(messageList))
                    .call()
                    .content();

            log.info("【小模型总结】Message列表处理完成，总结结果长度：{}字符", summaryResult.length());
            return summaryResult;
        } catch (Exception e) {
            // 日志修改：移除“讯飞星火”，改为通用的“总结小模型”
            log.error("【小模型总结】调用总结小模型处理Message列表异常", e);
            return "总结失败：" + e.getMessage();
        }
    }
}