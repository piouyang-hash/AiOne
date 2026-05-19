package org.myfx.controls.aione.ServiceCommon.utils;

import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.ServiceCommon.context.UserContext;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * 文件存储工具类（支持自定义文件后缀，提供常用后缀常量）
 */
@Slf4j
public class FileStorageUtil {

    // 可外部访问的文件后缀常量
    /** 无后缀（值为null，用于不需要添加后缀的场景） */
    public static final String NO_SUFFIX = null;
    /** EPUB文件后缀（.epub） */
    public static final String EPUB_SUFFIX = ".epub";
    /** 图片标记后缀（.image，仅作为标记，实际类型由魔数判断） */
    public static final String IMAGE_MARKER_SUFFIX = ".image";


    /**
     * 生成文件存储的完整路径（支持图片魔数识别，复用基础重载方法）
     * 包含图片魔数识别、后缀标准化，最终调用基础方法完成路径生成
     *
     * @param data      二进制数据（仅用于图片魔数识别，非图片可传null）
     * @param fileName  原始文件名（不含后缀）
     * @param targetDir 目标存储目录
     * @param suffix    文件后缀（如".epub"或IMAGE_MARKER_SUFFIX）
     * @return 文件的完整File对象（包含路径+文件名）；若目录创建失败则返回null
     * @throws IOException 文件名非法或魔数识别失败时抛出
     */
    public static File generateFilePath(byte[] data, String fileName, String targetDir, String suffix) throws IOException {
        // 1. 净化文件名（避免非法字符）
        String sanitizedFileName = sanitizeFileName(fileName);
        if (sanitizedFileName.isEmpty()) {
            throw new IOException("文件名净化后为空，无法生成路径");
        }

        // 2. 处理实际后缀（支持图片魔数识别）
        String actualSuffix = suffix;
        if (IMAGE_MARKER_SUFFIX.equals(suffix)) {
            if (data == null) {
                throw new IOException("图片魔数识别失败：数据为空");
            }
            actualSuffix = FileMagicNumberUtil.getRealImageSuffix(data);
            if (actualSuffix == null) {
                throw new IOException("无法识别的图片类型（魔数不匹配已知格式）");
            }
        }

        // 3. 标准化后缀格式（确保以"."开头）
        String normalizedSuffix = null;
        if (actualSuffix != null && !actualSuffix.isEmpty()) {
            normalizedSuffix = actualSuffix.startsWith(".") ? actualSuffix : "." + actualSuffix;
        }

        // 4. 直接调用基础重载方法，复用路径生成+目录创建逻辑
        return generateFilePath(sanitizedFileName, targetDir, normalizedSuffix);
    }

    /**
     * 生成文件存储路径（适配MultipartFile场景，与storeFile(MultipartFile...)配套）
     * 仅计算路径不创建文件，但会自动确保父目录存在（不存在则创建，存在则跳过）
     *
     * @param fileName  基础文件名（无需带后缀，后缀通过suffix参数指定）
     * @param targetDir 目标存储目录（如BookServiceConstant中的目录）
     * @param suffix    自定义文件后缀（可传NO_SUFFIX/null表示无后缀，或EPUB_SUFFIX等常量）
     * @return 文件的完整File对象（包含路径+文件名，仅为路径抽象，不实际创建文件）
     */
    public static File generateFilePath(String fileName, String targetDir, String suffix) {
        // 1. 净化文件名：与storeFile方法复用相同的净化逻辑，保证路径一致
        String sanitizedFileName = sanitizeFileName(fileName);

        // 2. 拼接完整文件名：与storeFile方法逻辑一致（处理suffix参数）
        String fullFileName;
        if (suffix != null) {
            fullFileName = sanitizedFileName + suffix;
        } else {
            fullFileName = sanitizedFileName;
        }

        // 3. 构建完整路径的File对象（仅抽象路径，不创建文件）
        File targetFile = new File(targetDir, fullFileName);

        // 4. 确保父目录存在：不存在则创建所有层级目录，存在则不做任何操作
        File parentDir = targetFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            boolean isDirCreated = parentDir.mkdirs();
            if (!isDirCreated) { // 目录创建失败时
                log.error("Failed to create parent directories for path: {}", parentDir.getAbsolutePath());
            }
        }

