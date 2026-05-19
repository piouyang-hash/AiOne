package org.myfx.controls.aione.UserService.controller.test;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 聊天接口 Demo（带 Cookie 会话标识）
 * 依赖：Spring Boot 3.x + Spring Web（jakarta.servlet 包）
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    // Cookie 相关配置
    private static final String COOKIE_NAME = "chat_sid";  // 自定义会话标识 Cookie 名
    private static final int COOKIE_MAX_AGE = 60 * 60 * 24 * 7; // Cookie 有效期：7天（秒）

    /**
     * 聊天消息发送接口
     * @param chatRequest 前端传入的消息体（JSON格式）
     * @param req HTTP请求对象（用于读取Cookie）
     * @param resp HTTP响应对象（用于写入Cookie）
     * @return 包含会话标识和消息的回复
     */
    @PostMapping("/send")
    public String handleChat(
            @RequestBody ChatRequest chatRequest,  // 接收前端传入的消息DTO
            HttpServletRequest req,
            HttpServletResponse resp) {

        // 1. 从请求中读取已有的会话标识 Cookie
        String sid = getCookieValue(req, COOKIE_NAME);

        // 2. 如果没有会话标识，生成新的 UUID 作为会话ID，并写入Cookie
        if (sid == null || sid.isEmpty()) {
            sid = UUID.randomUUID().toString().replace("-", ""); // 生成无横线的UUID，更简洁
            Cookie cookie = new Cookie(COOKIE_NAME, sid);
            cookie.setMaxAge(COOKIE_MAX_AGE); // 设置有效期
            cookie.setPath("/");              // 全站有效
            cookie.setHttpOnly(true);         // 禁止前端JS读取，提升安全性
            // cookie.setSecure(true);        // HTTPS环境下开启（本地测试注释掉）
            // cookie.setSameSite("Lax");     // 防止CSRF攻击，Lax模式兼容大部分场景
            resp.addCookie(cookie);           // 将Cookie写入响应
        }

        // 3. 模拟业务逻辑：拼接会话标识和用户消息（实际场景可对接LLM/数据库）
        String reply = String.format("【会话ID：%s】AI 回复：收到你的消息：%s", sid, chatRequest.getMessage());

        return reply;
    }

    /**
     * 工具方法：从HttpServletRequest中读取指定名称的Cookie值
     * @param req HTTP请求对象
     * @param cookieName 要读取的Cookie名称
     * @return Cookie值（不存在则返回null）
     */
    private String getCookieValue(HttpServletRequest req, String cookieName) {
        if (req.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : req.getCookies()) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /**
     * 内部静态DTO类：接收前端传入的聊天消息
     * 静态内部类：Spring能直接实例化（非静态内部类需要外部类实例，不适合DTO）
     */
    public static class ChatRequest {
        // 前端传入的消息内容（JSON字段名：message）
        private String message;

        // 必须提供无参构造器（Spring JSON反序列化需要）
        public ChatRequest() {}

        // Getter & Setter（Spring需要通过Getter读取属性）
        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}