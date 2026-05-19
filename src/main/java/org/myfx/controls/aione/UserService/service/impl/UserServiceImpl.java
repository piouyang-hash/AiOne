package org.myfx.controls.aione.UserService.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.ServiceCommon.context.JwtExpireTimeContext;
import org.myfx.controls.aione.ServiceCommon.context.RequestContext;
import org.myfx.controls.aione.ServiceCommon.context.UserContext;
import org.myfx.controls.aione.ServiceCommon.event.eventDTO.UserCanceledEvent;
import org.myfx.controls.aione.ServiceCommon.event.eventDTO.UserRegisteredEvent;
import org.myfx.controls.aione.ServiceCommon.event.publisher.UserEventPublisher;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.AppTypeEnum;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.LogicalDeleteEnum;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.RoleEnum;
import org.myfx.controls.aione.ServiceCommon.utils.JwtTokenUtil;
import org.myfx.controls.aione.UserService.common.exception.*;
import org.myfx.controls.aione.UserService.event.UserLoginSuccessEvent;
import org.myfx.controls.aione.UserService.event.UserUpgradeToAdminEvent;
import org.myfx.controls.aione.UserService.mapper.UserMapper;
import org.myfx.controls.aione.UserService.model.dto.LoginDTO;
import org.myfx.controls.aione.UserService.model.dto.UserOperateDTO;
import org.myfx.controls.aione.UserService.model.entity.User;
import org.myfx.controls.aione.UserService.model.vo.LoginResultVO;
import org.myfx.controls.aione.UserService.model.vo.RefreshTokenVO;
import org.myfx.controls.aione.UserService.service.UserService;
import org.myfx.controls.aione.UserService.util.PasswordEncryptor;
import org.myfx.controls.aione.UserService.util.validation.UserValidationUtil;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.TimeUnit;


