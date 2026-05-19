package org.myfx.controls.aione.ServiceCommon.annotation;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.*;

/**
 * 自定义跨域注解：封装Vue前端的跨域配置（支持GET/POST/PUT/DELETE/OPTIONS）
 */
@Target({ElementType.TYPE}) // 只能用在类上（控制器类）
@Retention(RetentionPolicy.RUNTIME) // 运行时保留，Spring能识别
@Documented // 生成文档时包含该注解
@CrossOrigin(
        origins = {"http://localhost:5173", "http://192.168.1.2:5173"},
        // 最终版：包含所有常用HTTP方法，一次配置终身受用
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
        allowedHeaders = "*",
        allowCredentials = "true"
)
public @interface MyVueApp {
    // 留空即可，核心配置已完整
}
