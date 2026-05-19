package org.myfx.controls.aione.UserService.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    // 注入Spring邮件发送器
    private final JavaMailSender mailSender;

    // 发件人邮箱（从配置文件获取）
    @Value("${spring.mail.username}")
    private String from;

    /**
     * 发送验证码到指定邮箱
     * @param to 收件人邮箱
     * @param code 验证码
     */
    public void sendVerificationCode(String to, String code) {
        // 创建简单邮件消息
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from); // 发件人
        message.setTo(to); // 收件人
        message.setSubject("登录验证码"); // 邮件主题
        message.setText("你的登录验证码是：" + code + "，5分钟内有效，请勿泄露给他人。"); // 邮件内容

        // 发送邮件
        try {
            mailSender.send(message);
            System.out.println("验证码已发送到：" + to);
        } catch (Exception e) {
            throw new RuntimeException("发送验证码失败：" + e.getMessage());
        }
    }
}