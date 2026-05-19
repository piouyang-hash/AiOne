package org.myfx.controls.aione.UserService.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.UserService.util.CodeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationService {

    private final EmailService emailService;
    private final StringRedisTemplate redisTemplate;

    // 验证码过期时间：5分钟
    @Value("${verification.code.expire-minutes}")
    private long codeExpireMinutes;  // 注意：去掉static，@Value不能直接注入静态字段


    // 超级管理员认证密钥过期时间：默认1分钟
    @Value("${admin.auth.expire-minutes}")
    private long adminAuthExpireMinutes;


    /**
     * 发送验证码并缓存（接收外部传入的邮箱参数）
     * @param email 接收验证码的邮箱
     */
    public void sendCode(String email) {
        // 1. 生成6位验证码
        String code = CodeUtils.generateCode();

        // 2. 发送邮件（直接使用传入的邮箱）
        emailService.sendVerificationCode(email, code);

        // 3. 缓存验证码（key：邮箱，value：验证码，过期时间从配置获取）
        redisTemplate.opsForValue().set(
                "verify:code:" + email,
                code,
                codeExpireMinutes,
                TimeUnit.MINUTES
        );
    }

    /**
     * 判断验证码是否无效（直接通过邮箱和输入的验证码验证）
     * @param email 接收验证码的邮箱（与发送时的邮箱一致）
     * @param inputCode 用户输入的验证码
     * @return 验证结果（true：验证码无效（错误或过期）；false：验证码有效（通过验证））
     */
    public boolean isInvalidCode(String email, String inputCode) {
        String key = "verify:code:" + email; // 直接用邮箱生成缓存key
        String cachedCode = redisTemplate.opsForValue().get(key); // 获取缓存的验证码

        // 验证通过则删除缓存，防止重复使用
        if (cachedCode != null && cachedCode.equals(inputCode)) {
            redisTemplate.delete(key);
            return false;
        }
        return true;
    }

    /**
     * 生成超级管理员认证密钥对：6位key + 32位高强度密码（仅存Redis，1分钟过期）
     */
    public void generateAdminAuthKey() {
        // 1. 生成6位key（标识）
        String authKey = CodeUtils.generateCode();
        // 2. 生成32位高强度密码（认证值）
        String authPassword = CodeUtils.generateJwtSecret();
        // 3. 存入Redis：key=admin:auth:6位key，value=32位密码，过期1分钟
        String redisKey = "admin:auth:" + authKey;
        redisTemplate.opsForValue().set(
                redisKey,
                authPassword,
                adminAuthExpireMinutes,
                TimeUnit.MINUTES
        );
        // 4. 用日志输出密钥对（替代System.out）
        log.info("【超级管理员认证密钥】6位Key：{} | 32位密码：{}", authKey, authPassword);
    }

    /**
     * 验证超级管理员认证密钥是否有效
     * @param inputKey 输入的6位key
     * @param inputPassword 输入的32位密码
     * @return true：密钥无效（错误/过期）；false：密钥有效（通过认证）
     */
    public boolean isInvalidAdminAuth(String inputKey, String inputPassword) {
        String redisKey = "admin:auth:" + inputKey;
        String cachedPassword = (String) redisTemplate.opsForValue().get(redisKey);

        // 验证通过则删除缓存（防止重复使用）
        if (cachedPassword != null && cachedPassword.equals(inputPassword)) {
            redisTemplate.delete(redisKey);
            return false;
        }
        return true;
    }
}