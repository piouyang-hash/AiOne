package org.myfx.controls.aione.UserService.common.exception;

/**
 * 空参数异常：当实体的某个参数为null时抛出
 */
public class NullParamException extends RuntimeException {

    // 构造方法：接收 实体名 和 参数名，生成具体错误信息
    public NullParamException(String entityName, String paramName) {
        super(entityName + "实体的" + paramName + "参数不能为空～");
    }
}