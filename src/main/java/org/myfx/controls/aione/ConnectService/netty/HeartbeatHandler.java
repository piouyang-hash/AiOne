package org.myfx.controls.aione.ConnectService.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.myfx.controls.aione.ConnectService.common.WebSocketConstants;
import org.myfx.controls.aione.ConnectService.dto.WebSocketMessage;
import org.myfx.controls.aione.ConnectService.utils.ChannelManager;
import org.myfx.controls.aione.ConnectService.utils.UserStatusRedisUtil;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.AppTypeEnum;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 心跳处理器：检测客户端空闲，更新Redis心跳状态
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Scope("prototype")
public class HeartbeatHandler extends ChannelInboundHandlerAdapter {

    private final UserStatusRedisUtil redisUtil;
    private final ChannelManager channelManager;

    /**
     * 处理空闲事件（客户端30秒没发心跳）
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent event) {
            if (event.state() == IdleState.READER_IDLE) {
                // 读空闲：客户端超时未发心跳，关闭连接+解绑
                Integer userId = ctx.channel().attr(ChannelManager.USER_ID_KEY).get();
                log.warn("用户{}长连接心跳超时，关闭连接", userId);
                channelManager.unbind(ctx.channel());
                ctx.close(); // 关闭通道
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // 1. 统一解析消息（复用简化逻辑）
        String message = parseWsMessage(msg);
        if (StringUtils.isBlank(message)) return;

        // 2. 处理心跳消息
        if (message.startsWith("heartbeat|")) {
            handleHeartbeatMessage(ctx, message);
            return;
        }

        // 3. 非心跳消息，交给下一个处理器
        ctx.fireChannelRead(msg);
    }

    /**
     * 统一解析WS消息
     */
    private String parseWsMessage(Object msg) {
        if (msg instanceof TextWebSocketFrame) {
            return ((TextWebSocketFrame) msg).text().trim();
        }
        return msg.toString().trim();
    }

    /**
     * 简化心跳消息处理逻辑（减少嵌套、统一校验）
     */
    private void handleHeartbeatMessage(ChannelHandlerContext ctx, String message) {
        try {
            // 1. 解析心跳包（heartbeat|userId|appName）
            String[] parts = message.split(WebSocketConstants.MSG_SEPARATOR);
            if (parts.length != 3) {
                throw new IllegalArgumentException("心跳包格式错误，正确：heartbeat|用户ID|应用名称");
            }
            Integer reqUserId = Integer.parseInt(parts[1]);
            String reqAppName = parts[2];

            // 2. 获取Channel绑定的属性（统一常量）
            Integer bindUserId = ctx.channel().attr(WebSocketConstants.USER_ID_KEY).get();
            String bindAppName = ctx.channel().attr(WebSocketConstants.APP_NAME_KEY).get();

            // 3. 统一校验（简化嵌套）
            if (!validateBindInfo(ctx, bindUserId, bindAppName, reqUserId, reqAppName)) {
                return;
            }

            // 4. 刷新心跳（简化逻辑）
            refreshHeartbeat(ctx, bindUserId);

        } catch (NumberFormatException e) {
            sendErrorResponse(ctx, "用户ID必须是数字");
            log.error("心跳包用户ID格式错误：{}", message, e);
        } catch (IllegalArgumentException e) {
            sendErrorResponse(ctx, e.getMessage());
            log.error("心跳包格式错误：{}", message, e);
        }
    }

    /**
     * 统一校验绑定信息（简化嵌套）
     */
    private boolean validateBindInfo(ChannelHandlerContext ctx, Integer bindUserId, String bindAppName,
                                     Integer reqUserId, String reqAppName) {
        // 校验1：未绑定用户
        if (bindUserId == null || bindAppName == null) {
            sendErrorResponse(ctx, "未绑定用户，请先登录");
            ctx.close();
            log.warn("收到未绑定连接的心跳包");
            return false;
        }
        // 校验2：用户ID/应用名称不匹配
        if (!bindUserId.equals(reqUserId) || !bindAppName.equals(reqAppName)) {
            sendErrorResponse(ctx, "用户ID/应用名称不匹配，禁止操作");
            ctx.close();
            log.warn("篡改检测：绑定({}|{})，前端发送({}|{})", bindUserId, bindAppName, reqUserId, reqAppName);
            return false;
        }
        return true;
    }

    /**
     * 刷新心跳（简化业务逻辑，适配appType）
     */
    private void refreshHeartbeat(ChannelHandlerContext ctx, Integer userId) {
        // 从Channel中获取绑定的appName
        AppTypeEnum appName = AppTypeEnum.valueOf(ctx.channel().attr(WebSocketConstants.APP_NAME_KEY).get());

        // 刷新Redis心跳
        boolean success = redisUtil.refreshHeartbeat(userId, appName);

        if (success) {
            // ✅ 心跳成功：发送标准心跳消息（中文：砰砰）
            ctx.writeAndFlush(WebSocketMessage.heartbeat("砰砰"));
        } else {
            // ❌ 心跳过期：发送过期提示（中文：状态过期）
            ctx.writeAndFlush(WebSocketMessage.heartbeat("状态过期"));
            ctx.close();
            log.warn("用户{}（应用：{}）心跳刷新失败：登录状态过期", userId, appName);
        }
    }

    /**
     * 统一发送错误响应
     */
    private void sendErrorResponse(ChannelHandlerContext ctx, String msg) {
        ctx.writeAndFlush(new TextWebSocketFrame("error: " + msg));
    }

    /**
     * 连接断开时解绑
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        channelManager.unbind(ctx.channel());
        ctx.fireChannelInactive();
    }
}