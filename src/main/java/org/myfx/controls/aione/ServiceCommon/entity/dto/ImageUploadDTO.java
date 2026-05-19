package org.myfx.controls.aione.ServiceCommon.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.myfx.controls.aione.ServiceCommon.utils.FileMagicNumberUtil;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Data
@Schema(description = "图片上传请求参数")
public class ImageUploadDTO {

    @Schema(description = "上传的图片文件", requiredMode = Schema.RequiredMode.REQUIRED)
    private MultipartFile imageFile;

    @Schema(description = "文件存储路径", requiredMode = Schema.RequiredMode.REQUIRED)
    private String filePath;

    /**
     * 获取【全局唯一】的最终文件名
     * 1. 生成 UUID 作为文件名主体（杜绝重复）
     * 2. 通过魔数工具获取文件真实后缀（保证文件类型安全）
     * 3. 拼接：UUID + 真实后缀，返回最终文件名
     * todo 魔数可能会有处理失败的时候，记得编写自定义错误
     */
    public String getRealFileName() throws IOException {
        // 1. 读取文件头，获取真实图片后缀（核心安全校验，保留！）
        byte[] fileHeader = FileMagicNumberUtil.readFileHeader(imageFile);
        String realSuffix = FileMagicNumberUtil.getRealImageSuffix(fileHeader);

        // 2. 生成无横杠的 UUID（全局唯一，替代原始文件名）
        String uuidFileName = UUID.randomUUID().toString().replace("-", "");

        // 3. 拼接唯一文件名 + 真实后缀（永不重复）
        return uuidFileName + "." + realSuffix;
    }
}