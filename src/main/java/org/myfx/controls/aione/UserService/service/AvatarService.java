package org.myfx.controls.aione.UserService.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.ServiceCommon.event.eventDTO.UserCanceledEvent;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.FileDeleteBusinessTypeEnum;
import org.myfx.controls.aione.ServiceCommon.utils.FileDeleteUtil;
import org.myfx.controls.aione.ServiceCommon.utils.FileMagicNumberUtil;
import org.myfx.controls.aione.ServiceCommon.utils.FileStorageUtil;
import org.myfx.controls.aione.ServiceCommon.utils.HashCalculatorUtil;
import org.myfx.controls.aione.UserService.common.UserServiceConstants;
import org.myfx.controls.aione.UserService.event.UserAvatarUpdateEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AvatarService {

    private final FileDeleteUtil fileDeleteUtil;
    /**
     * 处理用户头像更新事件：调用uploadAvatar方法并设置URL到事件中
     */
    @EventListener
    public void handleUserAvatarUpdate(UserAvatarUpdateEvent event) throws Exception {
        MultipartFile file = event.getFile();
        Integer userId = event.getUserId();

        // 调用uploadAvatar方法生成头像URL
        String avatarUrl = this.uploadAvatar(file, userId);

        // 将URL存入事件对象中
        event.setAvatarUrl(avatarUrl);

        // 调用deleteOldestAvatar方法，传入userId
        this.deleteOldestAvatar(userId);
    }

    /**
     * 监听用户取消事件，删除用户所有头像
     */
    @EventListener
    public void handleUserCanceledEvent(UserCanceledEvent event) {
        // 从事件中获取用户ID（假设事件有getUserId()方法）
        Integer userId = event.getUserId(); // 或event.getUserId()，根据事件实际方法调整

        // 调用deleteAllUserAvatars方法
        this.deleteAllUserAvatars(userId);

        log.info("用户{}取消操作，已删除其所有头像", userId);
    }


    @Value("${avatar.storage.path}")
    private String avatarRootPath;

    @Value("${avatar.max-count:5}")
    private Integer maxAvatarCount;

    /**
     * 上传用户头像
     * @param file 上传的头像文件
     * @param userId 当前用户ID
     * @return 头像的访问URL
     * @throws Exception 上传过程中的异常（如文件处理失败）
     */
    public String uploadAvatar(MultipartFile file, Integer userId) throws Exception {
        // 1. 校验文件（二次校验，确保文件有效）
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传的头像文件不能为空");
        }

        // 2. 计算文件哈希值（用于生成唯一文件名）
        String fileHash = HashCalculatorUtil.calculateHash(file);

        // 3. 获取文件真实后缀（通过文件魔数，防止后缀伪造）
        byte[] fileHeader = FileMagicNumberUtil.readFileHeader(file);
        String realSuffix = FileMagicNumberUtil.getRealImageSuffix(fileHeader);
        if (realSuffix == null || realSuffix.isEmpty()) {
            throw new IllegalArgumentException("不支持的图片格式，请上传jpg/png等格式");
        }

        // 生成唯一文件名：用户ID-哈希值-时间戳.后缀（如：1-a1b2c3d4-1764385845678）
        String mainFileName = userId + "-" + fileHash + "-" + System.currentTimeMillis() + ".";

        // 获取头像存储根目录（从常量配置类读取）
        String mainDir = UserServiceConstants.AVATAR_STORAGE_PATH;

        // 生成文件存储路径（调用工具类）
        File storageFile = FileStorageUtil.generateFilePath(mainFileName, mainDir, realSuffix);

        // 调用文件存储注册方法（替换原步骤四，事务提交后执行实际存储）
        FileStorageUtil.registerFileStoreAfterCommit(
                userId,                  // 业务ID（用户ID）
                file,                    // 上传的主文件
                mainFileName,            // 生成的文件名
                mainDir,                 // 存储目录
                realSuffix,              // 文件真实后缀
                UserServiceConstants.SHARE_ENABLED,      // 业务类型（用户业务）
                UserServiceConstants.USER_BUSINESS_DESC  // 业务描述
        );

        // 生成头像访问URL（返回给前端）
        return storageFile.getAbsolutePath();
    }


    /**
     * 根据用户ID列出该用户的所有头像文件绝对路径
     * @param userId 用户ID
     * @return 头像文件绝对路径列表
     */
    public List<String> listUserAvatars(Integer userId) {
        // 校验用户ID非空
        if (userId == null) {
            return Collections.emptyList();
        }

        File userAvatarDir = new File(avatarRootPath);
        if (!userAvatarDir.exists()) {
            return Collections.emptyList();
        }

        // 匹配以"userId-"开头的文件
        String prefix = userId + "-";
        File[] files = userAvatarDir.listFiles(f -> f.isFile() && f.getName().startsWith(prefix));

        if (files == null || files.length == 0) {
            return Collections.emptyList();
        }

        // 返回文件绝对路径列表
        return Arrays.stream(files)
                .map(File::getAbsolutePath)
                .collect(Collectors.toList());
    }

    /**
     * 根据用户ID和文件名获取单个头像的绝对路径
     * @param userId 用户ID
     * @param fileName 头像文件名
     * @return 头像绝对路径（文件不存在返回null）
     */
    public String getAvatarAbsolutePath(Integer userId, String fileName) {
        if (userId == null || fileName == null || !fileName.startsWith(userId + "-")) {
            return null;
        }

        File avatarFile = new File(avatarRootPath, fileName);
        return avatarFile.exists() ? avatarFile.getAbsolutePath() : null;
    }

    /**
     * 注册事务提交后的头像文件删除回调（非直接删除）
     * <p>【依赖说明】：此方法需在**有活跃Spring事务**的上下文中调用（即外层方法需加@Transactional注解），
     * 否则事务同步回调无法注册，文件将不会被删除！</p>
     * <p>【原理】：通过TransactionSynchronizationManager注册回调，仅当外层事务提交成功后才执行文件删除；
     * 若事务回滚或无活跃事务，回调不生效，文件保留。</p>
     * @param userId 用户ID
     * @param avatarAbsolutePath 头像文件的绝对路径（直接传入，无需处理）
     * @return 回调注册请求已提交（是否最终删除取决于事务状态和文件实际存在性）
     */
    public boolean deleteAvatar(Integer userId, String avatarAbsolutePath) {
        // 直接调用工具类，传入绝对路径
        fileDeleteUtil.registerSingleFileDeleteAfterCommit(
                userId,                  // 业务ID（用户ID）
                avatarAbsolutePath,      // 直接使用传入的绝对路径
                FileDeleteBusinessTypeEnum.USER_AVATAR // 业务类型："头像"
        );
        return true;
    }

    /**
     * 删除用户最旧的头像文件（根据文件名中的时间戳判断）
     * @param userId 用户ID
     */
    public void deleteOldestAvatar(Integer userId) {
        // 1. 获取用户所有头像的绝对路径列表
        List<String> allAvatarPaths = listUserAvatars(userId);

        // 用户无头像时打印日志并返回
        if (allAvatarPaths.isEmpty()) {
            log.info("用户{}暂无头像，无需执行删除操作", userId);
            return;
        }

        // 2. 判断当前头像数量是否超过最大值，未超过则不执行删除
        if (allAvatarPaths.size() <= maxAvatarCount) {
            log.info("用户{}当前头像数量{}未超过最大限制{}，不执行删除", userId, allAvatarPaths.size(), maxAvatarCount);
            return;
        }

        // 3. 遍历所有头像，解析时间戳并找到最旧的那个
        String oldestAvatarPath = null;
        long oldestTimestamp = Long.MAX_VALUE;

        for (String avatarPath : allAvatarPaths) {
            File avatarFile = new File(avatarPath);
            String fileName = avatarFile.getName();

            String[] nameParts = fileName.split("-");
            String timestampWithSuffix = nameParts[2];
            String timestampStr = timestampWithSuffix.split("\\.")[0];
            long fileTimestamp = Long.parseLong(timestampStr);

            if (fileTimestamp < oldestTimestamp) {
                oldestTimestamp = fileTimestamp;
                oldestAvatarPath = avatarPath;
            }
        }

        // 4. 提取最旧头像的文件名，调用deleteAvatar删除
        assert oldestAvatarPath != null;
        String oldestFileName = new File(oldestAvatarPath).getName();
        deleteAvatar(userId, oldestFileName);
        log.info("用户{}已删除最旧头像：{}", userId, oldestFileName);
    }

    /**
     * 注册指定用户所有头像文件的事务删除回调（非直接删除）
     *
     * @param userId 用户ID
     */
    public void deleteAllUserAvatars(Integer userId) {
        List<String> avatarPaths = listUserAvatars(userId);

        // 用户无头像时打印日志并返回0
        if (avatarPaths.isEmpty()) {
            log.info("用户{}暂无头像，无需注册删除回调", userId);
            return;
        }

        int registeredCount = 0; // 统计成功注册回调的文件数

        for (String path : avatarPaths) {
            // 从绝对路径中提取文件名（用于调用deleteAvatar）
            File avatarFile = new File(path);
            String fileName = avatarFile.getName();

            // 调用deleteAvatar方法注册单个文件的删除回调
            boolean isRegistered = deleteAvatar(userId, fileName);
            if (isRegistered) {
                registeredCount++;
            }
        }
        // 添加日志，记录成功注册的数量
        log.info("用户{}成功注册{}个头像文件的删除回调", userId, registeredCount);
    }
}