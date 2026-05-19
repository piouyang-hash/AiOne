package org.myfx.controls.aione.ServiceCommon.service.upper;

import lombok.RequiredArgsConstructor;
import org.myfx.controls.aione.ServiceCommon.entity.dto.FileOperationLogDTO;
import org.myfx.controls.aione.ServiceCommon.entity.dto.ImageUploadDTO;
import org.myfx.controls.aione.ServiceCommon.exception.FileSizeExceededException;
import org.myfx.controls.aione.ServiceCommon.service.FileOperationService;
import org.myfx.controls.aione.ServiceCommon.service.base.ImageService;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.FileBusinessTypeEnum;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.FileOperationStatusEnum;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.FileOperationTypeEnum;
import org.myfx.controls.aione.ServiceCommon.utils.FileBusinessUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ImageUploadServiceImpl implements ImageUploadService {

    private final ImageService imageService;
    private final FileOperationService fileOperationService;

    // ==================== 公开方法：AI角色头像上传 ====================
    @Override
    public String uploadAiRoleAvatar(MultipartFile file, Integer userId) throws IOException {
        // 调用通用方法，传入业务类型：AI角色头像
        return uploadCommonAvatar(file, userId, FileBusinessTypeEnum.AI_ROLE_AVATAR);
    }

    // ==================== 公开方法：用户头像上传（新增） ====================
    @Override
    public String uploadUserAvatar(MultipartFile file, Integer userId) throws IOException {
        // 调用通用方法，传入业务类型：用户头像
        return uploadCommonAvatar(file, userId, FileBusinessTypeEnum.USER_AVATAR);
    }

    // ==================== 私有通用方法：核心上传逻辑（代码复用） ====================
    private String uploadCommonAvatar(MultipartFile file, Integer userId, FileBusinessTypeEnum businessType) {
        // 1. 类型转换
        Long userIdLong = userId.longValue();
        String fullFilePath = null;
        FileOperationStatusEnum operationStatus = FileOperationStatusEnum.SUCCESS;
        String failReason = null;

        try {
            // 🔥 一行调用：文件大小校验（通过则继续，失败自动抛异常）
            validateFileSize(file, businessType);

            // 2. 构建上传DTO
            ImageUploadDTO imageUploadDTO = new ImageUploadDTO();
            imageUploadDTO.setImageFile(file);

            // 硬编码存储路径（通用）
            String physicalPath = FileBusinessUtil.getPhysicalPath(businessType);
            imageUploadDTO.setFilePath(physicalPath);

            // 3. 执行上传
            fullFilePath = imageService.uploadImage(imageUploadDTO);

        } catch (Exception e) {
            // 异常处理
            operationStatus = FileOperationStatusEnum.FAIL;
            failReason = "头像上传失败：" + e.getMessage();
            throw new RuntimeException("头像上传失败", e);
        } finally {
            // 4. 构建日志（通用）
            FileOperationLogDTO logDTO = new FileOperationLogDTO();
            logDTO.setBusinessId(userIdLong);
            logDTO.setBusinessType(businessType); // 动态业务类型
            logDTO.setFilePath(fullFilePath);
            logDTO.setOperationType(FileOperationTypeEnum.UPLOAD);
            logDTO.setOperationStatus(operationStatus);
            logDTO.setFailReason(failReason);
            logDTO.setOperatorId(userIdLong);

            // 记录日志
            fileOperationService.insertOperationLog(logDTO);
        }

        return fullFilePath;
    }

    // ==================== 🔥 封装：独立文件大小校验方法 ====================
    /**
     * 校验文件大小是否超出限制
     * 校验通过：无返回值，继续执行
     * 校验失败：抛出 FileSizeExceededException
     */
    private void validateFileSize(MultipartFile file, FileBusinessTypeEnum businessType) {
        // 1. 获取配置的最大文件大小
        String maxFileSizeStr = FileBusinessUtil.getMaxFileSize(businessType);
        // 2. 解析为字节数
        long maxFileSizeBytes = parseFileSizeToBytes(maxFileSizeStr);
        // 3. 校验并抛出自定义异常
        if (file.getSize() > maxFileSizeBytes) {
            throw new FileSizeExceededException("文件上传失败：文件大小超出限制，最大允许上传：" + maxFileSizeStr);
        }
    }

    // ==================== 解析文件大小为字节数（原有逻辑保留） ====================
    private long parseFileSizeToBytes(String fileSizeStr) {
        if (fileSizeStr == null || fileSizeStr.isBlank()) {
            return 0;
        }
        try {
            String upperSize = fileSizeStr.toUpperCase();
            if (upperSize.endsWith("MB")) {
                double size = Double.parseDouble(upperSize.replace("MB", "").trim());
                return (long) (size * 1024 * 1024);
            } else if (upperSize.endsWith("KB")) {
                double size = Double.parseDouble(upperSize.replace("KB", "").trim());
                return (long) (size * 1024);
            } else {
                return Long.parseLong(upperSize.trim());
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("文件大小配置解析失败：" + fileSizeStr);
        }
    }
}
