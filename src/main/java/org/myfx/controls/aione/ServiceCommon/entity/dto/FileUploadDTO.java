package org.myfx.controls.aione.ServiceCommon.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传请求DTO（通用版）
 * 包含文件上传最核心、最基础的三个参数
 */
@Data
@Schema(description = "文件上传请求参数")
public class FileUploadDTO {

    @Schema(description = "上传的文件本体", requiredMode = Schema.RequiredMode.REQUIRED)
    private MultipartFile file;

    @Schema(description = "文件存储目标路径", requiredMode = Schema.RequiredMode.REQUIRED, example = "/upload/avatar/ai_role")
    private String uploadPath;

    /**
     * 文件名（必须携带后缀，支持多段点：1.2.3.png → 后缀为png）
     * 校验规则：必须包含 . 符号，保证存在文件后缀
     */
    @Schema(description = "文件名称（必须携带后缀，如：avatar.png、1.2.3.txt）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileName;

    /**
     * 自定义文件名Setter：强制校验后缀
     * 必须包含 . 才能赋值，否则抛出参数异常
     */
    public void setFileName(String fileName) {
        // 核心校验：文件名不能为空 + 必须包含后缀（.）
        Assert.hasText(fileName, "文件名称不能为空");
        Assert.isTrue(fileName.contains("."), "文件名称必须携带后缀（必须包含 . 符号）");

        // 校验通过，赋值
        this.fileName = fileName;
    }

    /**
     * 获取文件完整存储路径（自动拼接斜杠，解决路径重复/问题）
     * 示例：uploadPath=/upload/avatar + fileName=test.png → /upload/avatar/test.png
     */
    public String getFullFilePath() {
        if (this.uploadPath.endsWith("/")) {
            return this.uploadPath + this.fileName;
        }
        return this.uploadPath + "/" + this.fileName;
    }
}