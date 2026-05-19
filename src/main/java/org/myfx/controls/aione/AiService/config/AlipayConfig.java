package org.myfx.controls.aione.AiService.config;

public class AlipayConfig {

    // ================== 你的沙箱环境固定配置 ==================
    // 沙箱网关（用你后台的正确地址，别用旧的！）
    public static final String GATEWAY_URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    // 你的沙箱APPID（固定）
    public static final String APP_ID = "9021000163628874";

    // ========== 你自己填写的密钥 ==========
    // 应用私钥（必须是 PKCS8 格式）
    public static final String APP_PRIVATE_KEY = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCGtBa7hTBKCiSURn3U1P5d1p6eMDUlPw7seUJ9nBjnVvGOpwMv8OQGLzXdKaEgbWgHF1803tSEPjiWIEvyOOhC4QOlzZ503LmDWCoRlfEYVzgVrUyKCrcvOlxUM3K0jsNu1HhU3HdCezMDQgr2G/68cEJKn0mJi4F4/EtYYVNUCMBFnbExTljnhTKnQeHQQ7UvOm17KBKKqbIoG0ow6BMu3jx8Hc6xpJbmQQ1zuQm3+buAcvslA+ABICla8prJnrADdOXW2nowA8P5/+8LlZAplYaYF3zeJp1Eua8Q9rp1SAodpv4iM3qfj/XgXGM7Id/T8FEqMXSx5v9rEIMzLGI3AgMBAAECggEAQXY5ae4DFqgxOKG9OvY7m4zWTtfsq+omLYOMbZCzhZF6GLJPWTf/CIZe7djyRdkFahTQntf/fTjjtQmC/vbq4L2LUUNI01AFE7B/2UX+Aa85QUeT363vchMxlXXfM6IBldXJmKv/hR5sHxQ0UYAvXpJLlQot6ZsTU6GAWDYMEdTth33s48kKCIndukyhAmB0Y2XZNKu4enPc1Ln8rrXlnR9XgqfWbA+0/mnO7L/LYAPa92IrTIe0vQM43wum+96WJP4rX4R34K79eIWKWbBf/7pULRiI+ov90NZhQtMEprDdWvJDHREuqhpRmlj7AXIMCVWvHOecUbRrvVCe+JvFqQKBgQDM1sQYFlkPDpnwB3rp5VxoKC1f5cAqxseFzRfDK5WQlXS///ImPOOPKmkfHq1WOgFpReDyg1aIpwN1lXY06IodVMzkbZyG0dxlcrmNPs6MH9pJgyRt9/Aw1GhLCWJVsPbUd3hWXmzLcc4WqZO0G8SgH65of3OhvDapJkVNbvGDawKBgQCoWOs672zg08BcFDQpRTrjoqnT+LstVQFsJdgmxu1f07g0ZJOQT5oHnLrxiOy88CvJyHRy2u5l5yVcKfSxgP+YB+YclPILmdYi/tKtVJrC7hH8FW8dcHDrtGO7hVSwcDI3BnCEhAp+kjMMX68rOYb8ZkSGjy6BiHB66GD6fCXbZQKBgDtuqNCwU0C6JNHu6avJf4k89wVW/O2sZSGeQyG+mJOtuYUnttN/YSsdzAhad0KIa/ZmbwEOOGJHywonxndNbR1biGNSplK7uy24Hrwrl/QyHbysjzpU8NzKFESJQX3JbaRx0XmFxh01NO2AYDmmwOMQriLlr+AiYt2rG/p+kRZnAoGBAJYUFzjGI4FXJSCxXkMAldxDsjTnhC6InqkEH+uJ4ipQu+HxW+AGJuHsZnhXQf25+r97tdyCso8j/ploSd1IjBPDzu/iFNokvdMa60RcrU9kH6JklVdgWzq16UdOOM7GZu3JDtwEPrrRqTK5xpa5RU6T+TkgQix2LxhTO8lyjelFAoGBALrRKyH94+t7cwNtQGTbmoSLQgzCBWr9wLPGq2sSf1ZoApwrA50vqXKzVJjMVxKGdyvk1ZLT/Qi/JmlZ+YGJxAadpG1Nev43E6H2S8c94m9Kt1rUpxVoK3zi50ybHg8UiBVh7of+g+/CtQm/O42FPGUgIOA3InTdb0BmbEOAe2Fp";
    // 支付宝公钥（沙箱后台复制）
    public static final String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoto9vSKU8+eHL/bhqJHqZrTNO50e9dbuF0tQTjnJKRaUKgVZrMVwJR1F+V5M/34Ix8E/Az1nKwGMyBk8nuEn0/cr7yBOYvNMgbCbHipXeGcNpNqslsuQymEN72MsCtD4ABR3t5np8RF0NM2LfLPbzU6r5NgYRt/QiEkj6/QPt/hP6r7JFp9YGcx3c4gSZXwd4us5wT9M3g8oQ0F9MB86dKtrz3e/4PimeiNY0LrafGvOHdZDqVVjoDhPK1RJLfGP+F+L5yrmefFYuZyw4C1okFMTq9rWWzuUXOnHYt6J9qkoIAOo+FmsWEo6/choWRNiFQfur7czbxAb1K8Hz82TsQIDAQAB";

    // 固定参数，不用改
    public static final String FORMAT = "JSON";
    public static final String CHARSET = "UTF-8";
    public static final String SIGN_TYPE = "RSA2";

    // 支付成功 → 跳转到 B 站（测试跳转用）
    public static final String RETURN_URL = "https://www.bilibili.com/";
    // 异步通知地址暂时不改，不影响测试
    public static final String NOTIFY_URL = "https://festivity-humid-hurdle.ngrok-free.dev/alipay/notify";
}