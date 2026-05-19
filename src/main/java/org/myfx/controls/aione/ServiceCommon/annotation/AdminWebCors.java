package org.myfx.controls.aione.ServiceCommon.annotation;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;
import java.lang.annotation.*;

/**
 * 后端管理网页专属跨域注解：仅允许后端网页域名（端口5174）调用
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@CrossOrigin(
        // 核心修改：端口改为5174（后端网页端口）
        origins = {"http://localhost:5174", "http://192.168.1.2:5174"},
        // 保留和AiAppCors一致的请求方法
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
        // 保留允许的请求头（Content-Type、Authorization）
        allowedHeaders = {"Content-Type", "Authorization"},
        // 保留允许携带凭证（Cookie/Token）
        allowCredentials = "true",
        // 保留预检请求缓存时间（1小时）
        maxAge = 3600
)
public @interface AdminWebCors {
}