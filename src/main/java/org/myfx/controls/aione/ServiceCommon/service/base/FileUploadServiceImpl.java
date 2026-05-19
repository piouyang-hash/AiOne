package org.myfx.controls.aione.ServiceCommon.service.base;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.ServiceCommon.entity.dto.FileUploadDTO;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {

    /**
     * 核心上传逻辑：保存文件到本地指定路径
     */
    @Override
    public String uploadFile(FileUploadDTO dto) throws IOException {
        // 1. 基础参数校验
        Assert.notNull(dto.getFile(), "上传文件不能为空");
        Assert.isTrue(!dto.getFile().isEmpty(), "上传文件内容不能为空");
        Assert.hasText(dto.getUploadPath(), "文件存储路径不能为空");

        // 2. 获取完整文件路径
        String fullFilePath = dto.getFullFilePath();
        File targetFile = new File(fullFilePath);

        // 3. 自动创建目录
        if (!targetFile.getParentFile().exists()) {
            boolean mkdirSuccess = targetFile.getParentFile().mkdirs();
            Assert.isTrue(mkdirSuccess, "文件目录创建失败，请检查权限");
        }

        // 4. 写入文件，捕获后【原样抛出原始IO异常】
        try {
            FileCopyUtils.copy(dto.getFile().getBytes(), targetFile);
        } catch (IOException e) {
            // 直接抛出原始异常，不包装、不篡改，上层可完整感知
            log.error(e.getMessage());
            throw e;
        }

        // 5. 返回路径
        return fullFilePath;
    }
}
