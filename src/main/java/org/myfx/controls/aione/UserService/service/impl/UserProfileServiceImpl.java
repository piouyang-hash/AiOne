package org.myfx.controls.aione.UserService.service.impl;

import lombok.RequiredArgsConstructor;

import org.myfx.controls.aione.ServiceCommon.context.UserContext;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.AppTypeEnum;
import org.myfx.controls.aione.UserService.common.UserServiceConstants;
import org.myfx.controls.aione.UserService.event.UserAvatarUpdateEvent;
import org.myfx.controls.aione.UserService.event.UserUpgradeToAdminEvent;
import org.myfx.controls.aione.UserService.mapper.UserProfileMapper;
import org.myfx.controls.aione.UserService.model.entity.UserProfile;
import org.myfx.controls.aione.UserService.service.UserProfileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * 用户资料业务层实现类
 * 实现用户资料的增删改查及业务逻辑处理
 */
@Service // 标记为服务层组件，让Spring管理
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    // 注入UserProfileMapper（数据访问层，操作数据库）
    private final UserProfileMapper userProfileMapper;
    private final ApplicationEventPublisher eventPublisher; // 注入事件发布器


    // 注入配置文件中的头像存储根路径
    @Value("${avatar.storage.path}")
    private String avatarStoragePath;

    /**
     * 新增用户资料（带参数校验）
     */
    @Override
    public void addUserProfile(UserProfile userProfile) {
        // 1. 校验参数：userId不能为空（因为关联user表）
        if (userProfile.getUserId() == null) {
            throw new IllegalArgumentException("新增用户资料失败：关联的用户ID（userId）不能为空");
        }

        // 2. 可选：如果前端没传昵称/简介，这里可以手动设置默认值（也可以靠数据库默认值）
        if (userProfile.getNickname() == null || userProfile.getNickname().trim().isEmpty()) {
            userProfile.setNickname("用户"); // 默认昵称
        }
        if (userProfile.getBio() == null || userProfile.getBio().trim().isEmpty()) {
            userProfile.setBio("空简介"); // 默认简介
        }
        if (userProfile.getAvatarUrl() == null) {
            userProfile.setAvatarUrl(UserServiceConstants.DEFAULT_AVATAR); // 默认空头像
        }

        // 3. 调用mapper新增（无返回值，靠mapper的SQL执行）
        userProfileMapper.insert(userProfile);
    }

    // 实现双参数方法（核心：接收appType参数，不再从上下文获取）
    @Override
    public void addUserProfile(Integer userId, AppTypeEnum appType) {
        // ===================== 简化版：一行非空校验 =====================
        Assert.notNull(userId, "新增用户资料失败：用户ID（userId）不能为空");
        Assert.notNull(appType, "新增用户资料失败：应用标识（appType）不能为空");

        // 幂等校验
        UserProfile existingProfile = userProfileMapper.findByUserIdAndAppType(userId, appType);
        if (existingProfile != null) {
            return;
        }

        // 创建并保存
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(userId);
        userProfile.setAppType(appType);
        this.addUserProfile(userProfile);
    }

    // ==================== 事件监听器（应用默认是管理系统） ====================
    /**
     * 监听「用户提升为管理员」事件，自动为该用户新增【管理系统】的用户资料
     * @param event 用户提升为管理员事件
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, fallbackExecution = true)
    public void handleUserUpgradeToAdminEvent(UserUpgradeToAdminEvent event) {
        // 1. 从事件中获取要提升的用户ID
        Integer userId = event.getUserId();

        // 2. 调用addUserProfile方法，appType默认使用【管理系统】（ADMIN_SYSTEM）
        AppTypeEnum defaultAppType = AppTypeEnum.ADMIN_SYSTEM; // 管理系统枚举值
        this.addUserProfile(userId, defaultAppType);

    }

    /**
     * 根据UserId删除用户资料
     */
    @Override
    public void deleteByUserId(Integer userId) {
        // 校验参数：userId为空直接返回false，避免无效调用
        if (userId == null) {
            return;
        }
        // 调用Mapper删除，返回受影响的行数
        userProfileMapper.deleteByUserId(userId);
    }

    /**
     * 更新用户资料
     * 核心调整：
     * 1. 移除id校验，改用userId+上下文appType做存在性校验；
     * 2. appType统一从UserContext获取，不再从userProfile实体读取
     * @param userProfile 用户资料实体（包含userId、昵称/简介等更新字段，无需传appType）
     * @param file        头像上传文件（可为null）
     * @return 更新后的用户资料
     */
    @Override
    @Transactional
    public UserProfile updateUserProfile(UserProfile userProfile, MultipartFile file) {
        // 1. 基础参数校验（移除appType校验，仅校验userId）
        if (userProfile.getUserId() == null) {
            throw new IllegalArgumentException("更新失败：用户ID不能为空");
        }

        // 2. 权限校验：只能修改自己的资料
        Integer currentUserId = UserContext.getUserId(); // 获取当前登录用户ID
        if (!userProfile.getUserId().equals(currentUserId)) {
            throw new SecurityException("无权修改他人资料");
        }

        // 3. 从上下文获取当前应用标识（核心：替代从userProfile读取appType）
        AppTypeEnum currentAppType = UserContext.getAppType();
        if (currentAppType == null) {
            throw new IllegalArgumentException("更新失败：当前请求的应用标识（appType）未从上下文获取到");
        }

        // 4. 核心校验：userId+上下文appType对应的资料是否存在
        UserProfile dbProfile = userProfileMapper.findByUserIdAndAppType(userProfile.getUserId(), currentAppType);
        if (dbProfile == null) {
            throw new IllegalArgumentException("更新失败：当前用户在该应用下的资料不存在");
        }

        // 5. 处理图片文件（如果有上传）：仅当有文件时才调用工具类，否则跳过
        if (file != null && !file.isEmpty()) { // 关键：判断文件存在且非空
            // 1. 先创建事件对象（只传source、file、userId，avatarUrl先不赋值）
            UserAvatarUpdateEvent event = new UserAvatarUpdateEvent(this, file, currentUserId);
            // 2. 发布事件（监听器会在这一步同步执行，给event的avatarUrl赋值）
            eventPublisher.publishEvent(event);
            // 3. 从事件对象中获取监听器处理后的avatarUrl！
            String avatarUrl = event.getAvatarUrl();
            // 4. 设置到用户资料中
            userProfile.setAvatarUrl(avatarUrl);
        }
        // 没有图片就不执行上面的逻辑，自然跳过

        // 设置应用名称参数
        userProfile.setAppType(currentAppType);

        // 6. 执行数据库更新（传入userProfile和上下文的appType，适配Mapper的参数）
        userProfileMapper.update(userProfile);

        // 7. 更新成功后，查询最新的用户资料并返回（用userId+上下文appType查询）
        return userProfileMapper.findByUserIdAndAppType(userProfile.getUserId(), currentAppType);
    }

    /**
     * 【重载实现】获取当前登录用户的资料
     * 自动从上下文获取userId和appType
     */
    @Override
    public UserProfile getCurrentUserProfile() {
        // 1. 从上下文获取当前用户ID
        Integer userId = UserContext.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("获取失败：当前用户ID未从上下文获取到");
        }

        // 2. 从上下文获取当前应用标识
        AppTypeEnum appType = UserContext.getAppType();
        Assert.notNull(appType, "获取失败：当前请求的应用标识（appType）未从上下文获取到");

        // 3. 调用双参数方法，实现逻辑复用
        return getUserProfile(userId, appType);
    }


    /**
     * 根据用户ID（关联的user表id）查询用户资料
     * 注意：需要在UserProfileMapper中新增findByUserId方法（已适配userId+appType参数）
     */
    @Override
    public UserProfile getUserProfile(Integer userId, AppTypeEnum appType) {
        // 1. 校验参数（新增appType非空校验）
        if (userId == null) {
            return null;
        }
        if (appType == null) {
            throw new IllegalArgumentException("查询失败：应用标识（appType）不能为空");
        }

        // 2. 调用mapper的findByUserId方法（传入userId+appType，适配多应用隔离）
        return userProfileMapper.findByUserIdAndAppType(userId, appType);
    }

    @Override
    public File getAvatarUrlByUserId(Integer userId) {
        // 1. 获取用户资料（已知头像存在，无需判断）
        UserProfile userProfile = getCurrentUserProfile();

        // 2. 提取数据库中的头像文件名（如"avatar_1_1762868001873.jpg"）
        String avatarFileName = userProfile.getAvatarUrl();

        // 3. 直接返回File对象（用根路径+文件名构造，自动处理路径分隔符）
        return new File(avatarStoragePath, avatarFileName);
    }
}