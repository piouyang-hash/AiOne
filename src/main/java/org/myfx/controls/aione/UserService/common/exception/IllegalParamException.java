package org.myfx.controls.aione.UserService.common.exception;

/**
 * 参数不合法异常：当实体的参数值不符合规则时抛出（比如ID为负数、年龄为负数等）
 */
public class IllegalParamException extends RuntimeException {

    // 构造方法：接收 实体名、参数名、不合法原因，生成具体错误信息
    public IllegalParamException(String entityName, String paramName, String reason) {
        super(entityName + "实体的" + paramName + "参数不合法：" + reason);
    }
}