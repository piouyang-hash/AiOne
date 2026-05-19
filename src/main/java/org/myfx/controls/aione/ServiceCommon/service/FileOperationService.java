package org.myfx.controls.aione.ServiceCommon.service;

import org.myfx.controls.aione.ServiceCommon.entity.FileOperationLog;
import org.myfx.controls.aione.ServiceCommon.entity.dto.FileOperationLogDTO;

import java.util.List;

/**
 * 文件操作业务接口
 */
public interface FileOperationService {

    /**
     * 新增文件操作日志
     */
    void insertOperationLog(FileOperationLogDTO dto);

    /**
     * 查询所有操作日志
     */
    List<FileOperationLog> selectAllLog();

    /**
     * 根据ID删除日志
     */
    void deleteLogById(Long id);
}
