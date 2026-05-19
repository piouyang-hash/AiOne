package org.myfx.controls.aione.UserService.util.validation;

import org.myfx.controls.aione.UserService.common.exception.IllegalParamException;
import org.myfx.controls.aione.UserService.common.exception.NullParamException;
import org.myfx.controls.aione.UserService.common.exception.UserNotFoundException;
import org.myfx.controls.aione.UserService.model.entity.User;

public class UserValidationUtil {

    // 实体名：固定为"User"（因为这个工具类专门校验User实体）
    private static final String ENTITY_NAME = "User";

    // -------------------------- 通用基础校验（抛自定义异常） --------------------------
    /**
     * 通用非空校验：参数为null时，抛NullParamException
     */
    private static void validateNotNull(Object obj, String paramName) {
        if (obj == null) {
            throw new NullParamException(ENTITY_NAME, paramName);
        }
    }

    /**
     * 通用条件校验：参数不满足条件时，抛IllegalParamException
     */
    private static void validateCondition(boolean condition, String paramName, String reason) {
        if (!condition) {
            throw new IllegalParamException(ENTITY_NAME, paramName, reason);
        }
    }


    // -------------------------- User实体具体校验 --------------------------
    /**
     * 校验User对象不能为null
     */
    public static void validateUserNotNull(User user) {
        validateNotNull(user, "整个用户对象"); // 参数名描述为“整个用户对象”
    }

    /**
     * 校验User的id是否合法
     */
    public static void validateUserId(Integer id) {
        validateNotNull(id, "id"); // 先校验id不为null
        validateCondition(id > 0, "id", "必须是正数（大于0）"); // 再校验是正数
    }

    // 校验密码（非null、长度≥8）
    public static void validatePassword(String password) {
        validateNotNull(password, "password");
        // 修改：把6改成8，提示文字同步更新
        validateCondition(password.length() >= 8, "password", "长度不能少于8位");
    }

    /**
     * 校验邮箱必须是QQ邮箱（格式：数字@qq.com）
     */
    public static void validateQqEmail(String email) {
        // 1. 先校验邮箱不为null
        validateNotNull(email, "email");

        // 2. 去除前后空格（避免用户输入带空格的无效邮箱）
        String trimmedEmail = email.trim();

        // 3. 校验QQ邮箱格式：@前是1个及以上数字，@后是qq.com（严格匹配，不允许多余字符）
        // 正则说明：^\\d+ 表示以1个及以上数字开头；@qq\\.com$ 表示必须以@qq.com结尾（.需转义）
        String qqEmailRegex = "^\\d+@qq\\.com$";
        boolean isQqEmail = trimmedEmail.matches(qqEmailRegex);

        // 4. 不匹配则抛异常
        validateCondition(isQqEmail, "email", "必须是QQ邮箱（格式：数字@qq.com）");
    }

    // -------------------------- 新增：仅校验返回的User是否为null（核心需求） --------------------------
    /**
     * 校验从数据库查询返回的User是否为null
     * 若为null，抛出UserNotFoundException（固定消息“用户不存在”）
     * 若不为null，不做任何处理
     */
    public static void validateReturnedUserNotNull(User returnedUser) {
        if (returnedUser == null) {
            throw new UserNotFoundException(); // 直接抛无参异常，消息固定为“用户不存在”
        }
    }
}
