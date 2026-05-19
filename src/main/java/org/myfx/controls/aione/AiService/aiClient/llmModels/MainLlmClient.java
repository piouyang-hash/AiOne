package org.myfx.controls.aione.AiService.aiClient.llmModels;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.aiClient.AMTest.AiModelClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 主要回复大模型动态客户端（单体架构版）
 * 可直接@Autowired注入使用
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Data
public class MainLlmClient {
    private final ApplicationContext applicationContext;

    // 主回复大模型激活配置（从配置文件读取）
    @Value("${llm.main.active-model}")
    private String mainActiveModelName;

    // ===================== 原有方法：完全兼容旧代码 =====================
    /**
     * 获取当前激活的主回复大模型客户端（直接调用即可，无需额外get方法）
     * 【兼容原有调用】只返回 ChatClient，不破坏现有逻辑
     */
    public ChatClient getClient() {
        return getAiModelClient().getChatClient();
    }

    // ===================== 新增方法：用于计费、获取typeId =====================
    /**
     * 获取【完整业务模型客户端】
     * 包含：ChatClient + modelName + inputTypeId + outputTypeId
     */
    public AiModelClient getAiModelClient() {
        try {
            AiModelClient modelClient = applicationContext.getBean(mainActiveModelName, AiModelClient.class);
            log.info("【主回复大模型】激活模型：{}，输入typeId：{}，输出typeId：{}",
                    modelClient.getModelName(),
                    modelClient.getInputTypeId(),
                    modelClient.getOutputTypeId());
            return modelClient;
        } catch (Exception e) {
            // 主模型兜底：通义千问
            String fallbackModel = "qwenClient";
            log.error("【主回复大模型】获取模型[{}]失败，使用兜底模型[{}]",
                    mainActiveModelName, fallbackModel, e);
            return applicationContext.getBean(fallbackModel, AiModelClient.class);
        }
    }
}