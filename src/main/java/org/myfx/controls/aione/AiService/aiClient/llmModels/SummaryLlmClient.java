package org.myfx.controls.aione.AiService.aiClient.llmModels;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.aiClient.AMTest.AiModelClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 总结小模型动态客户端（单体架构版）
 * 可直接@Autowired注入使用
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SummaryLlmClient {
    private final ApplicationContext applicationContext;

    // 总结小模型激活配置（从配置文件读取）
    @Value("${llm.summary.active-model}")
    private String summaryActiveModelName;

    // ===================== 原有方法：100%兼容旧代码，无侵入 =====================
    /**
     * 获取当前激活的总结小模型客户端（直接调用即可，无需额外get方法）
     */
    public ChatClient getClient() {
        return getAiModelClient().getChatClient();
    }

    // ===================== 新增方法：计费专用，获取typeId =====================
    /**
     * 获取【完整业务模型客户端】
     * 包含：ChatClient + modelName + inputTypeId + outputTypeId
     */
    public AiModelClient getAiModelClient() {
        try {
            AiModelClient modelClient = applicationContext.getBean(summaryActiveModelName, AiModelClient.class);
            log.info("【总结小模型】激活模型：{}，输入typeId：{}，输出typeId：{}",
                    modelClient.getModelName(),
                    modelClient.getInputTypeId(),
                    modelClient.getOutputTypeId());
            return modelClient;
        } catch (Exception e) {
            // 总结模型兜底：讯飞星火
            String fallbackModel = "sparkChatClient";
            log.error("【总结小模型】获取模型[{}]失败，使用兜底模型[{}]",
                    summaryActiveModelName, fallbackModel, e);
            return applicationContext.getBean(fallbackModel, AiModelClient.class);
        }
    }
}