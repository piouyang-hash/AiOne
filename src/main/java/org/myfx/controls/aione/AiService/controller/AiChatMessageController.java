package org.myfx.controls.aione.AiService.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.common.ai_chat_db.ChatRoleEnum;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiChatMessage;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiChatMessageService;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiChatSessionService;
import org.myfx.controls.aione.AiService.vo.AiChatMessageVO;
import org.myfx.controls.aione.ServiceCommon.AppResponse;
import org.myfx.controls.aione.ServiceCommon.SwaggerResponseConstants;
import org.myfx.controls.aione.ServiceCommon.annotation.AiAppCors;
import org.myfx.controls.aione.ServiceCommon.annotation.CheckJwt;
import org.myfx.controls.aione.ServiceCommon.annotation.CleanupThreadLocal;
import org.myfx.controls.aione.ServiceCommon.annotation.RateLimit;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AI 聊天消息控制器
 * 负责：聊天消息分页加载、历史消息拉取（替代全量查询，优化性能）
 */
@RestController
@RequestMapping("/ai/chat/message")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "AI对话消息接口", description = "AI对话的消息查询和分页查询")
@CleanupThreadLocal
@AiAppCors
public class AiChatMessageController {

    private final AiChatMessageService aiChatMessageService;

    private final AiChatSessionService aiChatSessionService;

