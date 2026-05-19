package org.myfx.controls.aione.ServiceCommon.service.upper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.ServiceCommon.entity.dto.FileOperationLogDTO;
import org.myfx.controls.aione.ServiceCommon.service.FileOperationService;
import org.myfx.controls.aione.ServiceCommon.service.base.FileDeleteService;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.FileBusinessTypeEnum;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.FileOperationStatusEnum;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.FileOperationTypeEnum;
import org.springframework.stereotype.Service;

/**
 * 图片删除业务实现类
 * 完全对标上传业务的日志/异常/枚举规范
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageDeleteServiceImpl implements ImageDeleteService {

    // 注入基础文件删除服务
    private final FileDeleteService fileDeleteService;
    // 注入文件操作日志服务
    private final FileOperationService fileOperationService;

    // ==================== 公开方法：删除AI角色头像 ====================
    @Override
    public void deleteAiRoleAvatar(String filePath, Integer userId) {
        deleteCommonImage(filePath, userId, FileBusinessTypeEnum.AI_ROLE_AVATAR);
    }

    // ==================== 公开方法：删除用户头像 ====================
    @Override
    public void deleteUserAvatar(String filePath, Integer userId) {
        deleteCommonImage(filePath, userId, FileBusinessTypeEnum.USER_AVATAR);
    }

    // ==================== 私有通用方法：核心删除逻辑（公式代码） ====================
    private void deleteCommonImage(String filePath, Integer userId, FileBusinessTypeEnum businessType) {
        // 1. 类型转换
        Long userIdLong = userId.longValue();
        FileOperationStatusEnum operationStatus = FileOperationStatusEnum.SUCCESS;
        String failReason = null;

        try {
            // 2. 调用基础文件删除服务
            fileDeleteService.deleteFile(filePath);
            log.info("图片删除成功，路径：{}，操作人：{}", filePath, userId);

        } catch (Exception e) {
            // 3. 异常捕获
            operationStatus = FileOperationStatusEnum.FAIL;
            failReason = "图片删除失败：" + e.getMessage();
            log.error("图片删除失败，路径：{}，异常：{}", filePath, e.getMessage());
            // 抛出异常（上层可捕获处理）
            throw new RuntimeException("图片删除失败", e);

        } finally {
            // 4. 记录删除日志（操作类型：DELETE）
            FileOperationLogDTO logDTO = new FileOperationLogDTO();
            logDTO.setBusinessId(userIdLong);
            logDTO.setBusinessType(businessType);
            logDTO.setFilePath(filePath);
            // 🔥 核心：操作类型为 删除
            logDTO.setOperationType(FileOperationTypeEnum.DELETE);
            logDTO.setOperationStatus(operationStatus);
            logDTO.setFailReason(failReason);
            logDTO.setOperatorId(userIdLong);

            // 插入日志
            fileOperationService.insertOperationLog(logDTO);
        }
    }
}
