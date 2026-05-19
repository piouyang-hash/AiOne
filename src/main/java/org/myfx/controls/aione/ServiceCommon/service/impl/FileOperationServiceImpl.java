package org.myfx.controls.aione.ServiceCommon.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.ServiceCommon.entity.FileOperationLog;
import org.myfx.controls.aione.ServiceCommon.entity.dto.FileOperationLogDTO;
import org.myfx.controls.aione.ServiceCommon.mapper.FileOperationLogMapper;
import org.myfx.controls.aione.ServiceCommon.service.FileOperationService;
import org.myfx.controls.aione.ServiceCommon.utils.SnowflakeGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * 文件操作业务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FileOperationServiceImpl implements FileOperationService {

    private final FileOperationLogMapper fileOperationLogMapper;

    // ==================== 核心：新增日志 ====================
    @Override
    public void insertOperationLog(FileOperationLogDTO dto) {
        // 1. 参数校验（简化if，用Spring断言）
        Assert.notNull(dto.getBusinessId(), "业务ID不能为空");
        Assert.notNull(dto.getBusinessType(), "业务类型不能为空");
        Assert.hasText(dto.getFilePath(), "文件路径不能为空");
        Assert.notNull(dto.getOperationType(), "操作类型不能为空");
        Assert.notNull(dto.getOperationStatus(), "操作状态不能为空");
        Assert.notNull(dto.getOperatorId(), "操作人ID不能为空");

        // 2. 生成雪花ID
        Long snowflakeId = SnowflakeGenerator.generateId();

        // 3. Spring自带工具类：DTO拷贝到实体类
        FileOperationLog logEntity = new FileOperationLog();
        BeanUtils.copyProperties(dto, logEntity);
        // 设置主键ID
        logEntity.setId(snowflakeId);

        // 4. 调用Mapper插入数据
        fileOperationLogMapper.insertOperationLog(logEntity);
    }

    // ==================== 查询全部 ====================
    @Override
    public List<FileOperationLog> selectAllLog() {
        return fileOperationLogMapper.selectAll();
    }

    // ==================== 根据ID删除 ====================
    @Override
    public void deleteLogById(Long id) {
        Assert.notNull(id, "日志ID不能为空");
        fileOperationLogMapper.deleteById(id);
    }
}