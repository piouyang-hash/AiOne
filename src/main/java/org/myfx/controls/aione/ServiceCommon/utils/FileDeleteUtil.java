package org.myfx.controls.aione.ServiceCommon.utils;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.ServiceCommon.entity.FileDeleteFailedLog;
import org.myfx.controls.aione.ServiceCommon.service.FileDeleteFailedLogService;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.FileDeleteBusinessTypeEnum;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Component
public class FileDeleteUtil {

    // ========== 重试配置（可抽为配置项，便于调整） ==========
    /** 最大重试次数 */
    private static final int MAX_RETRY_COUNT = 3;
    /** 重试间隔（毫秒） */
    private static final long RETRY_INTERVAL_MS = 500;

    // 重试总时长≈15秒，过期时间设30秒（留缓冲）
    private static final long EXPIRE_TIME_MS = 30 * 1000; // 30秒

    private final FileDeleteFailedLogService fileDeleteFailedLogService;

    @Resource(name = "fileDeleteExecutor")
    private Executor fileDeleteExecutor;

    // ========== 幂等核心：线程安全的缓存，记录正在处理的文件路径 ==========
    // key: 处理中的文件路径（trim后），value: 处理状态（true=处理中）
    private static final Map<String, Long> PROCESSING_FILES = new ConcurrentHashMap<>();

    /**
     * 删除单个文件（内部工具方法，仅供本类调用）
     * 【高频失败场景】：
     * 1. 文件被其他进程/线程占用（如流未关闭、杀毒软件锁定、Linux tail命令监控）；
     * 2. 文件权限异常（如只读属性、操作系统权限临时变更）；
     * 3. 网络文件系统（NFS/SMB）延迟或抖动（分布式存储场景）；
     * 4. 文件路径存在但实际为目录（非文件类型）。
     * 【注意事项】：
     * - 文件IO操作不属于数据库事务范畴，@Transactional无法保障其原子性，失败时无法回滚；
     * - 本方法仅做“尽力删除”，失败时仅记录日志不抛异常，避免阻断主流程。
     *
     * @param filePath 文件路径（null/空字符串会直接记录错误并返回）
     */
    private boolean deleteFile(String filePath) {
        // 1. 空路径校验
        if (filePath == null || filePath.isEmpty()) {
            log.error("删除错误。原因：路径是空值");
            return false; // 改为返回false，而非直接抛异常
        }
        File file = new File(filePath);
        // 2. 文件不存在
        if (!file.exists()) {
            log.warn("文件删除失败：文件不存在，路径={}", filePath);
            return false;
        }
        // 3. 路径是目录（非文件）
        if (file.exists() && !file.isFile()) {
            log.warn("文件删除失败：路径是目录而非文件，路径={}", filePath);
            return false;
        }
        // 4. 执行删除
        boolean deleted = file.delete();
        if (!deleted) {
            log.error("文件删除失败：文件被占用/权限不足，路径={}", filePath);
        }
        return deleted;
    }

    // ========== 新增：带重试的文件删除方法 ==========

