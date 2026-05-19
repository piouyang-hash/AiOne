package org.myfx.controls.aione.ConnectService.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.myfx.controls.aione.ConnectService.common.WebSocketConstants;
import org.myfx.controls.aione.ConnectService.dto.WebSocketMessage;
import org.myfx.controls.aione.ConnectService.service.UserStatusService;
import org.myfx.controls.aione.ConnectService.utils.ChannelManager;
import org.myfx.controls.aione.ConnectService.utils.UserStatusRedisUtil;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.AppTypeEnum;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 长连接业务处理器：处理用户连接绑定
 */
@Slf4j
@Component
@Scope("prototype")
@RequiredArgsConstructor
public class ConnectHandler extends ChannelInboundHandlerAdapter {

    private final ChannelManager channelManager;
    private final UserStatusRedisUtil redisUtil;
    private final UserStatusService userStatusService;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // 1. 统一解析WS消息（简化：提取核心逻辑）
        String message = parseWsMessage(msg);
        if (StringUtils.isBlank(message)) return;

        // 2. 处理绑定消息（bind|userId|appName）
        if (message.startsWith("bind|")) {
            handleBindMessage(ctx, message);
            return;
        }

        // 3. 非绑定消息，交给下一个处理器
        ctx.fireChannelRead(msg);
    }

    /**
     * 统一解析WebSocket消息（简化重复代码）
     */
    private String parseWsMessage(Object msg) {
        if (msg instanceof TextWebSocketFrame) {
            return ((TextWebSocketFrame) msg).text().trim();
        }
        return msg.toString().trim();
    }

    /**
     * 简化绑定消息处理逻辑（适配appType）
     */
    private void handleBindMessage(ChannelHandlerContext ctx, String message) {
        try {
            String[] parts = message.split(WebSocketConstants.MSG_SEPARATOR);
            if (parts.length != 3) {
                ctx.writeAndFlush(WebSocketMessage.heartbeat("绑定失败：格式错误，正确格式：bind|用户ID|应用名称"));
                log.error("绑定消息格式错误：{}", message);
                return;
            }

            Integer userId = Integer.parseInt(parts[1]);
            AppTypeEnum appName = AppTypeEnum.valueOf(parts[2]);

            ctx.channel().attr(WebSocketConstants.USER_ID_KEY).set(userId);
            ctx.channel().attr(WebSocketConstants.APP_NAME_KEY).set(String.valueOf(appName));

            channelManager.bind(userId, ctx.channel());
            redisUtil.bindChannel(userId, appName, ctx.channel().id().asLongText());

            // ====================== 修复：自动获取IP + 标准UA（不报错） ======================
            String realIp = ctx.channel().remoteAddress().toString()
                    .replace("/", "").split(":")[0];
            // ✅ 修复：使用标准PC端User-Agent，解析不抛异常
            String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0 WebSocket";

            // 调用业务方法
            userStatusService.buildLoginUserStatusAndInitWsConnect(
                    userId, appName, realIp, userAgent
            );

            ctx.writeAndFlush(WebSocketMessage.heartbeat("绑定成功"));
            log.info("用户{}（应用：{}）绑定WS连接成功，通道ID：{}", userId, appName, ctx.channel().id());

        } catch (NumberFormatException e) {
            ctx.writeAndFlush(WebSocketMessage.heartbeat("绑定失败：用户ID必须是数字"));
            log.error("绑定用户ID格式错误：{}", message, e);
        } catch (Exception e) {
            ctx.writeAndFlush(WebSocketMessage.heartbeat("绑定失败：" + e.getMessage()));
            log.error("绑定WS连接失败：{}", message, e);
        }
    }

    /**
     * 统一发送错误响应（简化重复代码）
     */
    private void sendErrorResponse(ChannelHandlerContext ctx, String msg) {
        ctx.writeAndFlush(new TextWebSocketFrame("error: " + msg));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("长连接异常", cause);
        channelManager.unbind(ctx.channel());
        ctx.close();
    }
}