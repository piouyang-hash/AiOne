package org.myfx.controls.aione.AiService.aiClient.advisor;

import lombok.RequiredArgsConstructor;
import org.myfx.controls.aione.AiService.service.upper.BaseMemoryService;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 结构化数据Advisor（StructuredDataAdvisor）
 * 核心能力：处理AI对话相关的结构化数据（如提取、解析、校验、存储结构化信息）
 * order（越小执行最早）：5
 */
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * 结构化数据Advisor
 * 核心能力：
 * 1. 同步场景：前置提取userId，填充用户信息/爱好到提示词模板
 * 2. 流式场景：复用前置模板填充逻辑，流式结束后无额外处理（后置逻辑为空）
 * order（越小执行最早）：5（在RagRetrievalAdvisor之后执行）
 */
@Component
@RequiredArgsConstructor
@Slf4j // 新增日志注解，便于调试
public class StructuredDataAdvisor implements BaseAdvisor {

    // 注入BaseMemoryService（构造器注入，符合Spring最佳实践）
    private final BaseMemoryService baseMemoryService;

    // --- 1. 同步逻辑：复用抽离的核心方法 ---
    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        return handleBeforeLogic(chatClientRequest);
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        handleAfterLogic(chatClientResponse.context());
        return chatClientResponse;
    }

    // --- 2. 流式逻辑：对齐改造范式，复用前置逻辑，后置无阻塞操作 ---
    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        // Before 逻辑：复用同步场景的模板填充逻辑
        ChatClientRequest augmentedRequest = handleBeforeLogic(request);

        return chain.nextStream(augmentedRequest)
                .doAfterTerminate(() -> {
                    // After 逻辑：异步执行（当前无业务逻辑，仅占位）
                    handleAfterLogic(request.context());
                    log.info("[StructuredDataAdvisor] 流式对话结束，无后置结构化数据处理");
                });
    }

    // --- 3. 抽离核心业务逻辑（复用） ---
    private ChatClientRequest handleBeforeLogic(ChatClientRequest request) {
        // 1. 从请求上下文提取userId
        var adviseContext = request.context();
        Integer userId = (Integer) adviseContext.get("userId");

        // 2. 判空保护：userId为空则抛出异常（保留原有Assert逻辑）
        Assert.notNull(userId, "Context中未找到userId，请检查参数传递逻辑");

        // 3. 从上下文提取三层记忆提示词模板
        PromptTemplate threeLayerMemoryPromptTemplate = (PromptTemplate) adviseContext.get("promptTemplate");

        // 4. 模板判空保护：模板不存在则直接返回原请求
        if (threeLayerMemoryPromptTemplate == null) {
            log.warn("[StructuredDataAdvisor] 上下文未找到promptTemplate，跳过用户信息填充");
            return request;
        }

        // 5. 调用BaseMemoryService获取用户信息和爱好
        String userProfile = baseMemoryService.getUserProfilePrompt(userId);
        String userHobby = baseMemoryService.getUserHobbyPrompt(userId);
        log.info("[StructuredDataAdvisor] 已获取用户{}的信息：profile={}, hobby={}", userId, userProfile, userHobby);

        // 6. 填充提示词模板变量（对应模板中的{userBasicInfo}和{userHobby}）
        threeLayerMemoryPromptTemplate.add("userBasicInfo", userProfile);
        threeLayerMemoryPromptTemplate.add("userHobby", userHobby);

        // 7. 返回处理后的请求（模板已填充变量）
        return request;
    }

    // 后置核心逻辑（当前无业务操作，仅占位便于后续扩展）
    private void handleAfterLogic(Map<String, Object> context) {
        // 预留扩展点：可在此实现AI响应的结构化解析、存储等逻辑
        log.debug("[StructuredDataAdvisor] 执行后置逻辑（当前无业务操作）");
    }

    /**
     * 自定义Advisor名称（英文，便于日志/调试识别）
     */
    @Override
    public String getName() {
        return "StructuredDataAdvisor";
    }

    /**
     * 执行顺序：设为5，在RagRetrievalAdvisor（order=5）之后执行（可根据业务调整）
     */
    @Override
    public int getOrder() {
        return 5;
    }
}