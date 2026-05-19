package org.myfx.controls.aione.ServiceCommon.annotation;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;
import java.lang.annotation.*;

/**
 * 公共跨域注解：允许读者端+AI端共同调用（如登录、注销接口）
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@CrossOrigin(
        origins = {"http://localhost:5173", "http://192.168.1.2:5173",
                "http://localhost:5175", "http://192.168.1.2:5175",
                "http://localhost:5174", "http://192.168.1.2:5174"
        },
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
        allowedHeaders = {"Content-Type", "Authorization"},
        allowCredentials = "true",
        maxAge = 3600
)
public @interface PublicAppCors {
}