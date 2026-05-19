package org.myfx.controls.aione.UserService.common.exception;

import lombok.Getter;

/**
 * VIP相关业务异常（如重复开通、未开通却执行关闭等场景）
 */
@Getter
public class VipException extends RuntimeException {

    // VIP错误枚举：包含所有VIP相关的错误类型
    @Getter
    public enum VipError {
        ALREADY_VIP("用户已开通VIP，无需重复操作"),
        NOT_VIP("用户未开通VIP，无法执行此操作"),
        OPERATION_FAILED("VIP操作失败（如数据库异常）"),
        INVALID_USER("无效的用户ID（用户不存在）"),
        OTHER("其他VIP相关异常");

        // 错误描述信息
        private final String message;

        // 枚举构造器
        VipError(String message) {
            this.message = message;
        }
    }

    // 持有具体的VIP错误类型（枚举实例）
    private final VipError vipError;

    // 构造方法1：直接传入VIP错误类型（最常用）
    public VipException(VipError vipError) {
        super(vipError.getMessage()); // 异常消息直接用枚举的描述
        this.vipError = vipError;
    }

    // 构造方法2：传入VIP错误类型 + 自定义补充消息（灵活扩展）
    public VipException(VipError vipError, String extraMessage) {
        super(vipError.getMessage() + "：" + extraMessage);
        this.vipError = vipError;
    }

    // ============================== 使用示例（方便后续参考）==============================
    // 1. 用户已开通VIP却再次调用开通接口时
    // throw new VipException(VipException.VipError.ALREADY_VIP);
    //
    // 2. 用户未开通VIP却调用关闭接口时
    // throw new VipException(VipException.VipError.NOT_VIP);
    //
    // 3. VIP操作因数据库异常失败时（补充具体原因）
    // throw new VipException(VipException.VipError.OPERATION_FAILED, "数据库连接超时");
    //
    // 4. 传入的用户ID不存在时
    // throw new VipException(VipException.VipError.INVALID_USER, "用户ID=100不存在");
    //
    // 5. 其他无法归类的VIP异常
    // throw new VipException(VipException.VipError.OTHER, "未知错误，请联系管理员");
    // ===================================================================================

}