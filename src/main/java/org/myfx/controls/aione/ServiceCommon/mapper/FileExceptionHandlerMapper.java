package org.myfx.controls.aione.ServiceCommon.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.myfx.controls.aione.ServiceCommon.entity.FileExceptionHandler;

import java.util.List;

/**
 * 文件异常处理 Mapper
 */
@Mapper
public interface FileExceptionHandlerMapper {

    /**
     * 查询所有记录
     */
    List<FileExceptionHandler> selectAll();

    /**
     * 分页查询
     */
    List<FileExceptionHandler> selectByPage(@Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    /**
     * 新增异常记录
     */
    int insertExceptionHandler(FileExceptionHandler handler);

    /**
     * 根据ID删除
     */
    int deleteById(@Param("id") Long id);
}