package org.myfx.controls.aione.AiService.entity.ai_chat_db;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.myfx.controls.aione.ServiceCommon.config.FileStorageConfig;

import java.time.LocalDateTime;

/**
 * AI微服务-角色表实体类
 */
@Data
@Schema(description = "AI角色实体")
public class AiRole {

    @Schema(description = "角色ID（主键，自增）")
    private Integer roleId;

    @Schema(description = "角色编码（唯一标识，如AFTER_SALE、BIRTHDAY_AI）")
    private String roleCode;

    @Schema(description = "角色基础描述（如：售后客服AI、生日祝福AI）")
    private String roleDesc;

    @Schema(description = "人设核心定义（AI性格/身份描述）")
    private String personaCore;

    @Schema(description = "人设语气风格（normal-正常/gentle-温柔/active-活泼/serious-严谨）")
    private String personaTone;

    @Schema(description = "角色头像路径")
    private String avatarPath;

    @Schema(description = "角色创建人ID（归属用户）")
    private Integer createUserId;

    @Schema(description = "角色状态 0-草稿 1-已发布")
    private Integer roleStatus;

    @Schema(description = "可见范围 0-私有(仅自己) 1-公开(所有人)")
    private Integer visibleScope;

    @Schema(description = "记录创建时间（自动填充）")
    private LocalDateTime createTime;

    @Schema(description = "记录更新时间（自动填充）")
    private LocalDateTime updateTime;


    // ==================== 业务层使用：接收【物理绝对路径】→ 自动截取【相对路径】存入数据库 ====================
    public void setAvatarPath(String absolutePath) {
        // 获取配置中心的物理根路径
        String physicalPath = FileStorageConfig.getAiRoleAvatarPhysicalPath();
        if (absolutePath != null && physicalPath != null && absolutePath.startsWith(physicalPath)) {
            // 截取掉物理前缀，只存相对路径
            this.avatarPath = absolutePath.substring(physicalPath.length());
        } else {
            this.avatarPath = absolutePath;
        }
    }

    // ==================== 业务层使用：返回【网络访问路径】→ 前端直接可访问 ====================
    public String getAvatarPath() {
        // 获取配置中心的网络前缀
        String networkUrl = FileStorageConfig.getAiRoleAvatarNetworkUrl();
        if (avatarPath == null || networkUrl == null) {
            return avatarPath;
        }
        // 修复：直接拼接！因为数据库里的 avatarPath 已经自带前置 /，无需额外加斜杠
        return networkUrl + avatarPath;
    }

    // MyBatis 专用：直接返回内部相对路径，不触发拼接
    public String getAvatarRelativePath() {
        return avatarPath;
    }

    // MyBatis 专用：直接设置内部相对路径，不触发去前缀逻辑
    public void setAvatarRelativePath(String relativePath) {
        this.avatarPath = relativePath;
    }

}