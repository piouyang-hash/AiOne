package org.myfx.controls.aione.ServiceCommon;

import lombok.Data;

/**
 * 强统一回复体：成功code固定200，错误必须填全code/message/data
 */
@Data
public class AppResponse<T> {

    // 三个字段均为final：创建后不可修改，彻底杜绝中途改值导致的不统一
    private final Integer code;
    private final String message;
    private final T data;

    // 私有构造方法：禁止外部手动new Result，只能通过工厂方法创建
    private AppResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // -------------------------- 成功响应（code固定200，不可修改） --------------------------
    /**
     * 成功响应（带业务数据+自定义提示语）
     * @param data 业务数据（任意类型）
     * @param message 自定义成功提示（比如“新增用户成功”“查询列表成功”）
     */
    public static <T> AppResponse<T> success(T data, String message) {
        // code强制写死200，用户无法修改
        return new AppResponse<>(200, message, data);
    }

    // -------------------------- 错误响应（必须填全code/message/data，data无则传null） --------------------------
    /**
     * 错误响应（填全三个字段）
     * @param code 错误状态码（比如400=参数错误，404=资源不存在，500=系统错误）
     * @param message 错误提示信息（具体原因）
     * @param data 错误辅助数据（无则传null，比如异常栈、错误详情等）
     */
    public static <T> AppResponse<T> error(Integer code, String message, T data) {
        // 强制要求传code和message，data显式传入（无则填null），三个字段均不缺失
        return new AppResponse<>(code, message, data);
    }
}