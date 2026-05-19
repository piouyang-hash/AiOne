package org.myfx.controls.aione.ServiceCommon.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.myfx.controls.aione.ServiceCommon.entity.FileDeleteFailedLog;

import java.util.List;
import java.util.Map;

/**
 * 文件删除失败日志Mapper接口
 */
@Mapper
public interface FileDeleteFailedLogMapper {

    /**
     * 插入文件删除失败日志（雪花ID需提前生成并传入）
     * @param log 日志实体（id字段需赋值雪花ID）
     * @return 影响行数
     */
    int insertFailedLog(FileDeleteFailedLog log);

    // 新增：查询所有记录
    List<FileDeleteFailedLog> selectAll();

    // 新增：分页查询（推荐）
    List<FileDeleteFailedLog> selectByPage(Map<String, Integer> pageParams);

    /**
     * 根据ID删除日志
     * @param id 雪花ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 删除已解决的日志（清理数据）
     * @param isResolved 解决状态（1=已解决）
     * @return 影响行数
     */
    int deleteResolvedLogs(@Param("isResolved") Integer isResolved);
}