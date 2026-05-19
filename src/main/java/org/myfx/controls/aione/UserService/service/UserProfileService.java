package org.myfx.controls.aione.UserService.service;

import org.myfx.controls.aione.ServiceCommon.serviceEnum.AppTypeEnum;
import org.myfx.controls.aione.UserService.model.entity.UserProfile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * 用户资料业务层接口
 * 处理用户资料相关的业务逻辑（如参数校验、事务控制等）
 */
public interface UserProfileService {

    /**
     * 新增用户资料
     * @param userProfile 要新增的用户资料对象（需包含有效的userId，其他字段可选，默认值会由数据库或业务层补充）
     * @throws IllegalArgumentException 当userId为null或无效时抛出（如用户不存在）
     */
    void addUserProfile(UserProfile userProfile);

    /**
     * 新增用户资料（指定用户ID+应用标识，其他字段使用默认值）
     * @param userId  关联的用户ID（user表的id）
     * @param appType 应用标识枚举（多应用隔离核心参数，不能为空）
     * @throws IllegalArgumentException 当userId或appType为null/无效时抛出
     */
    void addUserProfile(Integer userId, AppTypeEnum appType);

    /**
     * 根据用户ID（关联的user表id）删除用户资料
     * 这个是注销调用的接口，会删除此用户的所有的用户资料
     * @param userId 关联的用户ID
     */
    void deleteByUserId(Integer userId);

    /**
     * 更新用户资料（支持更新基本信息及头像图片）
     *
     * @param userProfile 包含更新信息的用户资料对象（必须包含有效的id和userId，否则无法定位记录或校验权限）
     * @param file        可选的头像图片文件（可为null，null表示不更新头像）
     * @return 操作结果：直接返回更新后的UserProfile
     * @throws IllegalArgumentException 当userProfile的id为null、userId为null时抛出（基础参数无效）
     * @throws SecurityException        当userProfile的userId与当前登录用户ID不一致时抛出（无权修改他人资料）
     * @throws RuntimeException         当图片文件校验失败（非真实图片）或上传过程出错时抛出
     */
    UserProfile updateUserProfile(UserProfile userProfile, MultipartFile file) throws Exception;

    /**
     * 【重载1】查询用户资料（自动从上下文获取当前用户表示和应用类型）
     * 适用于「当前请求所属应用」的场景（如登录用户在阅读器应用内查询自己的资料）
     * @return 对应的用户资料对象；当前应用下无资料则返回null
     */
    UserProfile getCurrentUserProfile();

    /**
     * 根据用户ID+应用标识查询用户资料
     * @param userId 关联的用户ID（user表的id）
     * @param appId  应用标识枚举（1=阅读器，2=AI聊天，3=拓展位），多应用隔离核心参数
     * @return 对应的用户资料对象；一个用户在一个应用下只有一条资料，不存在则返回null
     */
    UserProfile getUserProfile(Integer userId, AppTypeEnum appId);

    /**
     * 根据用户ID查询头像URL（直接返回字符串）
     * @param userId 关联的用户ID（user表的id）
     * @return 直接返回头像文件
     */
    File getAvatarUrlByUserId(Integer userId);

}