package org.myfx.controls.aione.ServiceCommon.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.myfx.controls.aione.ServiceCommon.entity.FileOperationLog;

import java.util.List;

/**
 * 文件操作全量日志 Mapper
 */
@Mapper
public interface FileOperationLogMapper {

    /**
     * 查询所有记录
     */
    List<FileOperationLog> selectAll();

    /**
     * 分页查询
     */
    List<FileOperationLog> selectByPage(@Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    /**
     * 新增操作日志
     */
    int insertOperationLog(FileOperationLog log);

    /**
     * 根据ID删除
     */
    int deleteById(@Param("id") Long id);
}