    // ====================== 1. 首次加载：获取会话最新N条消息 ======================
    @Operation(
            summary = "查询会话最新消息（分页首屏）",
            description = """
                查询指定AI对话会话的最新N条消息（用于首次进入聊天页加载）：
                1. 登录态接口，需携带有效的JWT Token；
                2. 会话UUID通过路径参数传入；
                3. 默认返回20条，可通过limitNum自定义；
                4. 消息按对话正序返回，直接渲染；
                5. 过滤系统消息/临时消息，仅保留用户+AI消息。
                """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/latest/{sessionUuid}")
    @CheckJwt
    @RateLimit(seconds = 60, maxCount = 20)
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    public AppResponse<List<AiChatMessageVO>> listLatestChatMessages(
            @Parameter(description = "对话会话UUID", required = true)
            @PathVariable String sessionUuid,
            @Parameter(description = "每次加载数量（默认20）")
            @RequestParam(defaultValue = "20") Integer limitNum) {

        // 1. 通用转换：sessionUuid → sessionId
        AppResponse<Long> sessionIdResp = getSessionIdFromUuid(sessionUuid);
        if (!sessionIdResp.getCode().equals(200)) {
            return AppResponse.error(sessionIdResp.getCode(), sessionIdResp.getMessage(), null);
        }
        Long sessionId = sessionIdResp.getData();

        // 2. 调用Service
        List<AiChatMessage> messageList = aiChatMessageService.listLatestChatMessages(sessionId, limitNum);

        // 3. 调用公共方法转换VO
        List<AiChatMessageVO> voList = convertToVoList(messageList, sessionUuid);

        return AppResponse.success(voList, "最新消息查询成功");
    }

    // ====================== 2. 上拉加载：获取更早的历史消息（游标分页） ======================
    @Operation(
            summary = "上拉加载历史消息（分页）",
            description = """
                上拉加载更早的聊天历史（游标分页）：
                1. 登录态接口，需携带有效的JWT Token；
                2. 必须传入当前页面最小的消息ID（雪花ID）；
                3. 默认返回20条历史消息；
                4. 消息按正序返回，直接拼接到列表顶部。
                """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/history/{sessionUuid}")
    @CheckJwt
    @RateLimit(seconds = 60, maxCount = 20)
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    public AppResponse<List<AiChatMessageVO>> listMoreHistoryMessages(
            @Parameter(description = "对话会话UUID", required = true)
            @PathVariable String sessionUuid,
            @Parameter(description = "当前页面最小消息ID（雪花ID）", required = true)
            @RequestParam Long minMessageId,
            @Parameter(description = "每次加载数量（默认20）")
            @RequestParam(defaultValue = "20") Integer limitNum) {

        // 1. 通用转换：sessionUuid → sessionId
        AppResponse<Long> sessionIdResp = getSessionIdFromUuid(sessionUuid);
        if (!sessionIdResp.getCode().equals(200)) {
            return AppResponse.error(sessionIdResp.getCode(), sessionIdResp.getMessage(), null);
        }
        Long sessionId = sessionIdResp.getData();

        // 2. 调用Service
        List<AiChatMessage> messageList = aiChatMessageService.listMoreHistoryMessages(sessionId, minMessageId, limitNum);

        // 3. 调用公共方法转换VO
        List<AiChatMessageVO> voList = convertToVoList(messageList, sessionUuid);

        return AppResponse.success(voList, "历史消息加载成功");
    }

    /**
     * 查询指定会话下的所有消息（按创建时间正序）
     */
    @Operation(
            summary = "查询会话下的所有消息",
            description = """
                查询指定AI对话会话下的所有消息（用户消息+AI回复）：
                1. 登录态接口，需携带有效的JWT Token；
                2. 会话UUID通过路径参数传入（标准UUID v4格式）；
                3. 消息按创建时间正序返回，保持对话时序；
                4. 接口1分钟内最多调用5次，防止高频查询；
                5. 过滤掉系统（SPRINGBOOT）类型的消息，仅返回用户/AI助手消息。
                """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/{sessionUuid}")
    @CheckJwt
    @RateLimit(seconds = 60, maxCount = 20)
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    public AppResponse<List<AiChatMessageVO>> listChatMessages(
            @Parameter(description = "对话会话UUID（标准UUID v4格式）", required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String sessionUuid) {

        // 🔥 统一替换：使用通用方法校验+获取sessionId
        AppResponse<Long> sessionIdResp = getSessionIdFromUuid(sessionUuid);
        if (!sessionIdResp.getCode().equals(200)) {
            return AppResponse.error(sessionIdResp.getCode(), sessionIdResp.getMessage(), null);
        }
        Long sessionId = sessionIdResp.getData();

        // 3. 从Service层查询原始消息列表
        List<AiChatMessage> messageList = aiChatMessageService.listChatMessagesBySessionId(sessionId);

        // 🔥 核心：调用公共私有方法，统一过滤+转换VO
        List<AiChatMessageVO> voList = convertToVoList(messageList, sessionUuid);

        // 5. 返回VO列表
        return AppResponse.success(voList, "会话消息查询成功");
    }

    // ====================== 🔥 抽离公共方法：消息过滤 + VO转换 ======================
    /**
     * 私有公共方法：过滤消息 + 转换为AiChatMessageVO
     * 1. 过滤系统消息(SPRINGBOOT)
     * 2. 过滤临时消息(is_temp=1)
     * 3. 转换VO并赋值sessionUuid
     */
    private List<AiChatMessageVO> convertToVoList(List<AiChatMessage> messageList, String sessionUuid) {
        List<AiChatMessageVO> voList = new ArrayList<>();
        // 过滤逻辑
        List<AiChatMessage> filteredMessageList = messageList.stream()
                .filter(message -> !ChatRoleEnum.SPRINGBOOT.equals(message.getRole())
                        && message.getIsTemp() != 1)
                .toList();
        // VO转换
        for (AiChatMessage message : filteredMessageList) {
            AiChatMessageVO vo = new AiChatMessageVO();
            BeanUtils.copyProperties(message, vo);
            vo.setSessionUuid(sessionUuid);
            voList.add(vo);
        }
        return voList;
    }

    // ====================== 通用工具方法（完全复用你写的） ======================
    /**
     * 通用工具：从 sessionUuid 获取 sessionId，统一异常处理
     */
    private AppResponse<Long> getSessionIdFromUuid(String sessionUuid) {
        if (sessionUuid == null || sessionUuid.isBlank()) {
            return AppResponse.error(400, "会话UUID不能为空", null);
        }

        Long sessionId;
        try {
            sessionId = aiChatSessionService.getSessionIdByUUID(sessionUuid);
        } catch (IllegalArgumentException e) {
            return AppResponse.error(400, e.getMessage(), null);
        }

        if (sessionId == null) {
            return AppResponse.error(400, "会话不存在，sessionUuid=" + sessionUuid, null);
        }

        return AppResponse.success(sessionId, "转换成功");
    }
}