// @Service注解：告诉Spring这是一个服务层组件，会被自动管理
@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    // 用final修饰，确保依赖不可变（更安全）
    private final UserMapper userMapper;
    private final JwtTokenUtil jwtTokenUtil;
    private final StringRedisTemplate stringRedisTemplate;
    private final ApplicationEventPublisher eventPublisher; // 注入事件发布器

    private final UserEventPublisher userEventPublisher;

    @Override
    @Transactional
    public void registerUser(LoginDTO loginDTO) {
        // 1. 参数校验
        if (loginDTO == null) {
            throw new IllegalArgumentException("注册失败：登录DTO不能为空");
        }
        UserValidationUtil.validatePassword(loginDTO.getPassword());
        UserValidationUtil.validateQqEmail(loginDTO.getEmail());
        if (loginDTO.getAppType() == null) {
            throw new IllegalArgumentException("注册失败：应用标识（appType）不能为空");
        }

        // 2. 检查邮箱是否存在
        if (userMapper.existsByEmail(loginDTO.getEmail())) {
            throw new RegisterException(RegisterException.RegisterError.EMAIL_EXISTS);
        }

        // 3. 密码加密 + 构建用户实体
        String encryptedPassword = PasswordEncryptor.encrypt(loginDTO.getPassword());
        User user = new User();
        user.setEmail(loginDTO.getEmail());
        user.setPassword(encryptedPassword);

        // 4. 保存用户
        userMapper.add(user);
        Integer userId = user.getId();

        // ===================== 核心修改：调用UserEventPublisher发布事件 =====================
        userEventPublisher.publishUserRegisteredEvent(userId, loginDTO.getAppType());

        log.info("用户注册成功，已发布本地事件，用户ID：{}，应用类型：{}", userId, loginDTO.getAppType());
    }

    @Override
    public boolean checkRegisterSuccess(UserOperateDTO userOperateDTO) {
        // 参数校验
        Assert.notNull(userOperateDTO, "注册状态检查参数不能为空");
        Assert.notNull(userOperateDTO.getUserId(), "用户ID不能为空");

        Integer currentUserId = userOperateDTO.getUserId();
        log.info("开始检查注册状态，用户ID: {}", currentUserId);

        // 调用Mapper，查找未逻辑删除的用户记录
        User user = userMapper.findById(currentUserId, LogicalDeleteEnum.NOT_DELETED);

        // 判断用户是否存在
        if (user != null) {
            log.info("注册成功，用户ID: {} 存在且未被删除。", currentUserId);
            return true;
        } else {
            log.info("注册状态异常，未找到用户ID: {} 的未删除记录。", currentUserId);
            return false;
        }
    }

    @Override
    public LoginResultVO login(LoginDTO loginDTO) { // 核心修改：参数从User改为LoginDTO
        // 1. 直接校验邮箱格式 + 通过邮箱查询用户（移除用户名逻辑，取值适配DTO）
        UserValidationUtil.validateQqEmail(loginDTO.getEmail());
        User existingUser = getUserByEmail(loginDTO.getEmail());

        // 2. 校验用户是否存在
        if (existingUser == null) {
            throw new LoginException(LoginException.LoginError.INVALID_CREDENTIALS);
        }

        // 3. 校验密码（移除验证码逻辑，取值适配DTO）
        boolean isPasswordValid = PasswordEncryptor.verify(loginDTO.getPassword(), existingUser.getPassword());
        if (!isPasswordValid) {
            throw new LoginException(LoginException.LoginError.INVALID_CREDENTIALS);
        }


        LoginResultVO result = new LoginResultVO();
        result.setUserId(existingUser.getId());
        result.setProfile(null);

        return result;
    }

    @Override
    public LoginResultVO quickLogin(LoginDTO loginDTO) {
        // 1. 仅通过邮箱查询用户
        User existingUser = getUserByEmail(loginDTO.getEmail());
        if (existingUser == null) {
            throw new LoginException(LoginException.LoginError.INVALID_CREDENTIALS);
        }

        // 2. 校验密码
        boolean isPasswordValid = PasswordEncryptor.verify(loginDTO.getPassword(), existingUser.getPassword());
        if (!isPasswordValid) {
            throw new LoginException(LoginException.LoginError.INVALID_CREDENTIALS);
        }

        // 3. 解析应用枚举并校验管理员权限
        AppTypeEnum appType = loginDTO.getAppType();
        if (AppTypeEnum.ADMIN_SYSTEM.equals(appType)) {
            if (!RoleEnum.ADMIN.equals(existingUser.getRole())) {
                throw new LoginException(LoginException.LoginError.FORBIDDEN_ADMIN);
            }
        }

        // ===================== 新增：获取记住我状态 =====================
        boolean rememberMe = Boolean.TRUE.equals(loginDTO.getRememberMe());

        // 4. 生成登录token
       // ===================== 新增：生成双Token（无痛过渡） =====================
        String accessToken = jwtTokenUtil.generateAccessToken(existingUser.getId(), existingUser.getRole(), appType);
        String refreshToken = jwtTokenUtil.generateRefreshToken(existingUser.getId(), existingUser.getRole(), appType, rememberMe);

        // 5. 构建登录结果
        LoginResultVO result = new LoginResultVO();
        result.setUserId(existingUser.getId());
        result.setProfile(null);

        // ===================== 【核心新增】构建并赋值 RefreshTokenVO =====================
        RefreshTokenVO refreshTokenVO = new RefreshTokenVO();
        refreshTokenVO.setAccessToken(accessToken);
        refreshTokenVO.setRefreshToken(refreshToken);
        // 自动填充：accessToken过期时间 + 根据rememberMe填充refreshToken过期时间
        refreshTokenVO.setRememberMe(rememberMe);
        // 赋值到登录结果中
        result.setRefreshTokenVO(refreshTokenVO);

        // 6. 【核心修改】发布用户登录成功事件
        eventPublisher.publishEvent(new UserLoginSuccessEvent(
                this,
                existingUser.getId(),
                appType
        ));

        return result;
    }

    @Override
    public LoginResultVO autoLogin() {
        // 1. 从UserContext获取用户ID（由CheckJwt注解对应的切面提前存入）
        Integer userId = UserContext.getUserId(); // 假设UserContext存储的是Integer类型

        // 2. 根据用户ID查询用户信息（复用已有方法）
        User user = getUserById(userId);
        if (user == null) {
            throw new LoginException(LoginException.LoginError.INVALID_CREDENTIALS);
        }

        // 3. 封装LoginResult（token从UserContext获取，切面中已解析并存入）
        LoginResultVO result = new LoginResultVO();
        result.setUserId(user.getId());
        result.setProfile(null);

        return result;
    }

    @Override
    @Transactional
    public boolean cancelUserAccount() {
        // 1. 获取当前登录用户ID
        Integer currentUserId = UserContext.getUserId();

        // 2. 校验用户ID
        UserValidationUtil.validateUserId(currentUserId);

        // 3. 校验用户存在
        User user = userMapper.findById(currentUserId, LogicalDeleteEnum.NOT_DELETED);
        UserValidationUtil.validateUserNotNull(user);

        // ===================== 核心修改：调用UserEventPublisher发布注销事件 =====================
        userEventPublisher.publishUserCanceledEvent(currentUserId);

        log.info("用户注销成功，已发布本地注销事件，用户ID：{}", currentUserId);
        return true;
    }

    @Override
    public boolean cancelUserMain(UserOperateDTO userDTO) {
        // 参数校验
        Assert.notNull(userDTO, "用户操作DTO不能为空");
        Assert.notNull(userDTO.getUserId(), "用户ID不能为空");

        Integer userId = userDTO.getUserId();
        log.info("开始逻辑删除用户主表，用户ID: {}", userId);

        // 检查用户是否已被删除
        User user = userMapper.findById(userDTO.getUserId(), LogicalDeleteEnum.NOT_DELETED);
        if (user != null && user.getIsDeleted().getCode() == 1) {
            log.info("用户已被删除，跳过");
            return true; // 幂等返回成功
        }

        // 调用Mapper，逻辑删除用户
        int affectedRows = userMapper.logicalDeleteById(userId);

        if (affectedRows > 0) {
            log.info("用户主表逻辑删除成功，用户ID: {}", userId);
        } else {
            log.warn("用户主表逻辑删除失败，用户ID: {} 可能不存在或已被删除", userId);
        }

        return affectedRows > 0;
    }

    @Override
    public boolean logicRecoverForCancelFail(UserOperateDTO userOperateDTO) {
        // 参数校验
        Assert.notNull(userOperateDTO, "注销失败补偿参数不能为空");
        Assert.notNull(userOperateDTO.getUserId(), "用户ID不能为空");

        Integer userId = userOperateDTO.getUserId();
        log.info("开始执行注销失败补偿，用户ID: {}", userId);

        // 可选：添加业务层幂等检查
        // 查询用户当前状态
        User user = userMapper.findById(userId, LogicalDeleteEnum.DELETED);
        if (user == null) {
            // 用户不存在或未被删除
            log.info("用户未被删除，无需补偿恢复，用户ID: {}", userId);
            return true;
        }

        // 调用Mapper，逻辑复原用户
        int affectedRows = userMapper.recoverLogicalDeleteById(userId);

        if (affectedRows > 0) {
            log.info("注销失败补偿成功，已逻辑复原用户ID: {}", userId);
        } else {
            log.info("注销失败补偿未执行，用户ID: {} 可能未被逻辑删除", userId);
        }

        return true;
    }

    @Override
    public void logout() {
        // 1. 无参数，直接从RequestContext获取当前JWT令牌（内部已处理格式校验）
        String currentToken = RequestContext.getToken();

        // 2. 从JwtExpireTimeContext获取当前JWT的过期时间
        Date jwtExpireDate = JwtExpireTimeContext.getExpireDate();

        // 3. 计算JWT剩余有效期（确保黑名单与JWT同时过期，不占用Redis内存）
        long remainExpireMillis = jwtExpireDate.getTime() - System.currentTimeMillis();
        if (remainExpireMillis <= 0) {
            return; // 令牌已过期，无需加入黑名单
        }

        // 4. 将令牌加入Redis黑名单，使其失效
        stringRedisTemplate.opsForValue().set(
                "jwt:blacklist:" + currentToken,
                "invalid", // 值无实际意义，仅用于标记
                remainExpireMillis,
                TimeUnit.MILLISECONDS
        );
    }

    /**
     * 未登录状态下重置密码（专门适配找回密码控制器）
     * 核心逻辑：无登录态依赖 + 邮箱定位用户 + 密码加密更新
     */
    @Override
    @Transactional
    public void resetPassword(String email, String newPassword) {
        // 1. 校验邮箱+新密码格式
        UserValidationUtil.validateQqEmail(email);
        UserValidationUtil.validatePassword(newPassword);

        // 2. 查询用户（双重保险）
        User existingUser = userMapper.findByEmail(email);
        UserValidationUtil.validateUserNotNull(existingUser);

        // 3. 加密新密码（用于后续更新数据库）
        String encryptedPassword = PasswordEncryptor.encrypt(newPassword);

        // 4. 关键修复：用verify方法对比新密码明文和旧密码加密值
        boolean isSamePassword = PasswordEncryptor.verify(newPassword, existingUser.getPassword());
        log.info("【找回密码】密码对比结果 - 邮箱：{}，新密码与旧密码是否一致：{}", email, isSamePassword);

        // 5. 相同则抛异常，禁止修改
        if (isSamePassword) {
            log.warn("【找回密码】新密码与旧密码一致，抛出异常 - 邮箱：{}", email);
            throw new OldNewPasswordSameException("新密码与旧密码一致，请更换其他密码");
        }

        // 6. 执行密码更新
        User updateUser = new User();
        updateUser.setId(existingUser.getId());
        updateUser.setPassword(encryptedPassword);
        userMapper.updatePassword(updateUser);

        log.info("【找回密码】密码更新成功 - 邮箱：{}", email);
    }

    @Override
    @Transactional // 事务保证：校验和更新操作原子性
    public void changePassword(String oldPassword, String newPassword) {
        // 1. 获取当前登录用户ID（ThreadLocal中的用户ID，作为基准）
        Integer currentUserId = UserContext.getUserId();
        UserValidationUtil.validateUserId(currentUserId); // 校验当前用户ID非空

        // 2. 查询当前用户完整信息（用于验证原密码、对比新旧密码）
        User existingUser = userMapper.findById(currentUserId, LogicalDeleteEnum.NOT_DELETED);
        UserValidationUtil.validateUserNotNull(existingUser);

        // ========== 核心修复：验证原密码 + 模糊提示 ==========
        boolean isOldPasswordCorrect = PasswordEncryptor.verify(oldPassword, existingUser.getPassword());
        if (!isOldPasswordCorrect) {
            // 日志记录具体原因（便于排查），对外只抛模糊异常
            log.warn("【登录态改密码】用户ID：{}，原密码验证失败（模糊提示：身份验证未通过）", currentUserId);
            // 抛你原代码已有的SecurityException，提示语模糊化
            throw new SecurityException("身份验证未通过，请检查信息后重试");
        }

        // 3. 校验新密码格式（强度、长度等）
        UserValidationUtil.validatePassword(newPassword);

        // 4. 校验新密码是否与旧密码一致（避免改完和原来一样）
        boolean isSameWithOld = PasswordEncryptor.verify(newPassword, existingUser.getPassword());
        if (isSameWithOld) {
            log.warn("【登录态改密码】用户ID：{}，新密码与旧密码一致，禁止修改", currentUserId);
            // 这里也可模糊提示（可选，若想更严格，也可保留原有提示）
            throw new SecurityException("身份验证未通过，请检查信息后重试");
        }

        // 5. 加密新密码 + 执行更新（复用你原有的有效逻辑）
        String encryptedNewPassword = PasswordEncryptor.encrypt(newPassword);
        User updateUser = new User();
        updateUser.setId(currentUserId);
        updateUser.setPassword(encryptedNewPassword);
        userMapper.updatePassword(updateUser);

        log.info("【登录态改密码】用户ID：{}，密码更新成功", currentUserId);
    }

    @Override
    @Transactional // 事务保证：校验和更新操作原子性
    public void changeEmail(String newEmail) {
        // 1. 从上下文获取当前登录用户ID（核心：无需手动传ID，防止越权）
        Integer currentUserId = UserContext.getUserId();

        // 2. 校验新邮箱格式（必须是QQ邮箱）
        UserValidationUtil.validateQqEmail(newEmail); // 格式不符直接抛异常

        // 3. 检查新邮箱是否与旧邮箱一致（避免无效更新）
        User currentUser = userMapper.findById(currentUserId, LogicalDeleteEnum.NOT_DELETED);
        if (currentUser == null) {
            throw new RuntimeException("当前登录用户不存在");
        }
        String oldEmail = currentUser.getEmail();
        if (newEmail.equals(oldEmail)) {
            throw new OldNewEmailSameException("新邮箱与旧邮箱一致，请更换其他邮箱");
        }

        // 4. 检查新邮箱是否已被其他用户占用
        if (userMapper.existsByEmail(newEmail)) {
            throw new UserAlreadyExistsException("该QQ邮箱已被其他用户注册，无法使用");
        }

        // 5. 直接调用mapper更新（无需封装User实体，传两个参数即可）
        int affectedRows = userMapper.updateEmail(currentUserId, newEmail);
        if (affectedRows == 0) {
            throw new RuntimeException("邮箱更新失败，用户不存在或未修改");
        }
    }

    /**
     * 根据当前登录用户ID提升为超级管理员（需认证通过）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upgradeToAdmin(boolean isAuthenticated) {
        // 1. 从ThreadLocal获取当前登录用户ID
        Integer currentUserId = UserContext.getUserId();

        // 2. 校验认证是否通过
        if (!isAuthenticated) {
            throw new RuntimeException("认证未通过，无法提升为超级管理员");
        }

        // 3. 执行权限提升操作
        userMapper.updateUserRoleToAdmin(currentUserId);

        // 4. 发布「用户提升为管理员」事件（核心新增）
        eventPublisher.publishEvent(new UserUpgradeToAdminEvent(currentUserId));
    }

    @Override
    public User getUserById(Integer id) {
        UserValidationUtil.validateUserId(id); // 校验ID合法性
        User user = userMapper.findById(id, LogicalDeleteEnum.NOT_DELETED);
        UserValidationUtil.validateReturnedUserNotNull(user); // 校验返回结果
        return user;
    }

    @Override
    public User getUserByEmail(String email) {
        UserValidationUtil.validateQqEmail(email); // 校验邮箱合法性
        User user = userMapper.findByEmail(email);
        UserValidationUtil.validateReturnedUserNotNull(user); // 校验返回结果
        return user;
    }


    @Override
    public List<Integer> getAllUserIdsByBatch(int batchSize) {
        List<Integer> allUserIds = new ArrayList<>();
        int offset = 0; // 偏移量：初始从第0条开始

        while (true) {
            // 调用Mapper分页查询
            List<Integer> batchIds = userMapper.selectUserIdsByBatch(offset, batchSize);

            // 如果查询结果为空，说明已查询完所有用户，退出循环
            if (batchIds.isEmpty()) {
                break;
            }

            // 将当前批次的ID加入总列表
            allUserIds.addAll(batchIds);
            // 偏移量累加，准备查询下一批
            offset += batchSize;

            // 每批查询后休眠50ms，降低数据库压力（可根据服务器性能调整）
            try {

                Thread.sleep(50);
            } catch (InterruptedException e) {
                // 中断时恢复中断状态，避免线程状态异常
                Thread.currentThread().interrupt();
                break;
            }
        }

        return allUserIds;
    }
}