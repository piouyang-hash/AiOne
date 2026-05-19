package org.myfx.controls.aione.ServiceCommon.service.upper;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

/**
 * AI角色头像上传 业务接口
 */
public interface ImageUploadService {

    /**
     * AI角色头像上传（核心业务方法）
     * @param file 头像文件
     * @param userId 操作人/归属用户ID
     * @return 上传后的文件完整路径
     */
    String uploadAiRoleAvatar(MultipartFile file, Integer userId) throws IOException;

    /**
     * 用户头像上传（新增）
     */
    String uploadUserAvatar(MultipartFile file, Integer userId) throws IOException;

}