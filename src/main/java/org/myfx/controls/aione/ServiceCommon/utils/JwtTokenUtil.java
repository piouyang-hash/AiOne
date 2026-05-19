package org.myfx.controls.aione.ServiceCommon.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.ServiceCommon.config.JwtConfig;
import org.myfx.controls.aione.ServiceCommon.exception.AuthException;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.AppTypeEnum;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.JwtTokenType;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.RoleEnum;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.util.Assert;

/**
 * JWT 工具类（Spring组件，支持读取配置文件）
 */
@Component // 标记为Spring组件，可被注入和读取配置
@RequiredArgsConstructor
@Slf4j
public class JwtTokenUtil {

    // ===================== 密钥工具方法（私有，底层使用） =====================
    // AccessToken 专属密钥
    private SecretKey getAccessTokenSignKey() {
        String accessTokenSecret = JwtConfig.getAccessTokenSecret();
        // 🔥 断言：AccessToken密钥不能为空
        Assert.hasText(accessTokenSecret, "JWT AccessToken专属密钥配置不能为空，请检查application.yml");
        return Keys.hmacShaKeyFor(accessTokenSecret.getBytes());
    }

    // RefreshToken 专属密钥
    private SecretKey getRefreshTokenSignKey() {
        String refreshTokenSecret = JwtConfig.getRefreshTokenSecret();
        // 🔥 断言：RefreshToken密钥不能为空
        Assert.hasText(refreshTokenSecret, "JWT RefreshToken专属密钥配置不能为空，请检查application.yml");
        return Keys.hmacShaKeyFor(refreshTokenSecret.getBytes());
    }

    // ===================== 【切面专用：核心方法】根据Token类型自动解析Claims =====================
    // 切面只需要传token和枚举，底层自动选密钥、校验、抛异常
    public Claims extractClaimsByTokenType(String token, JwtTokenType jwtTokenType) {
        SecretKey signKey;
        if (JwtTokenType.ACCESS == jwtTokenType) {
            signKey = getAccessTokenSignKey();
        } else if (JwtTokenType.REFRESH == jwtTokenType) {
            signKey = getRefreshTokenSignKey();
        } else {
            throw new AuthException(AuthException.AuthError.OTHER, "不支持的Token类型");
        }
        return extractAllClaimsByCustomKey(token, signKey);
    }

    // 生成 AccessToken
    public String generateAccessToken(Integer id, RoleEnum role, AppTypeEnum appType) {
        validateTokenParams(id, role, appType);
        Map<String, Object> claims = buildTokenClaims(id, role, appType);
        long expirationTime = JwtConfig.getAccessTokenExpirationTime();

        return Jwts.builder()
                .claims(claims)
                .claim("token_type", JwtTokenType.ACCESS.getType())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getAccessTokenSignKey())
                .compact();
    }

    // 生成 RefreshToken
    public String generateRefreshToken(Integer id, RoleEnum role, AppTypeEnum appType, boolean rememberMe) {
        validateTokenParams(id, role, appType);
        Map<String, Object> claims = buildTokenClaims(id, role, appType);

        long expirationTime;
        if (rememberMe) {
            expirationTime = JwtConfig.getRefreshTokenExpirationTime();
        } else {
            expirationTime = JwtConfig.getRefreshTokenSessionExpirationTime();
        }

        return Jwts.builder()
                .claims(claims)
                .claim("token_type", JwtTokenType.REFRESH.getType())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getRefreshTokenSignKey())
                .compact();
    }

    // ===================== 私有工具方法（完全不动） =====================
    private void validateTokenParams(Integer id, RoleEnum role, AppTypeEnum appType) {
        if (id == null) {
            String errorMsg = "生成Token失败：用户ID（id）不能为空";
            log.warn(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        if (role == null) {
            log.warn("生成Token警告：用户角色（role）为空，将继续生成令牌");
        }
        if (appType == null) {
            log.warn("生成Token警告：应用类型（appType）为空，将继续生成令牌");
        }
    }

    private Map<String, Object> buildTokenClaims(Integer id, RoleEnum role, AppTypeEnum appType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", id);
        claims.put("role", role);
        if (appType != null) {
            claims.put("appType", appType.name());
        }
        return claims;
    }

    /**
     * 新方法：自定义密钥解析（底层专用，切面不调用）
     * 适配 JJWT 0.13.0 版本
     */
    private Claims extractAllClaimsByCustomKey(String token, SecretKey signKey) {
        try {
            // JJWT 0.13.0 标准解析写法
            return Jwts.parser()
                    .verifyWith(signKey)  // 验证密钥（0.13.0 新API，替代旧版setSigningKey）
                    .build()
                    .parseSignedClaims(token) // 解析签名token
                    .getPayload(); // 获取载荷（替代旧版getBody）

        } catch (ExpiredJwtException e) {
            throw new AuthException(AuthException.AuthError.TOKEN_EXPIRED);
        } catch (SignatureException e) {
            throw new AuthException(AuthException.AuthError.INVALID_SIGNATURE);
        } catch (MalformedJwtException e) {
            throw new AuthException(AuthException.AuthError.MALFORMED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new AuthException(AuthException.AuthError.OTHER, e.getMessage());
        }
    }

}