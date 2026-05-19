package org.myfx.controls.aione.AiService.aiClient.advisor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.aiClient.advisor.dto.ChatInformationDTO;
import org.myfx.controls.aione.AiService.utils.EventToPromptMappingUtil;
import org.myfx.controls.aione.ServiceCommon.AppResponse;
import org.myfx.controls.aione.ServiceCommon.entity.feign.EventRecordResponseDTO;
import org.myfx.controls.aione.SimulationGame.service.upper.UpperSequenceService;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 获取当前执行的事件Advisor（中文名称）
 * 核心能力：调用事件驱动微服务Feign接口，获取当前执行事件并生成AI提示词，存入上下文
 * order（越小执行最早）：7
 */
@Component
@RequiredArgsConstructor // 构造器注入注解
@Slf4j // 日志注解（建议添加，便于排查问题）
public class CurrentExecutingEventAdvisor implements BaseAdvisor {

    // 1. 替换为注入事件驱动微服务的Feign客户端（构造器注入）
    private final UpperSequenceService upperSequenceService;

    // 2.
    private final EventToPromptMappingUtil eventToPromptMappingUtil;

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        // 标准写法：从上下文获取DTO
        ChatInformationDTO chatInfoDTO = ChatInformationDTO.getFromContext(chatClientRequest.context());

        // 【修改1】初始值为 null（无数据时传null）
        String eventPrompt = null;

        try {
            // 【核心修改】直接调用本地业务方法，替代远程Feign调用
            EventRecordResponseDTO eventDTO = upperSequenceService.getCurrentExecutingEvent();

            // 构建事件提示词
            eventPrompt = eventToPromptMappingUtil.buildEventPromptFromDTO(eventDTO);
            log.debug("【CurrentExecutingEventAdvisor】查询当前执行事件成功，已生成提示词");
        } catch (Exception e) {
            // 【修改2】捕获业务方法的断言/空指针异常，兼容原有熔断降级逻辑
            log.warn("【CurrentExecutingEventAdvisor】查询事件失败：{}", e.getMessage());
        }

        // 直接注入参数（null会自动传入）
        chatInfoDTO.setCurrentExecutingEvent(eventPrompt);
        log.debug("【CurrentExecutingEventAdvisor】已向提示词模板注入事件提示词");

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
     * 流式场景：手动调用before逻辑，先进行事件获取的feign调用
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
        return "CurrentExecutingEventAdvisor";
    }

    /**
     * 切面执行顺序：设为7（按你的要求）
     */
    @Override
    public int getOrder() {
        return 7;
    }
}