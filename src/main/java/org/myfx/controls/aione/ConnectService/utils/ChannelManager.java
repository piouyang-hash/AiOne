package org.myfx.controls.aione.ConnectService.utils;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 长连接通道管理（微服务高并发：ConcurrentHashMap保证线程安全）
 */
@Component
public class ChannelManager {
    // 存储：用户ID → Channel（长连接通道）
    private static final Map<Integer, Channel> USER_CHANNEL_MAP = new ConcurrentHashMap<>();
    // Channel属性：绑定用户ID（从通道中获取用户ID）
    public static final AttributeKey<Integer> USER_ID_KEY = AttributeKey.valueOf("userId");

    /**
     * 绑定用户与通道
     */
    public void bind(Integer userId, Channel channel) {
        // 先移除旧通道（同一用户重连）
        Channel oldChannel = USER_CHANNEL_MAP.remove(userId);
        if (oldChannel != null) {
            oldChannel.close(); // 关闭旧连接
        }
        // 绑定新通道
        USER_CHANNEL_MAP.put(userId, channel);
        channel.attr(USER_ID_KEY).set(userId); // 通道中存储用户ID
    }

    /**
     * 解绑用户与通道（连接断开时）
     */
    public void unbind(Channel channel) {
        Integer userId = channel.attr(USER_ID_KEY).get();
        if (userId != null) {
            USER_CHANNEL_MAP.remove(userId);
        }
    }

    /**
     * 根据用户ID获取通道（推送消息/检测连接）
     */
    public Channel getChannel(Integer userId) {
        return USER_CHANNEL_MAP.get(userId);
    }
}