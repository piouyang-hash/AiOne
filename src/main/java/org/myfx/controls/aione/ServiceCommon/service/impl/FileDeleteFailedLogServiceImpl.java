package org.myfx.controls.aione.ServiceCommon.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.ServiceCommon.entity.FileDeleteFailedLog;
import org.myfx.controls.aione.ServiceCommon.mapper.FileDeleteFailedLogMapper;
import org.myfx.controls.aione.ServiceCommon.service.FileDeleteFailedLogService;
import org.myfx.controls.aione.ServiceCommon.utils.SnowflakeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileDeleteFailedLogServiceImpl implements FileDeleteFailedLogService {

    private final FileDeleteFailedLogMapper fileDeleteFailedLogMapper;

    // ========== 原有方法：插入失败日志 ==========
    @Override
    public boolean insertFailedLog(FileDeleteFailedLog failedLog) {
        if (failedLog == null) {
            log.error("插入文件删除失败日志失败：日志实体为null");
            return false;
        }
        if (ObjectUtils.isEmpty(failedLog.getFilePath())) {
            log.error("插入文件删除失败日志失败：文件路径为空");
            return false;
        }
        if (failedLog.getBusinessType() == null) {
            log.error("插入文件删除失败日志失败：业务类型编码为空，文件路径={}", failedLog.getFilePath());
            return false;
        }

        // 雪花ID生成（替换为你的工具）
        Long snowflakeId = SnowflakeGenerator.generateId();
        failedLog.setId(snowflakeId);
        // 强制未解决
        failedLog.setIsResolved(0);
        // 默认值补充
        if (failedLog.getFailCount() == null || failedLog.getFailCount() <= 0) {
            failedLog.setFailCount(1);
        }
        if (failedLog.getCreateTime() == null) {
            failedLog.setCreateTime(LocalDateTime.now());
        }

        int affectRows = fileDeleteFailedLogMapper.insertFailedLog(failedLog);
        if (affectRows > 0) {
            log.info("插入文件删除失败日志成功，雪花ID={}，文件路径={}", snowflakeId, failedLog.getFilePath());
            return true;
        } else {
            log.warn("插入文件删除失败日志失败：数据库无影响行数，雪花ID={}，文件路径={}", snowflakeId, failedLog.getFilePath());
            return false;
        }
    }

    // ========== 新增：查询所有日志 ==========
    @Override
    public List<FileDeleteFailedLog> selectAllFailedLogs() {

        List<FileDeleteFailedLog> logList = fileDeleteFailedLogMapper.selectAll();
        log.info("查询所有文件删除失败日志成功，共{}条记录", logList.size());
        return logList;

    }

    // ========== 新增：分页查询日志 ==========
    @Override
    public List<FileDeleteFailedLog> selectFailedLogsByPage(int pageNum, int pageSize) {
        // 参数校验：页码≥1，每页条数≥1且≤100（避免单次查太多）
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 20; // 默认每页20条
        }
        // 计算偏移量：LIMIT offset, pageSize
        int offset = (pageNum - 1) * pageSize;


        Map<String, Integer> pageParams = new HashMap<>();
        pageParams.put("offset", offset);
        pageParams.put("pageSize", pageSize);
        List<FileDeleteFailedLog> logList = fileDeleteFailedLogMapper.selectByPage(pageParams);
        log.info("分页查询文件删除失败日志成功，页码={}，每页条数={}，查询到{}条记录", pageNum, pageSize, logList.size());
        return logList;

    }

    // ========== 新增：根据ID删除日志 ==========
    @Override
    public boolean deleteFailedLogById(Long id) {
        // 参数校验
        if (id == null || id <= 0) {
            log.error("删除文件删除失败日志失败：ID为空或无效");
            return false;
        }


        int affectRows = fileDeleteFailedLogMapper.deleteById(id);
        if (affectRows > 0) {
            log.info("删除文件删除失败日志成功，ID={}", id);
            return true;
        } else {
            log.warn("删除文件删除失败日志失败：ID={} 不存在或已被删除", id);
            return false;
        }

    }

    // ========== 新增：删除已解决的日志 ==========
    @Override
    public int deleteResolvedFailedLogs(Integer isResolved) {
        // 参数校验：固定只允许传1（已解决），避免误删未解决的
        if (isResolved == null || isResolved != 1) {
            log.error("删除已解决日志失败：仅允许删除isResolved=1的记录，传入值={}", isResolved);
            return 0;
        }

        int affectRows = fileDeleteFailedLogMapper.deleteResolvedLogs(isResolved);
        log.info("删除已解决的文件删除失败日志成功，共删除{}条记录", affectRows);
        return affectRows;

    }
}