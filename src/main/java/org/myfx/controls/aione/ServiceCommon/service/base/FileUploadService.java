package org.myfx.controls.aione.ServiceCommon.service.base;

import org.myfx.controls.aione.ServiceCommon.entity.dto.FileUploadDTO;

import java.io.IOException;

/**
 * 文件上传业务接口
 */
public interface FileUploadService {

    /**
     * 执行文件上传保存
     * @param dto 文件上传参数
     * @return 最终保存的完整文件路径
     * @throws IOException 文件写入失败时抛出
     */
    String uploadFile(FileUploadDTO dto) throws IOException;
}