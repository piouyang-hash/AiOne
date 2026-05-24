package org.myfx.controls.aione.ServiceCommon.context;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.ServiceCommon.exception.AuthException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求上下文工具类：用ThreadLocal存储当前请求的IP和请求头信息
 * 线程私有，确保多请求间数据隔离，避免参数冗余传递
 */
@Slf4j
public class RequestContext {

    // 存储当前请求的IP地址
    private static final ThreadLocal<String> CURRENT_IP = new ThreadLocal<>();

    // 储存当前请求的方法名
    private static final ThreadLocal<String> CURRENT_METHOD_NAME = new ThreadLocal<>(); // 新增这行

    // 存储当前请求的所有头信息（键：头名称，值：头内容）
    private static final ThreadLocal<Map<String, String>> CURRENT_HEADERS = new ThreadLocal<>();

    /**
     * 从当前请求初始化上下文（IP+请求头）
     * 需在请求处理前置阶段调用（如拦截器preHandle）
     */
    public static void init(Object handler) { // 新增handler参数，用于获取方法名
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("当前无有效HTTP请求上下文，无法初始化RequestContext");
        }
        HttpServletRequest request = attributes.getRequest();

        // 0. 解析并设置当前请求的方法名（仅当handler是方法接口时）
        if (handler instanceof HandlerMethod handlerMethod) {
            String methodName = handlerMethod.getMethod().getName();
            CURRENT_METHOD_NAME.set(methodName);
        }

        // 1. 解析IP（兼容代理场景）
        String ip = getIpAddress(request);
        CURRENT_IP.set(ip);

        // 2. 解析所有请求头
        Map<String, String> headers = new HashMap<>(16);
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headers.put(headerName, headerValue);
        }
        // 关键：打印所有请求头信息（日志级别用INFO，清晰不冗余）
        log.info("当前请求的所有请求头：{}", headers);
        CURRENT_HEADERS.set(headers);
    }

    /**
     * 获取当前请求的IP地址
     * 优先从代理头获取，否则取原生远程地址
     */
    private static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多代理场景取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 公开方法：从请求头中获取并处理纯JWT令牌（自动截取Bearer前缀）
     * 【双Token架构说明】本方法无需适配Access/Refresh双Token场景
     * 前端单次请求只会携带【一种】令牌（要么AccessToken，要么RefreshToken），通用解析无需修改
     * @return 处理后的纯令牌字符串（如"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."）
     * @throws AuthException 当Authorization头缺失或格式错误时抛出
     */
    public static String getToken() {
        // 1. 从当前请求头获取Authorization请求头
        String authorizationHeader = getHeader("Authorization");

        // 2. 校验请求头是否存在，不存在则抛出认证异常
        if (authorizationHeader == null) {
            throw new AuthException(AuthException.AuthError.MISSING_AUTH_HEADER);
        }

        // 3. 截取Bearer+空格(7位)，返回纯Token，适配所有JWT令牌(Access/Refresh通用)
        return authorizationHeader.substring(7).trim();
    }

    /**
     * 获取当前请求的IP地址
     */
    public static String getIp() {
        return CURRENT_IP.get();
    }

    /**
     * 获取当前请求的所有头信息
     */
    public static Map<String, String> getHeaders() {
        return CURRENT_HEADERS.get();
    }

    /**
     * 获取当前请求的方法名
     */
    public static String getMethodName() {
        return CURRENT_METHOD_NAME.get();
    }

    /**
     * 根据头名称获取指定请求头的值
     */
    public static String getHeader(String headerName) {
        Map<String, String> headers = CURRENT_HEADERS.get();
        return headers != null ? headers.get(headerName) : null;
    }

    /**
     * 清除当前线程的上下文信息（必须在请求结束后调用，如拦截器afterCompletion）
     * 防止线程池复用导致的内存泄漏和数据污染
     */
    public static void clear() {
        CURRENT_IP.remove();
        CURRENT_HEADERS.remove();
        CURRENT_METHOD_NAME.remove();
    }
}