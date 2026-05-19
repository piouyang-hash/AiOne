package org.myfx.controls.aione.ServiceCommon.service.base;

import org.myfx.controls.aione.ServiceCommon.entity.dto.ImageUploadDTO;

import java.io.IOException;

public interface ImageService {

    /**
     * 上传图片文件
     * @param dto 图片上传参数
     * @return 上传成功后的文件完整存储路径
     */
    String uploadImage(ImageUploadDTO dto) throws IOException;
}