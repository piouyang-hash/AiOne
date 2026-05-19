package org.myfx.controls.aione.AiService.aiClient.advisor;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.aiClient.advisor.dto.ChatInformationDTO;
import org.myfx.controls.aione.AiService.utils.PromptTemplateReader;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 提示词模板渲染Advisor（PromptTemplateRenderAdvisor）
 * 核心能力：填充用户当前问题并渲染提示词模板，打印最终渲染结果
 * order（越小执行最早）：30
 */
@Slf4j
@Component
public class PromptTemplateRenderAdvisor implements BaseAdvisor {

    // 🔥 注入你需要的模板读取器
    @Resource
    private PromptTemplateReader promptTemplateReader;

    // 🔥 初始化Mustache工厂（和你示例代码一致）
    private final MustacheFactory mustacheFactory = new DefaultMustacheFactory();

    /**
     * 前置逻辑：渲染系统提示词模板，拆分SystemMessage和UserMessage，替换AI请求的原始Prompt
     * 无需手动拆分模板内容，用户问题直接用原始msg，更简洁
     */
// ==================== 核心改造后的 before 方法 ====================
    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        // 一行获取DTO + 强制非空校验（不变）
        ChatInformationDTO chatInfoDTO = ChatInformationDTO.getFromContext(chatClientRequest.context());
        Assert.notNull(chatInfoDTO, "ChatInformationDTO 不能为空");

        // 🔥 核心改造1：调用DTO方法，自动填充所有模板参数（内部初始化Map+非空校验）
        chatInfoDTO.fillPromptTemplateParams();
        // 获取填充好的模板Map
        Map<String, Object> promptTemplateMap = chatInfoDTO.getPromptTemplate();

        // 统一判空保护（无参数则直接返回）
        if (promptTemplateMap == null) {
            return chatClientRequest;
        }

        // ==================================================================================
        // 核心渲染逻辑（完全不变）
        // 1. 读取原始模板文件
        String originalTemplate = promptTemplateReader.readTemplateFile();
        Assert.hasText(originalTemplate, "提示词原始模板不能为空");

        // 2. Mustache原生渲染
        Mustache mustache = mustacheFactory.compile(new StringReader(originalTemplate), "threeLayerMemoryPrompt");
        StringWriter writer = new StringWriter();
        mustache.execute(writer, promptTemplateMap);
        // 渲染后的最终系统提示词
        String systemPromptStr = writer.toString().trim();
        // ==================================================================================

        // 以下所有逻辑 === 按要求新增历史消息拼接 ===
        // 提取原Prompt的模型配置
        ChatOptions originalPromptOptions = chatClientRequest.prompt().getOptions();

        // 🔥 核心改造2：动态组装消息列表（系统提示词 + 历史消息 + 用户消息）
        List<Message> messageList = new ArrayList<>();
        // 1. 添加系统提示词
        messageList.add(new SystemMessage(systemPromptStr));
        // 2. 添加历史对话消息（非空才添加，空列表自动跳过）
        List<Message> historyMessages = chatInfoDTO.getHistoryMessagesFormatted();
        if (!CollectionUtils.isEmpty(historyMessages)) {
            messageList.addAll(historyMessages);
        }
        // 3. 添加用户当前消息
        messageList.add(new UserMessage(chatInfoDTO.getUserMessage()));

        // 构建最终Prompt
        Prompt basePrompt = new Prompt(messageList);
        Prompt finalPrompt = basePrompt.mutate().chatOptions(originalPromptOptions).build();

        // 完整提示词赋值给DTO（完全不变）
        String fullPromptText = finalPrompt.getContents();
        if (fullPromptText == null || fullPromptText.isBlank()) {
            log.warn("完整提示词文本为空，未赋值到DTO");
        } else {
            chatInfoDTO.setFullPromptText(fullPromptText);
            // 打印完整提示词
            log.error("==================== 完整提示词内容 START ====================");
            log.error(finalPrompt.toString());
            log.error("==================== 完整提示词内容 END ====================");
            log.debug("已将完整提示词文本赋值到DTO，长度：{}字符", fullPromptText.length());
        }

        // 返回新请求
        return chatClientRequest.mutate().prompt(finalPrompt).build();
    }

    /**
     * 后置逻辑：暂不实现
     */
    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }

    /**
     * 自定义Advisor名称（与类名一致，便于日志/调试识别）
     */
    @Override
    public String getName() {
        return "PromptTemplateRenderAdvisor";
    }

    /**
     * 执行顺序：设为30，在前面的Advisor（如StructuredDataAdvisor order=4）之后执行
     */
    @Override
    public int getOrder() {
        return 30;
    }
}