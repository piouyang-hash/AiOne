package org.myfx.controls.aione.ConnectService.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * 设备类型枚举（包含code编码和desc描述）
 * 枚举值：PC、Android、iOS、H5、小程序等
 */
@Schema(description = "设备类型枚举")
@Getter // Lombok注解，自动生成getCode()、getDesc()方法
public enum DeviceTypeEnum {

    /** 电脑端 */
    PC("001", "电脑端（Windows/Mac）"),
    /** 安卓手机 */
    ANDROID("002", "安卓手机"),
    /** iOS手机 */
    IOS("003", "苹果手机"),
    /** H5页面 */
    H5("004", "H5网页"),
    /** 微信小程序 */
    MINI_PROGRAM("005", "微信小程序");

    /** 设备编码（唯一标识，比如存到Redis/数据库用） */
    @Schema(description = "设备编码")
    private final String code;

    /** 设备描述（展示用，比如接口文档、日志） */
    @Schema(description = "设备描述")
    private final String desc;

    // 构造方法（枚举的构造方法必须是private）
    DeviceTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @JsonValue  // 告诉 Jackson 序列化时只取这个值
    public String getCode() {
        return code;
    }

    /**
     * 静态方法：根据code获取枚举（方便业务调用）
     * 比如：DeviceTypeEnum.getByCode("001") → 返回PC
     */
    @JsonCreator  // 告诉 Jackson 反序列化时如何根据值找枚举
    public static DeviceTypeEnum getByCode(String code) {
        for (DeviceTypeEnum enumItem : DeviceTypeEnum.values()) {
            if (enumItem.getCode().equals(code)) {
                return enumItem;
            }
        }
        throw new IllegalArgumentException("无效的设备编码：" + code);
    }

    /**
     * 核心解析方法：根据User-Agent字符串匹配设备类型枚举
     * @param userAgent HTTP请求头中的user-agent字符串（可为null）
     * @return 匹配的设备枚举，无匹配时抛出IllegalArgumentException（和getByCode风格一致）
     */
    public static DeviceTypeEnum parseFromUserAgent(String userAgent) {
        // 1. 处理空值场景
        if (userAgent == null || userAgent.trim().isEmpty()) {
            throw new IllegalArgumentException("User-Agent字符串不能为空");
        }

        // 2. 统一转小写，避免大小写匹配问题
        String ua = userAgent.toLowerCase().trim();

        // 3. 按优先级匹配（小程序 > 移动端 > PC > H5）
        // 微信小程序特征：包含micromessenger + miniprogram/mini program
        if (ua.contains("micromessenger") && (ua.contains("miniprogram") || ua.contains("mini program"))) {
            return MINI_PROGRAM;
        }
        // 安卓手机特征：包含android且是移动端
        else if (ua.contains("android") && ua.contains("mobile")) {
            return ANDROID;
        }
        // iOS手机特征：包含iphone/ios/ipad且是移动端
        else if ((ua.contains("iphone") || ua.contains("ios") || ua.contains("ipad")) && ua.contains("mobile")) {
            return IOS;
        }
        // PC端特征：包含windows/macintosh，且不含mobile（排除移动端伪装）
        else if (ua.contains("windows") || (ua.contains("macintosh") && !ua.contains("mobile"))) {
            return PC;
        }
        // H5页面：以上都不匹配，但包含移动端特征（mobile/webkit）
        else if (ua.contains("mobile") || ua.contains("webkit") || ua.contains("html5")) {
            return H5;
        }
        // 无匹配场景
        else {
            throw new IllegalArgumentException("无法解析的User-Agent：" + userAgent);
        }
    }
}