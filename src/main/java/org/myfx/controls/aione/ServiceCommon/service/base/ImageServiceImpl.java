package org.myfx.controls.aione.ServiceCommon.service.base;

import lombok.RequiredArgsConstructor;
import org.myfx.controls.aione.ServiceCommon.entity.dto.FileUploadDTO;
import org.myfx.controls.aione.ServiceCommon.entity.dto.ImageUploadDTO;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    // 注入你封装的文件上传业务类
    private final FileUploadService fileUploadService;

    @Override
    public String uploadImage(ImageUploadDTO dto) throws IOException {
        // 1. 基础参数校验
        Assert.notNull(dto.getImageFile(), "图片文件不能为空");
        Assert.hasText(dto.getFilePath(), "文件存储路径不能为空");

        // 2. 组装 通用文件上传DTO (核心：三个必填参数)
        FileUploadDTO fileUploadDTO = new FileUploadDTO();
        // 文件本体
        fileUploadDTO.setFile(dto.getImageFile());
        // 存储路径
        fileUploadDTO.setUploadPath(dto.getFilePath());
        // 【关键】使用带真实后缀的文件名（你之前封装的方法）
        fileUploadDTO.setFileName(dto.getRealFileName());

        // 4. 返回最终路径（接口修改后要求返回值）
        return fileUploadService.uploadFile(fileUploadDTO);
    }
}