package org.myfx.controls.aione.ServiceCommon.annotation;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;
import java.lang.annotation.*;

/**
 * 读者端App专属跨域注解：仅允许读者端域名调用
 */
@Target({ElementType.TYPE, ElementType.METHOD}) // 可用于类（整个控制器）或方法（单个接口）
@Retention(RetentionPolicy.RUNTIME)
@Documented
@CrossOrigin(
        origins = {"http://localhost:5173", "http://192.168.1.2:5173"},
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
        allowedHeaders = {"Content-Type", "Authorization"}, // 生产环境建议明确请求头，比*安全
        allowCredentials = "true",
        maxAge = 3600 // 预检请求缓存1小时
)
public @interface ReaderAppCors {
}