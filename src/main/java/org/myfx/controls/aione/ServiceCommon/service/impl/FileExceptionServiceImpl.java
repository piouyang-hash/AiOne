package org.myfx.controls.aione.ServiceCommon.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.ServiceCommon.entity.FileExceptionHandler;
import org.myfx.controls.aione.ServiceCommon.entity.dto.FileExceptionHandlerDTO;
import org.myfx.controls.aione.ServiceCommon.mapper.FileExceptionHandlerMapper;
import org.myfx.controls.aione.ServiceCommon.service.FileExceptionService;
import org.myfx.controls.aione.ServiceCommon.utils.SnowflakeGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * 文件异常处理 业务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FileExceptionServiceImpl implements FileExceptionService {

    private final FileExceptionHandlerMapper fileExceptionHandlerMapper;

    // ==================== 核心：新增异常记录 ====================
    @Override
    public void insertExceptionHandler(FileExceptionHandlerDTO dto) {
        // 1. 断言参数校验（和之前完全一致）
        Assert.notNull(dto.getLogId(), "关联操作日志ID不能为空");
        Assert.notNull(dto.getBusinessId(), "业务ID不能为空");
        Assert.notNull(dto.getBusinessType(), "业务类型不能为空");
        Assert.hasText(dto.getFilePath(), "异常文件路径不能为空");
        Assert.notNull(dto.getOperationType(), "操作类型不能为空");
        Assert.notNull(dto.getRetryCount(), "重试次数不能为空");
        Assert.notNull(dto.getMaxRetry(), "最大重试次数不能为空");
        Assert.hasText(dto.getFinalFailReason(), "最终失败原因不能为空");
        Assert.notNull(dto.getHandleStatus(), "处理状态不能为空");

        // 2. 生成雪花ID
        Long snowflakeId = SnowflakeGenerator.generateId();

        // 3. DTO拷贝到实体类
        FileExceptionHandler handler = new FileExceptionHandler();
        BeanUtils.copyProperties(dto, handler);
        // 设置主键ID
        handler.setId(snowflakeId);

        // 4. 调用Mapper插入
        fileExceptionHandlerMapper.insertExceptionHandler(handler);
    }

    // ==================== 查询所有异常记录 ====================
    @Override
    public List<FileExceptionHandler> selectAllException() {
        return fileExceptionHandlerMapper.selectAll();
    }

    // ==================== 根据ID删除 ====================
    @Override
    public void deleteExceptionById(Long id) {
        Assert.notNull(id, "异常记录ID不能为空");
        fileExceptionHandlerMapper.deleteById(id);
    }
}