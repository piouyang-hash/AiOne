package org.myfx.controls.aione.ServiceCommon.context;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.ServiceCommon.exception.AuthException;
import java.lang.ScopedValue;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public final class RequestContext {

    // 1. 封装所有请求数据（包含你要的 methodName！）
    public record RequestContextData(
            String ip,
            String methodName,
            Map<String, String> headers
    ) {}

    // 2. 唯一ScopedValue
    private static final ScopedValue<RequestContextData> CONTEXT = ScopedValue.newInstance();

    private RequestContext() {}

    // ==================== AOP内部调用：绑定作用域 ====================
    public static ScopedValue.Carrier bind(RequestContextData data) {
        return ScopedValue.where(CONTEXT, data);
    }

    // ==================== 原有方法：完全兼容，一行不改 ====================
    public static String getToken() {
        String authorizationHeader = getHeader("Authorization");
        if (authorizationHeader == null) {
            throw new AuthException(AuthException.AuthError.MISSING_AUTH_HEADER);
        }
        return authorizationHeader.substring(7).trim();
    }

    public static String getIp() {
        return CONTEXT.isBound() ? CONTEXT.get().ip() : null;
    }

    public static Map<String, String> getHeaders() {
        return CONTEXT.isBound() ? CONTEXT.get().headers() : null;
    }

    public static String getMethodName() {
        return CONTEXT.isBound() ? CONTEXT.get().methodName() : null;
    }

    public static String getHeader(String headerName) {
        if (!CONTEXT.isBound()) return null;
        return CONTEXT.get().headers().get(headerName);
    }

    // ==================== 工具方法 ====================
    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) ip = request.getHeader("Proxy-Client-IP");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) ip = request.getHeader("WL-Proxy-Client-IP");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) ip = request.getRemoteAddr();
        if (ip != null && ip.contains(",")) ip = ip.split(",")[0].trim();
        return ip;
    }

    public static Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.put(name, request.getHeader(name));
        }
        log.info("请求头：{}", headers);
        return headers;
    }
}