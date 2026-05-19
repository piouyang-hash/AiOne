package org.myfx.controls.aione.ServiceCommon.annotation;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;
import java.lang.annotation.*;

/**
 * AI端App专属跨域注解：仅允许AI端域名调用
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@CrossOrigin(
        origins = {"http://localhost:5175", "http://192.168.1.2:5175"},
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
        allowedHeaders = {"Content-Type", "Authorization"},
        allowCredentials = "true",
        maxAge = 3600
)
public @interface AiAppCors {
}