    /**
     * 带重试的文件删除（核心重试逻辑）
     * 【测试专用】：强制抛出异常，模拟文件删除始终失败的场景
     * @param filePath 文件路径
     * @return true=删除成功，false=重试后仍失败
     */
    private boolean deleteFileWithRetry(String filePath) {
        int retryCount = 0;
        while (true) {
            try {
                // 恢复原逻辑：调用真实的文件删除方法，获取返回值
                boolean deleteSuccess = deleteFile(filePath);
                if (deleteSuccess) {
                    log.info("文件删除成功（重试次数：{}），路径={}", retryCount, filePath);
                    return true;
                }

                // 删除失败，进入重试逻辑（无需捕获异常，仅基于返回值判断）
                retryCount++;
                if (retryCount >= MAX_RETRY_COUNT) {
                    // 重试耗尽，返回失败
                    log.error("文件删除重试耗尽（最大{}次），最终失败，路径={}",
                            MAX_RETRY_COUNT, filePath);
                    return false;
                }
                // 未耗尽，等待后重试
                log.warn("文件删除失败（第{}次重试），路径={}，{}ms后重试",
                        retryCount, filePath, RETRY_INTERVAL_MS);
                try {
                    TimeUnit.MILLISECONDS.sleep(RETRY_INTERVAL_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("文件删除重试被中断，路径={}", filePath, ie);
                    return false;
                }
            } catch (Exception e) {
                // 捕获deleteFile执行过程中可能抛出的异常（如IO异常），同样触发重试
                retryCount++;
                if (retryCount >= MAX_RETRY_COUNT) {
                    log.error("文件删除重试耗尽（最大{}次），最终失败，路径={}，原因={}",
                            MAX_RETRY_COUNT, filePath, e.getMessage());
                    return false;
                }
                log.warn("文件删除执行异常（第{}次重试），路径={}，原因={}，{}ms后重试",
                        retryCount, filePath, e.getMessage(), RETRY_INTERVAL_MS);
                try {
                    TimeUnit.MILLISECONDS.sleep(RETRY_INTERVAL_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("文件删除重试被中断，路径={}", filePath, ie);
                    return false;
                }
            }
        }
    }


    // ========== 单个文件删除回调（替换为枚举） ==========
    /**
     * 注册事务提交后的单个文件删除回调（Spring事务同步）
     * 功能：在Spring事务提交成功后，异步删除指定文件；事务回滚则取消删除
     * 场景：用于数据库操作与单个文件删除的一致性保障（如删除头像、单文件资源时）
     * @param id          业务ID（如用户ID、书籍ID），用于日志追踪
     * @param filePath    待删除文件的路径（必填）
     * @param businessType 业务类型（枚举，必填），用于日志区分
     */
    // ========== 单个文件删除回调（替换为枚举 + 重试 + 失败入库） ==========
    // @Async("fileDeleteExecutor") // 指定专用线程池
    // 这样使用是绝对不行的，事务是线程绑定的
    // 当前无活跃Spring事务，跳过单个文件删除回调注册（业务ID：1，文件路径：D:\my-fileLog-db\20251227220404974.txt）
    // 注入自定义线程池（替代静态变量，更规范）
    // ========== 移除@Async！回调注册必须同步执行 ==========
    public void registerSingleFileDeleteAfterCommit (
            Integer id,
            String filePath,
            FileDeleteBusinessTypeEnum businessType) {
        // 1. 参数校验
        if (filePath == null || filePath.trim().isEmpty()) {
            log.warn("文件路径为空，跳过单个文件删除回调注册（业务ID：{}）", id);
            return;
        }
        if (businessType == null) {
            log.warn("业务类型枚举为空，跳过单个文件删除回调注册（业务ID：{}，文件路径：{}）", id, filePath);
            return;
        }
        String realFilePath = filePath.trim();

        // 2. 同步注册回调（主线程，保留事务上下文）
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    // ========== 异步执行删除逻辑（加幂等校验） ==========
                    CompletableFuture.runAsync(() -> {
                        String businessDesc = businessType.getDesc();
                        // 幂等校验第一步：尝试标记文件为“处理中”
                        Long oldTime = PROCESSING_FILES.putIfAbsent(realFilePath, System.currentTimeMillis());
                        boolean isFirstProcess = oldTime == null; // 无旧值=首次处理
                        if (!isFirstProcess) {
                            // 已在处理中，直接跳过，实现幂等
                            log.warn("【幂等拦截】{}业务ID[{}]的文件{}正在重试删除中，跳过重复执行",
                                    businessDesc, id, realFilePath);
                            return;
                        }

                        // 幂等校验通过，执行删除逻辑（最终块清理标记）
                        try {
                            if (status == TransactionSynchronization.STATUS_COMMITTED) {
                                log.info("【异步】开始执行{}业务ID[{}]的文件删除重试逻辑，路径={}",
                                        businessDesc, id, realFilePath);
                                try {
                                    boolean deleteSuccess = deleteFileWithRetry(realFilePath);
                                    if (!deleteSuccess) {
                                        log.warn("【异步】【{}】业务ID[{}]文件删除重试{}次失败，准备写入失败日志，路径={}",
                                                businessDesc, id, MAX_RETRY_COUNT, realFilePath);
                                        saveDeleteFailedLog(id, realFilePath, businessType);
                                    } else {
                                        log.info("【异步】事务提交成功，已删除【{}】业务ID[{}]的物理文件：{}",
                                                businessDesc, id, realFilePath);
                                    }
                                } catch (Exception e) {
                                    log.error("【异步】【{}】业务ID[{}]文件删除重试时异常，路径={}",
                                            businessDesc, id, realFilePath, e);
                                    saveDeleteFailedLog(id, realFilePath, businessType);
                                }
                            } else if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                                log.warn("【异步】事务回滚，取消删除【{}】业务ID[{}]的物理文件：{}",
                                        businessType.getDesc(), id, realFilePath);
                            } else {
                                log.warn("【异步】事务未知状态[{}]，取消删除【{}】业务ID[{}]的物理文件：{}",
                                        status, businessType.getDesc(), id, realFilePath);
                            }
                        } finally {
                            // ========== 幂等核心：无论成功/失败/异常，都清理处理标记 ==========
                            PROCESSING_FILES.remove(realFilePath);
                            log.info("【幂等清理】{}业务ID[{}]的文件{}处理标记已清理，路径={}",
                                    businessDesc, id, realFilePath, realFilePath);
                        }
                    }, fileDeleteExecutor);
                }
            });
            log.info("回调注册成功：{}业务ID[{}]，文件路径={}", businessType.getDesc(), id, realFilePath);
        } else {
            log.warn("当前无活跃Spring事务，跳过单个文件删除回调注册（业务ID：{}，文件路径：{}）", id, realFilePath);
        }
    }

    // ========== 公用文件删除回调（异步+幂等+重试+失败入库） ==========
    /**
     * 注册事务提交后的文件删除回调（Spring事务同步）
     * 功能：在Spring事务提交成功后，异步删除指定的文件和封面文件；事务回滚则取消删除
     * 场景：用于数据库操作与文件删除的一致性保障（如删除书籍时，先删库再删文件）
     * @param id          业务ID（如书籍ID），用于日志追踪
     * @param filePath    主文件路径（必填，如EPUB文件路径）
     * @param coverPath   封面文件路径（可选，可为null）
     * @param businessType 业务类型（枚举，必填），用于日志区分
     */
    public void registerFileDeleteAfterCommit (
            Integer id,
            String filePath,
            String coverPath,
            FileDeleteBusinessTypeEnum businessType) {
        // 1. 参数校验：主路径+枚举非空
        if (filePath == null || filePath.trim().isEmpty()) {
            log.warn("主文件路径为空，跳过多文件删除回调注册（业务ID：{}）", id);
            return;
        }
        if (businessType == null) {
            log.warn("业务类型枚举为空，跳过多文件删除回调注册（业务ID：{}，主文件路径：{}）", id, filePath);
            return;
        }
        String realFilePath = filePath.trim();
        String realCoverPath = coverPath == null ? null : coverPath.trim();

        // 2. 同步注册回调（主线程保留事务上下文）
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    // ========== 异步执行主文件+封面文件删除逻辑（核心改造） ==========
                    CompletableFuture.runAsync(() -> {
                        String businessDesc = businessType.getDesc();
                        // 记录需要清理的标记（主文件+封面文件），便于finally统一清理
                        String[] needCleanPaths = new String[]{realFilePath, realCoverPath};

                        try {
                            if (status == TransactionSynchronization.STATUS_COMMITTED) {
                                log.info("【异步】开始执行{}业务ID[{}]的多文件删除逻辑：主文件={}、封面文件={}",
                                        businessDesc, id, realFilePath, realCoverPath);

                                // ========== 处理主文件删除（必填，幂等校验） ==========
                                boolean mainFileIsFirst = PROCESSING_FILES.putIfAbsent(realFilePath, System.currentTimeMillis()) == null;
                                if (!mainFileIsFirst) {
                                    log.warn("【幂等拦截】{}业务ID[{}]的主文件{}正在重试删除中，跳过重复执行",
                                            businessDesc, id, realFilePath);
                                } else {
                                    boolean mainFileSuccess = false;
                                    try {
                                        mainFileSuccess = deleteFileWithRetry(realFilePath);
                                        if (mainFileSuccess) {
                                            log.info("【异步】事务提交成功，已删除【{}】业务ID[{}]的主物理文件：{}",
                                                    businessDesc, id, realFilePath);
                                        } else {
                                            log.warn("【异步】【{}】业务ID[{}]主文件删除重试{}次失败，准备写入失败日志，路径={}",
                                                    businessDesc, id, MAX_RETRY_COUNT, realFilePath);
                                            saveDeleteFailedLog(id, realFilePath, businessType);
                                        }
                                    } catch (Exception e) {
                                        log.error("【异步】【{}】业务ID[{}]主文件删除重试时异常，路径={}",
                                                businessDesc, id, realFilePath, e);
                                        saveDeleteFailedLog(id, realFilePath, businessType);
                                    }
                                }

                                // ========== 处理封面文件删除（可选，幂等校验） ==========
                                if (realCoverPath != null && !realCoverPath.isEmpty()) {
                                    boolean coverFileIsFirst = PROCESSING_FILES.putIfAbsent(realCoverPath, System.currentTimeMillis()) == null;
                                    if (!coverFileIsFirst) {
                                        log.warn("【幂等拦截】{}业务ID[{}]的封面文件{}正在重试删除中，跳过重复执行",
                                                businessDesc, id, realCoverPath);
                                    } else {
                                        boolean coverFileSuccess = false;
                                        try {
                                            coverFileSuccess = deleteFileWithRetry(realCoverPath);
                                            if (coverFileSuccess) {
                                                log.info("【异步】事务提交成功，已删除【{}】业务ID[{}]的封面物理文件：{}",
                                                        businessDesc, id, realCoverPath);
                                            } else {
                                                log.warn("【异步】【{}】业务ID[{}]封面文件删除重试{}次失败，准备写入失败日志，路径={}",
                                                        businessDesc, id, MAX_RETRY_COUNT, realCoverPath);
                                                saveDeleteFailedLog(id, realCoverPath, businessType);
                                            }
                                        } catch (Exception e) {
                                            log.error("【异步】【{}】业务ID[{}]封面文件删除重试时异常，路径={}",
                                                    businessDesc, id, realCoverPath, e);
                                            saveDeleteFailedLog(id, realCoverPath, businessType);
                                        }
                                    }
                                }

                            } else if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                                // 事务回滚：仅记录日志
                                log.warn("【异步】事务回滚，取消删除【{}】业务ID[{}]的物理文件：主文件={}、封面文件={}",
                                        businessDesc, id, realFilePath, realCoverPath);
                            } else {
                                // 未知事务状态：保守处理
                                log.warn("【异步】事务未知状态[{}]，取消删除【{}】业务ID[{}]的物理文件",
                                        status, businessDesc, id);
                            }
                        } finally {
                            // ========== 统一清理幂等标记（主文件+封面文件） ==========
                            for (String path : needCleanPaths) {
                                if (path != null && !path.isEmpty()) {
                                    PROCESSING_FILES.remove(path);
                                    log.info("【幂等清理】{}业务ID[{}]的文件{}处理标记已清理",
                                            businessDesc, id, path);
                                }
                            }
                        }
                    }, fileDeleteExecutor); // 指定自定义线程池
                }
            });
            log.info("多文件删除回调注册成功：{}业务ID[{}]，主文件={}、封面文件={}",
                    businessType.getDesc(), id, realFilePath, realCoverPath);
        } else {
            log.warn("当前无活跃Spring事务，跳过多文件删除回调注册（业务ID：{}）", id);
        }
    }


    /**
     * 构建失败日志并写入数据库（最终兜底机制）
     * @param businessId 业务ID（用户ID/书籍ID等）
     * @param filePath 失败的文件路径
     * @param businessType 业务类型（枚举，必填）
     */
    private void saveDeleteFailedLog(Integer businessId, String filePath, FileDeleteBusinessTypeEnum businessType) {
        // 1. 前置校验：核心参数非空
        Assert.notNull(fileDeleteFailedLogService, "FileDeleteFailedLogService未注入，无法写入失败日志");
        if (businessId == null || businessId <= 0) {
            log.warn("写入失败日志跳过：业务ID无效，filePath={}", filePath);
            return;
        }
        if (filePath == null || filePath.trim().isEmpty()) {
            log.warn("写入失败日志跳过：文件路径为空，businessId={}", businessId);
            return;
        }
        // 枚举非空校验+兜底
        if (businessType == null) {
            log.warn("写入失败日志警告：业务类型枚举为空，businessId={}，默认使用未知类型", businessId);
            businessType = FileDeleteBusinessTypeEnum.TEST; // 兜底：默认测试类型，也可新增UNKNOWN枚举项
        }

        // 2. 构建失败日志实体
        FileDeleteFailedLog failedLog = new FileDeleteFailedLog();
        failedLog.setBusinessId(businessId); // 业务ID（用户ID/书籍ID）
        failedLog.setFilePath(filePath.trim()); // 失败的文件路径（去空格）
        failedLog.setBusinessType(businessType); // 枚举直接取编码（核心修改）
        failedLog.setFailCount(MAX_RETRY_COUNT); // 失败次数=最大重试次数
        // 日志中加入枚举描述，更易理解
        failedLog.setFailReason(String.format("【%s】文件删除重试%d次仍失败（可能原因：文件被占用、权限不足、文件不存在）",
                businessType.getDesc(), MAX_RETRY_COUNT));
        failedLog.setCreateTime(LocalDateTime.now()); // 创建时间

        // 3. 调用Service写入数据库
        boolean insertSuccess = fileDeleteFailedLogService.insertFailedLog(failedLog);
        if (insertSuccess) {
            log.info("文件删除失败日志写入数据库成功，业务类型={}，业务ID={}，文件路径={}",
                    businessType.getDesc(), businessId, filePath);
        } else {
            log.error("文件删除失败日志写入数据库失败，业务类型={}，业务ID={}，文件路径={}",
                    businessType.getDesc(), businessId, filePath);
        }
    }


    // ========== 定时清理过期标记（每分钟执行一次） ==========
    @Scheduled(fixedRate = 60 * 1000) // fixedRate：每隔60秒执行一次
    public void cleanExpiredProcessingFiles() {
        long now = System.currentTimeMillis();
        // 统计清理前的数量（便于日志排查）
        int beforeCount = PROCESSING_FILES.size();

        // 遍历并删除过期的标记
        PROCESSING_FILES.entrySet().removeIf(entry -> {
            String filePath = entry.getKey();
            long startTime = entry.getValue();
            // 判断是否过期：当前时间 - 处理开始时间 > 过期时间
            boolean isExpired = (now - startTime) > EXPIRE_TIME_MS;

            if (isExpired) {
                log.warn("【幂等过期清理】文件路径{}处理标记已过期（开始时间：{}，当前时间：{}），强制清理",
                        filePath, startTime, now);
            }
            return isExpired;
        });

        // 日志输出清理结果（便于监控）
        int afterCount = PROCESSING_FILES.size();
        int cleanCount = beforeCount - afterCount;
        if (cleanCount > 0) {
            log.info("【幂等过期清理】完成，共清理{}个过期标记，剩余{}个活跃标记", cleanCount, afterCount);
        } else {
            log.debug("【幂等过期清理】完成，无过期标记需要清理，当前活跃标记数：{}", afterCount);
        }
    }


}
