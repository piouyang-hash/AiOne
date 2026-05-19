package org.myfx.controls.aione.ServiceCommon.serviceEnum;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 文件业务类型枚举（统一管理文件上传的业务场景）
 * code：数字编码（Integer类型，存入数据库）
 * desc：业务描述（用于日志/前端展示）
 * serviceName：所属微服务名称
 */
@Getter // 自动生成getter方法，方便获取code、desc、serviceName
public enum FileBusinessTypeEnum {

    // todo 需要加第四个变量，就是文件的存储路径，我可以使用value读取，就在这里写，不用纠结了，这个是第二天的逻辑

    /** AI角色头像上传 */
    AI_ROLE_AVATAR(1, "AiRole的头像上传", "ai-service"),

    /** 用户头像上传 */
    USER_AVATAR(2, "用户头像上传", "user-service"),

    /** 书籍封面上传 */
    BOOK_COVER(3, "书籍封面上传", "book-service");

    /** 业务编码（存入数据库的数字值，MySQL INT类型） */
    @EnumValue // MyBatis-Plus注解：指定存入数据库的字段值
    private final Integer code;

    /** 业务描述（中文语义化，用于日志/前端展示） */
    private final String desc;

    /** 所属微服务名称 */
    private final String serviceName;

    // 构造方法（私有，枚举固定写法）
    FileBusinessTypeEnum(Integer code, String desc, String serviceName) {
        this.code = code;
        this.desc = desc;
        this.serviceName = serviceName;
    }

    /**
     * 核心方法：根据数据库code值获取枚举
     * @param code 数据库存储的数字编码
     * @return 匹配的枚举值
     * @throws IllegalArgumentException 无匹配编码时抛出异常
     */
    public static FileBusinessTypeEnum getByCode(Integer code) {
        for (FileBusinessTypeEnum typeEnum : values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        throw new IllegalArgumentException("无效的文件业务类型编码：" + code);
    }
}