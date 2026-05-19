package org.myfx.controls.aione.AiService.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.dto.AiChatDTO;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiChatSession;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiChatSessionService;
import org.myfx.controls.aione.AiService.service.base.status.StatusThresholdService;
import org.myfx.controls.aione.AiService.service.facade.ChatTaskService;
import org.myfx.controls.aione.AiService.utils.PromptTemplateReader;
import org.myfx.controls.aione.ServiceCommon.utils.SnowflakeGenerator;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * AI主动问候 无参GET接口控制器
 * 端口：8086
 */
@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor // 依赖注入（替换@Autowired，更优雅）
public class AiActiveGreetingController {

    // 注入原有业务服务（保持不变）
    private final StatusThresholdService statusThresholdService;
    private final AiChatSessionService aiChatSessionService;
    private final ChatTaskService chatTaskService;
    private final PromptTemplateReader promptTemplateReader;

    /**
     * AI主动问候 - 无参GET接口
     * 内部固定参数：当前地点=家，当前事件=睡觉，下一个地点=学校，下一个事件=上课
     */
    @GetMapping("/active/greeting")
    public String aiActiveGreeting() {
        // ===================== 固定内部赋值 =====================
        String locationDesc = "家";
        String eventDesc = "睡觉";
        String nextLocationDesc = "学校";
        String nextEventDesc = "上课";
        // ======================================================

        try {
            log.info("【AI主动问候-接口】开始执行，待触发用户筛选...");
            List<Integer> userIdList = statusThresholdService.listTriggerActiveMsgUserIds();
            if (userIdList == null || userIdList.isEmpty()) {
                String msg = "【AI主动问候-接口】无符合条件的用户，任务结束";
                log.info(msg);
                return msg;
            }

            List<AiChatSession> activeSessionList = aiChatSessionService.batchGetUserCurrentActiveSession(userIdList);
            if (activeSessionList == null || activeSessionList.isEmpty()) {
                String msg = "【AI主动问候-接口】用户无活跃会话，任务结束";
                log.info(msg);
                return msg;
            }

            Map<Integer, AiChatSession> userSessionMap = activeSessionList.stream()
                    .collect(Collectors.toMap(AiChatSession::getUserId, session -> session));

            // 生成模板提示词（原有逻辑不变）
            String userMessage = fillEventPromptTemplate(locationDesc, eventDesc, nextLocationDesc, nextEventDesc);

            for (Integer userId : userIdList) {
                try {
                    AiChatSession session = userSessionMap.get(userId);
                    if (session == null) {
                        log.warn("【AI主动问候-接口】用户{}无活跃会话，跳过", userId);
                        continue;
                    }

                    String sessionUuid = session.getSessionUuid();
                    Integer roleId = session.getRoleId() == null ? 1 : session.getRoleId();

                    AiChatDTO aiChatDTO = new AiChatDTO();
                    aiChatDTO.setUserId(userId);
                    aiChatDTO.setSessionUuid(sessionUuid);
                    aiChatDTO.setRoleId(roleId);
                    aiChatDTO.setMessage(userMessage);
                    aiChatDTO.setTaskId(UUID.randomUUID().toString());
                    aiChatDTO.setUserMessageId(SnowflakeGenerator.generateId());
                    aiChatDTO.setIsActiveMessage(Boolean.TRUE);
                    long currentTimestamp = System.currentTimeMillis();
                    aiChatDTO.setUserSendTimestamp(currentTimestamp);

                    chatTaskService.startAiStreamChatTaskForAiActive(aiChatDTO);
                    log.info("【AI主动问候-接口】用户{}主动消息任务已启动，会话UUID：{}", userId, sessionUuid);

                } catch (Exception e) {
                    log.error("【AI主动问候-接口】用户{}消息发送失败", userId, e);
                }
            }

            String result = "【AI主动问候-接口】全部任务执行完成";
            log.info(result);
            return result;

        } catch (Exception e) {
            String errorMsg = "❌ AI主动问候接口整体任务失败 | 当前地点: {}, 当前事件: {}, 下一个地点: {}, 下一个事件: {}";
            log.error(errorMsg, locationDesc, eventDesc, nextLocationDesc, nextEventDesc, e);
            return errorMsg.replace("{}", locationDesc)
                    .replace("{}", eventDesc)
                    .replace("{}", nextLocationDesc)
                    .replace("{}", nextEventDesc) + "，具体异常：" + e.getMessage();
        }
    }

    /**
     * 填充事件场景提示词模板（系统规范命名）
     * @param locationDescA  地点A（刚结束事件的地点）
     * @param eventDescA     事件A（刚结束的事件）
     * @param locationDescB  地点B（前往的目的地）
     * @param eventDescB     事件B（准备执行的事件）
     */
    private String fillEventPromptTemplate(String locationDescA, String eventDescA, String locationDescB, String eventDescB) {
        // 读取指定模板文件：EventPrompt.txt
        PromptTemplate promptTemplate = promptTemplateReader.readTemplateFile("EventPrompt.txt");
        // 填充4个核心参数（对应模板占位符）
        promptTemplate.add("LocationA", locationDescA);
        promptTemplate.add("EventA", eventDescA);
        promptTemplate.add("LocationB", locationDescB);
        promptTemplate.add("EventB", eventDescB);

        return promptTemplate.render();
    }
}