package org.myfx.controls.aione.AiService.aiClient.advisor;

import lombok.RequiredArgsConstructor;
import org.myfx.controls.aione.AiService.aiClient.advisor.dto.ChatInformationDTO;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiRole;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiRoleService;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 人设填充Advisor（Persona Fill Advisor）
 * 【关键说明】：
 * 1. 本Advisor的order必须大于RagRetrievalAdvisor（如RagRetrievalAdvisor=4），确保模板先加载其他变量再填充人设；
 * 2. 此处设置order=5，执行顺序：RagRetrievalAdvisor(4) → PersonaFillAdvisor(5)
 * order（越小执行最早）：5
 * 一定要小于提示词渲染切面
 */
@Component
@RequiredArgsConstructor
public class PersonaFillAdvisor implements BaseAdvisor {

    // 注入AI角色业务服务（构造器注入）
    private final AiRoleService aiRoleService;


    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        // 标准写法：从上下文获取DTO
        ChatInformationDTO chatInfoDTO = ChatInformationDTO.getFromContext(chatClientRequest.context());

        // 初始值直接为 null（无数据时传null）
        String personaContent = null;
        // 从DTO获取角色ID
        Integer roleId = chatInfoDTO.getRoleId();

        // 调用服务查询角色（无异常、不报错）
        AiRole aiRole = aiRoleService.getByRoleId(roleId);

        // 统一处理返回值
        if (aiRole != null && aiRole.getPersonaCore() != null) {
            personaContent = aiRole.getPersonaCore();
        }

        // 直接注入参数（null会自动传入）
        chatInfoDTO.setPersona(personaContent);

        return chatClientRequest;
    }

    /**
     * 后置逻辑：暂不实现
     */
    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }

    /**
     * 流式场景：手动调用before逻辑，确保人设填充执行
     */
    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        // 核心：手动调用before方法，执行人设填充逻辑
        ChatClientRequest processedRequest = before(request, chain);

        // 继续流式调用链，返回处理后的请求
        return chain.nextStream(processedRequest);
    }

    /**
     * 自定义Advisor名称（英文，便于日志/调试识别）
     */
    @Override
    public String getName() {
        return "PersonaFillAdvisor";
    }

    /**
     * 执行顺序：设为5，确保在RagRetrievalAdvisor(4)之后执行，模板已加载完成
     */
    @Override
    public int getOrder() {
        return 5;
    }
}