package org.myfx.controls.aione.UserService.aspect;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.myfx.controls.aione.UserService.common.annotation.ValidEmailCode;
import org.myfx.controls.aione.UserService.service.VerificationService;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;

/**
 * 邮箱验证码校验切面
 */
@Component
@Aspect
@RequiredArgsConstructor
public class EmailCodeValidateAspect {

    private final VerificationService verificationService; // 注入验证码校验服务

    /**
     * 切入点：标注了@ValidEmailCode注解的方法
     */
    @Pointcut("@annotation(org.myfx.controls.aione.UserService.common.annotation.ValidEmailCode)")
    public void emailCodeValidatePointcut() {}

    /**
     * 前置通知：执行验证码校验逻辑
     */
    @Before("emailCodeValidatePointcut() && @annotation(validEmailCode)")
    public void beforeValidate(JoinPoint joinPoint, ValidEmailCode validEmailCode) {
        // 1. 判断是否开启校验，未开启则直接返回
        if (!validEmailCode.validate()) {
            return;
        }

        // 2. 提取参数：邮箱和验证码（重构后能兼容String email参数）
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String email = extractEmailFromArgs(joinPoint.getArgs(), signature.getParameterNames());
        String code = extractCodeFromArgs(joinPoint.getArgs(), signature.getParameterNames());

        System.out.println(email);
        System.out.println(code);
        // 3. 参数校验：email和code不能为空
        validateParams(email, code);

        // ========== 测试阶段硬编码：仅校验验证码是否为 6 个 1 ==========
//         注释掉真实验证码校验逻辑，测试完可恢复
//         if (verificationService.isInvalidCode(email, code)) {
//             throw new RuntimeException("验证码错误或已过期");
//         }

        // 硬编码校验：仅允许 111111 作为验证码通过（测试专用）
        String testCode = "111111";
        if (!testCode.equals(code)) {
            throw new RuntimeException("测试阶段仅支持验证码：111111");
        }
    }

    /**
     * 重构：从方法参数中提取邮箱（兼容两种场景）
     * 场景1：参数是@RequestParam String email（参数名=email）
     * 场景2：参数是User对象（含getEmail()方法）
     */
    private String extractEmailFromArgs(Object[] args, String[] paramNames) {
        // 场景1：优先通过参数名"email"提取（适配@RequestParam String email）
        for (int i = 0; i < paramNames.length; i++) {
            if ("email".equals(paramNames[i]) && args[i] != null) {
                return args[i].toString(); // 直接取String类型的email值
            }
        }

        // 场景2：兼容User对象参数（保留原有逻辑，兜底）
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            try {
                // 反射获取User对象的getEmail方法（适配任意含getEmail()的对象）
                Method getEmailMethod = arg.getClass().getMethod("getEmail");
                Object emailObj = getEmailMethod.invoke(arg);
                if (emailObj != null) {
                    return emailObj.toString();
                }
            } catch (NoSuchMethodException e) {
                // 不是User对象，跳过
                continue;
            } catch (Exception e) {
                throw new RuntimeException("获取用户邮箱失败：" + e.getMessage());
            }
        }

        // 两种场景都没找到，返回null（后续validateParams会校验）
        return null;
    }

    /**
     * 提取验证码（复用原有逻辑，确保参数名是"code"能正确提取）
     */
    private String extractCodeFromArgs(Object[] args, String[] paramNames) {
        for (int i = 0; i < paramNames.length; i++) {
            if ("code".equals(paramNames[i]) && args[i] != null) {
                return args[i].toString();
            }
        }
        return null;
    }

    /**
     * 参数非空校验
     */
    private void validateParams(String email, String code) {
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("邮箱不能为空");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new RuntimeException("验证码不能为空");
        }
    }
}