        return targetFile;
    }


    /**
     * 核心方法：将文件存储到指定目录，支持自定义后缀（可传常量或null）
     * @param file 待存储的文件（前端上传的MultipartFile）(Spring专门用于处理 HTTP 协议中上传的文件)
     * @param fileName 基础文件名（无需带后缀，后缀通过suffix参数指定）
     * @param targetDir 目标存储目录（如BookServiceConstant中的目录）
     * @param suffix 自定义文件后缀（可传NO_SUFFIX/null表示无后缀，或EPUB_SUFFIX等常量）
     * @return 存储后的完整文件路径（如 D:/my-book-db/public-books/xxx.epub 或 xxx）
     * @throws IOException 存储失败时抛出IO异常（如目录不存在、权限不足等）
     */
    private static String storeFile(MultipartFile file, String fileName, String targetDir, String suffix) throws IOException {
        // 1. 调用generateFilePath生成路径（复用路径生成逻辑，避免重复代码）
        File targetFile = generateFilePath(fileName, targetDir, suffix);

        // 2. 存储文件
        file.transferTo(targetFile);

        // 3. 返回完整路径
        return targetFile.getAbsolutePath();
    }

    /**
     * 重载方法：处理本地File类型文件的存储（复用generateFilePath统一路径生成）
     * @param file 本地文件（java.io.File）
     * @param fileName 文件名（不含后缀）
     * @param targetDir 目标存储目录
     * @param suffix 文件后缀（如".jpg"）
     * @return 存储后的文件绝对路径
     * @throws IOException 存储失败时抛出
     */
    private static String storeFile(File file, String fileName, String targetDir, String suffix) throws IOException {
        // 1. 复用generateFilePath生成目标文件路径（统一路径规则，消除重复代码）
        File targetFile = generateFilePath(fileName, targetDir, suffix);

        // 2. 存储文件（本地文件复制）
        java.nio.file.Files.copy(file.toPath(), targetFile.toPath());

        // 3. 返回完整绝对路径
        return targetFile.getAbsolutePath();
    }


    /**
     * 重载方法：将二进制字节数组存储为文件（核心存储逻辑）
     *
     * @param data      二进制字节数组
     * @param fileName  文件名（不含后缀）
     * @param targetDir 目标存储目录
     * @param suffix    文件后缀（如".jpg"或IMAGE_MARKER_SUFFIX）
     * @return 存储后的文件绝对路径
     * @throws IOException 存储失败时抛出（目录创建失败、写入失败等）
     */
    private static String storeFile(byte[] data, String fileName, String targetDir, String suffix) throws IOException {
        // 1. 调用公共方法生成文件路径
        File targetFile = generateFilePath(data, fileName, targetDir, suffix);

        // 2. 将二进制数组写入目标文件
        java.nio.file.Files.write(targetFile.toPath(), data);

        // 3. 返回完整绝对路径
        return targetFile.getAbsolutePath();
    }


    /**
     * 注册事务提交后的文件存储回调（入库成功后执行）
     * @param businessId 业务ID（如书籍ID、文档ID等）
     * @param mainFile 主文件（前端上传的MultipartFile，如EPUB、PDF等）
     * @param mainFileName 主文件名称（不含后缀）
     * @param mainDir 主文件存储目录
     * @param mainSuffix 主文件后缀（如.epub、.pdf）
     * @param attachBytes 附属文件字节数组（如封面图、缩略图等）
     * @param attachFileName 附属文件名称（不含后缀）
     * @param attachDir 附属文件存储目录
     * @param attachSuffix 附属文件后缀（如.jpg、.png）
     * @param businessType 业务类型（1=公共资源，0=私有资源）
     * @param businessDesc 业务描述（如"书籍"、"文档"，用于日志区分）
     */
    public static void registerFileStoreAfterCommit(
            Integer businessId,
            MultipartFile mainFile,
            String mainFileName,
            String mainDir,
            String mainSuffix,
            byte[] attachBytes,
            String attachFileName,
            String attachDir,
            String attachSuffix,
            Integer businessType,
            String businessDesc) {
        // 1. 处理业务类型和用户ID（私有资源专属）
        String businessPrefix;
        if (businessType == 0) {
            // 私有资源：获取用户ID并拼接前缀
            Integer userId = UserContext.getUserId();
            businessPrefix = userId != null ? "用户(ID:" + userId + ")-私有" + businessDesc : "未知用户-私有" + businessDesc;
        } else if (businessType == 1) {
            // 公共资源：直接用业务描述
            businessPrefix = "公共" + businessDesc;
        } else {
            businessPrefix = "未知类型" + businessDesc;
        }

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == TransactionSynchronization.STATUS_COMMITTED) {
                        try {
                            String mainFilePath = storeFile(mainFile, mainFileName, mainDir, mainSuffix);
                            String attachFilePath = storeFile(attachBytes, attachFileName, attachDir, attachSuffix);

                            // 成功日志：通用化描述
                            log.info("{}ID[{}]文件存储成功：主文件={}, 附属文件={}", businessPrefix, businessId, mainFilePath, attachFilePath);
                        } catch (Exception e) {
                            // 失败日志：通用化描述
                            log.error("{}ID[{}]文件存储失败，需人工处理！详情：{}", businessPrefix, businessId, e.getMessage(), e);
                        }
                    } else {
                        // 回滚日志：通用化描述
                        log.warn("{}ID[{}]入库事务回滚，取消文件存储", businessPrefix, businessId);
                    }
                }
            });
        } else {
            // 无活跃事务时打印warn日志
            log.warn("当前无活跃Spring事务，跳过文件删除回调注册（业务ID：{}）", businessId);
        }
    }

    /**
     * 注册事务提交后的单文件存储回调（仅主文件，无附属文件）
     * @param businessId 业务ID（如书籍ID、文档ID等）
     * @param mainFile 主文件（前端上传的MultipartFile，如EPUB、PDF等）
     * @param mainFileName 主文件名称（不含后缀）
     * @param mainDir 主文件存储目录
     * @param mainSuffix 主文件后缀（如.epub、.pdf）
     * @param businessType 业务类型（1=公共资源，0=私有资源）
     * @param businessDesc 业务描述（如"书籍"、"文档"，用于日志区分）
     */
    public static void registerFileStoreAfterCommit(
            Integer businessId,
            MultipartFile mainFile,
            String mainFileName,
            String mainDir,
            String mainSuffix,
            Integer businessType,
            String businessDesc) {
        // 1. 处理业务类型和用户ID（私有资源专属）
        String businessPrefix;
        if (businessType == 0) {
            // 私有资源：获取用户ID并拼接前缀
            Integer userId = UserContext.getUserId();
            businessPrefix = userId != null ? "用户(ID:" + userId + ")-私有" + businessDesc : "未知用户-私有" + businessDesc;
        } else if (businessType == 1) {
            // 公共资源：直接用业务描述
            businessPrefix = "公共" + businessDesc;
        } else {
            businessPrefix = "未知类型" + businessDesc;
        }

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == TransactionSynchronization.STATUS_COMMITTED) {
                        try {
                            // 调用单文件存储方法
                            String mainFilePath = storeFile(mainFile, mainFileName, mainDir, mainSuffix);

                            // 成功日志：仅描述主文件
                            log.info("{}ID[{}]单文件存储成功：主文件={}", businessPrefix, businessId, mainFilePath);
                        } catch (Exception e) {
                            // 失败日志：适配单文件场景
                            log.error("{}ID[{}]单文件存储失败，需人工处理！详情：{}", businessPrefix, businessId, e.getMessage(), e);
                        }
                    } else {
                        // 回滚日志：通用化描述
                        log.warn("{}ID[{}]入库事务回滚，取消单文件存储", businessPrefix, businessId);
                    }
                }
            });
        } else {
            // 无活跃事务时打印warn日志
            log.warn("当前无活跃Spring事务，跳过文件删除回调注册（业务ID：{}）", businessId);
        }
    }

    /**
     * 净化文件名：移除/替换操作系统禁止的特殊字符
     * 禁止字符包括：\/:*?"<>|（Windows系统禁止），其他系统兼容这些限制
     */
    private static String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            // 处理空文件名（避免生成空字符串，用默认名）
            return "unknown_file";
        }
        // 正则匹配所有禁止的特殊字符，替换为下划线_
        // 禁止字符集：\/:*?"<>|
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }


}