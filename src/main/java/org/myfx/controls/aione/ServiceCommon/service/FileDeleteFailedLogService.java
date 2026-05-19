package org.myfx.controls.aione.ServiceCommon.service;

import org.myfx.controls.aione.ServiceCommon.entity.FileDeleteFailedLog;

import java.util.List;

/**
 * 文件删除失败日志Service接口（完整版）
 */
public interface FileDeleteFailedLogService {

    // 原有方法：插入失败日志
    boolean insertFailedLog(FileDeleteFailedLog failedLog);

    // ========== 新增：查询方法 ==========
    /**
     * 查询所有文件删除失败日志（基础版，数据量大时建议用分页）
     * @return 所有失败日志列表
     */
    List<FileDeleteFailedLog> selectAllFailedLogs();

    /**
     * 分页查询文件删除失败日志（推荐）
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页条数
     * @return 分页后的日志列表
     */
    List<FileDeleteFailedLog> selectFailedLogsByPage(int pageNum, int pageSize);

    // ========== 新增：删除方法 ==========
    /**
     * 根据ID删除单条失败日志
     * @param id 雪花ID
     * @return 是否删除成功
     */
    boolean deleteFailedLogById(Long id);

    /**
     * 删除已解决的失败日志（清理数据）
     * @param isResolved 解决状态（固定传1=已解决）
     * @return 删除的记录数
     */
    int deleteResolvedFailedLogs(Integer isResolved);
}