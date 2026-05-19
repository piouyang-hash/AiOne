package org.myfx.controls.aione.ConnectService.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.myfx.controls.aione.AiService.aiClient.advisor.dto.ChatStreamErrorMetaVO;
import org.myfx.controls.aione.AiService.dto.ChatChunkDTO;
import org.myfx.controls.aione.ConnectService.common.WsMsgTypeEnum;

/**
 * WebSocket 统一消息体（生产级规范版）
 */
@Data
@Schema(description = "WebSocket 统一消息协议")
public class WebSocketMessage {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Schema(description = "消息类型编码：HEARTBEAT=心跳检测 AI_PUSH=AI消息推送", example = "AI_PUSH")
    private String msgType;

    @Schema(description = "消息内容（支持字符串/对象）", example = "AI给你发送了一条新消息")
    private Object message;

    // 枚举入参，赋值code
    public void setMsgType(WsMsgTypeEnum msgTypeEnum) {
        this.msgType = msgTypeEnum.getCode();
    }

    // ===================== 🌟 核心：极简静态方法 =====================
    /**
     * 构建心跳消息（字符串类型）
     */
    public static TextWebSocketFrame heartbeat(String message) {
        return createFrame(WsMsgTypeEnum.HEARTBEAT, message);
    }

    /**
     * 构建AI推送消息（直接传入DTO对象，无序列化、无中转）
     */
    public static TextWebSocketFrame aiPush(ChatChunkDTO message) {
        return createFrame(WsMsgTypeEnum.AI_PUSH, message);
    }

    // ===================== 🔥 新增：AI错误推送（标准VO格式） =====================
    public static TextWebSocketFrame aiPushError(ChatStreamErrorMetaVO errorMetaVO) {
        return createFrame(WsMsgTypeEnum.AI_ERROR, errorMetaVO);
    }

    // ===================== 内部封装：极简逻辑 - 直接封装+整体序列化 =====================
    /**
     * @param type      消息类型
     * @param message   消息内容（支持 String / ChatChunkDTO 任意对象）
     */
    private static TextWebSocketFrame createFrame(WsMsgTypeEnum type, Object message) {
        try {
            // 1. 构建标准消息体
            WebSocketMessage msg = new WebSocketMessage();
            msg.setMsgType(type);
            // 🔥 直接存储对象（DTO/字符串 都支持）
            msg.setMessage(message);

            // 2. 整体一次性序列化（无任何判断、无二次转义）
            String finalJson = OBJECT_MAPPER.writeValueAsString(msg);
            return new TextWebSocketFrame(finalJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("WebSocket消息构建失败", e);
        }
    }
}