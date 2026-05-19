package org.myfx.controls.aione.ConnectService.common;

import io.netty.util.AttributeKey;

/**
 * WebSocket公共常量
 */
public class WebSocketConstants {
    // 用户ID属性Key（统一复用）
    public static final AttributeKey<Integer> USER_ID_KEY = AttributeKey.valueOf("userId");
    // 应用名称属性Key
    public static final AttributeKey<String> APP_NAME_KEY = AttributeKey.valueOf("appName");
    // 消息分割符
    public static final String MSG_SEPARATOR = "\\|";
}