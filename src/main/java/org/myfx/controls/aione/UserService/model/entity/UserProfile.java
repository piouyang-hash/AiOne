package org.myfx.controls.aione.UserService.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.myfx.controls.aione.ServiceCommon.config.FileStorageConfig;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.AppTypeEnum;

import java.time.LocalDateTime;

/**
 * 用户资料信息实体
 */
@Data
@Schema(description = "用户资料信息")
public class UserProfile {

    @Schema(description = "用户资料ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer id; // 自增主键（只读，无需前端传递）

    @Schema(description = "用户ID（必须与当前登录用户ID一致）", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer userId; // 外键关联user表（测试时需填写，业务层会校验是否为当前登录用户）

    @Schema(description = "用户昵称", example = "AbstractUserProfile")
    private String nickname; // 昵称（可修改）

    /** 应用类型枚举（非数据库字段，用于业务层转换） */
    @Schema(description = "应用类型（枚举）")
    private AppTypeEnum appType;

    @Schema(description = "个人简介", example = "爱写代码的理工男～")
    private String bio; // 简介（可为空）

    @Schema(description = "头像URL（前端无需填写，后端处理图片后自动生成）", accessMode = Schema.AccessMode.READ_ONLY)
    private String avatarUrl; // 头像路径（后端自动设置，前端无需传递）

    @Schema(description = "创建时间", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createTime; // 创建时间（系统自动生成，只读）

    // ==================== 业务层使用：接收【物理绝对路径】→ 自动截取【相对路径】存入数据库 ====================
    public void setAvatarUrl(String absolutePath) {
        // 获取配置中心的用户头像物理根路径
        String physicalPath = FileStorageConfig.getUserAvatarPhysicalPath();
        if (absolutePath != null && physicalPath != null && absolutePath.startsWith(physicalPath)) {
            // 截取掉物理前缀，只存相对路径
            this.avatarUrl = absolutePath.substring(physicalPath.length());
        } else {
            this.avatarUrl = absolutePath;
        }
    }

    // ==================== 业务层使用：返回【网络访问路径】→ 前端直接可访问 ====================
    public String getAvatarUrl() {
        // 获取配置中心的用户头像网络前缀
        String networkUrl = FileStorageConfig.getUserAvatarNetworkUrl();
        if (avatarUrl == null || networkUrl == null) {
            return avatarUrl;
        }
        // 直接拼接：数据库里的avatarUrl已自带前置/，无需额外加斜杠
        return networkUrl + avatarUrl;
    }

    // ==================== MyBatis 专用方法：绕过路径处理，直接操作原始字段 ====================
    /**
     * MyBatis 专用：直接返回数据库存储的相对路径，不触发网络路径拼接
     */
    public String getAvatarRelativeUrl() {
        return avatarUrl;
    }

    /**
     * MyBatis 专用：直接设置数据库存储的相对路径，不触发物理路径截取
     */
    public void setAvatarRelativeUrl(String relativeUrl) {
        this.avatarUrl = relativeUrl;
    }
}