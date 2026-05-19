package org.myfx.controls.aione.ServiceCommon.service;

import org.myfx.controls.aione.ServiceCommon.entity.FileExceptionHandler;
import org.myfx.controls.aione.ServiceCommon.entity.dto.FileExceptionHandlerDTO;

import java.util.List;

/**
 * 文件异常处理 业务接口
 */
public interface FileExceptionService {

    /**
     * 新增文件异常记录
     */
    void insertExceptionHandler(FileExceptionHandlerDTO dto);

    /**
     * 查询所有异常记录
     */
    List<FileExceptionHandler> selectAllException();

    /**
     * 根据ID删除异常记录
     */
    void deleteExceptionById(Long id);
}