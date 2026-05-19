package org.myfx.controls.aione.ServiceCommon.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT 配置类（单体应用版）
 * 项目启动时加载配置，无动态刷新，全局静态调用
 * 全局直接调用：JwtConfig.getAccessTokenSecret()
 */
@Component
public class JwtConfig {

    // ==================== 静态 Getter（调用方式 100% 不变） ====================
    // ===================== 静态配置变量（全局直接使用） =====================
    @Getter
    private static String accessTokenSecret;
    @Getter
    private static long accessTokenExpirationTime;
    @Getter
    private static String refreshTokenSecret;
    @Getter
    private static long refreshTokenExpirationTime;
    @Getter
    private static long refreshTokenSessionExpirationTime;

    // ===================== 配置注入（实例变量接收） =====================
    // AccessToken 密钥
    @Value("${jwt.access-token.secret}")
    private String injectAccessTokenSecret;

    // AccessToken 过期时间(1小时)
    @Value("${jwt.access-token.expiration}")
    private long injectAccessTokenExpirationTime;

    // RefreshToken 密钥
    @Value("${jwt.refresh-token.secret}")
    private String injectRefreshTokenSecret;

    // RefreshToken 过期时间(7天)
    @Value("${jwt.refresh-token.expiration}")
    private long injectRefreshTokenExpirationTime;

    // 会话级 RefreshToken 过期时间（不记住我用）
    @Value("${jwt.refresh-token.session-expiration}")
    private long injectRefreshTokenSessionExpirationTime;

    // ===================== 初始化赋值（项目启动仅执行一次） =====================
    @PostConstruct
    public void initConfig() {
        accessTokenSecret = injectAccessTokenSecret;
        accessTokenExpirationTime = injectAccessTokenExpirationTime;
        refreshTokenSecret = injectRefreshTokenSecret;
        refreshTokenExpirationTime = injectRefreshTokenExpirationTime;
        refreshTokenSessionExpirationTime = injectRefreshTokenSessionExpirationTime;
    }

}