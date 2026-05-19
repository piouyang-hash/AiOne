package org.myfx.controls.aione.UserService.service;

import org.myfx.controls.aione.UserService.common.exception.LoginException;
import org.myfx.controls.aione.UserService.common.exception.UserAlreadyExistsException;
import org.myfx.controls.aione.UserService.model.dto.LoginDTO;
import org.myfx.controls.aione.UserService.model.dto.UserOperateDTO;
import org.myfx.controls.aione.UserService.model.entity.User;
import org.myfx.controls.aione.UserService.model.vo.LoginResultVO;

import java.util.List;

public interface UserService {

    /**
     * 用户注册（需验证邮箱验证码）
     * @param loginDTO 注册的登录DTO对象（包含邮箱、密码、应用标识等注册信息，无需传username）
     * 注意：该方法会对用户密码进行加密处理（使用BCrypt算法），数据库中存储的是加密后的密文，不会保留明文密码；
     *      同时，会自动创建指定应用下的默认UserProfile到数据库；
     *      appType（应用标识）从LoginDTO中获取，用于多应用隔离创建用户资料
     * @throws RuntimeException 当验证码错误或已过期时抛出运行时异常
     * @throws UserAlreadyExistsException 当邮箱已被注册时抛出（不再校验用户名）
     */
    void registerUser(LoginDTO loginDTO);

    /**
     * 检查用户注册是否成功
     * 用于Saga事务模式的注册流程起始点，通过查询用户存在性判断注册最终状态
     *
     * @param userOperateDTO 用户操作DTO，包含要检查的用户ID
     * @return 注册成功状态（true=注册成功，false=注册失败）
     */
    boolean checkRegisterSuccess(UserOperateDTO userOperateDTO);

    /**
     * 用户登录（需验证邮箱/密码及验证码）
     * @param loginDTO 包含登录所需的信息（必填：邮箱email、密码password；无需填写：username）
     * @return 登录成功的凭证（包含JWT令牌、用户ID、用户名及用户资料等信息）
     * @throws RuntimeException 当用户不存在、密码错误、验证码错误或已过期时抛出
     */
    LoginResultVO login(LoginDTO loginDTO);

    // 新增：快速登录方法（免验证码）
    LoginResultVO quickLogin(LoginDTO loginDTO) throws LoginException;

    /**
     * 自动登录（基于前端已有的JWT令牌，需在请求头中携带Authorization: Bearer <token>）
     * 注：前端需自行保存令牌，接口返回结果中不包含token
     * @return 登录成功的凭证（用户基本信息，不含token）
     * @throws RuntimeException 当用户不存在等情况时抛出
     */
    LoginResultVO autoLogin();

    /**
     * 注销当前登录用户账号（级联删除）
     * 说明：会同时删除当前登录用户的主信息（user表）及其关联的用户资料（UserProfile表，按当前应用隔离）；
     *      无需传入用户ID，系统自动从上下文获取当前登录用户ID。
     * @return true：注销成功（用户及资料均被删除）；false：注销失败（比如用户ID不存在）
     */
    boolean cancelUserAccount();

    /**
     * Saga专用：逻辑删除用户主表记录
     * 仅操作用户主表，不涉及其他服务
     *
     * @param userDTO 用户操作DTO
     * @return 是否删除成功
     */
    boolean cancelUserMain(UserOperateDTO userDTO);

    /**
     * 注销失败补偿：逻辑复原已删除的用户
     * 用于Saga事务的补偿操作，当用户注销流程失败时调用
     *
     * @param userOperateDTO 用户操作DTO，包含用户ID
     * @return 是否成功执行补偿
     */
    boolean logicRecoverForCancelFail(UserOperateDTO userOperateDTO);

    /**
     * 退出登录：无需参数，内部自动获取当前JWT令牌并使其失效
     * 核心逻辑：将当前请求的JWT加入黑名单，使其无法再使用
     */
    void logout();

    /**
     * 未登录状态下重置密码（适配找回密码场景）
     * @param email  用户绑定的邮箱
     * @param newPassword 新密码（明文）
     */
    void resetPassword(String email, String newPassword);

    /**
     * 更改用户密码（登录态下）
     * 功能说明：验证原密码正确性后，对新密码进行格式校验、自动加密，更新至当前登录用户的数据库记录中
     * @param oldPassword 原密码明文（用户输入的旧密码，用于身份验证）
     * @param newPassword 新密码明文（如 "Abc123456"），后端会自动校验格式、去除首尾双引号后加密
     * 注意：用户 ID 从登录上下文 UserContext 中自动获取，无需传入，防止越权修改他人密码
     */
    void changePassword(String oldPassword, String newPassword);

    /**
     * 更改用户邮箱（简化版）
     * 功能说明：仅接收新邮箱，用户ID从登录上下文自动获取，无需手动传参
     * @param newEmail 新QQ邮箱地址（如 "123456@qq.com"），需符合QQ邮箱格式
     */
    void changeEmail(String newEmail);

    /**
     * 根据当前登录用户ID提升为超级管理员（需认证通过）
     * @param isAuthenticated 认证是否通过（true=通过，false=不通过）
     */
    void upgradeToAdmin(boolean isAuthenticated);

    /**
     * 根据ID查询单个用户
     * @param id 要查询的用户ID
     * @return 对应的用户对象；如果不存在则返回null
     */
    User getUserById(Integer id);

    /**
     * 根据邮箱查询用户
     * @param email 用户邮箱（需符合格式，如QQ邮箱）
     * @return 匹配的用户对象（无匹配时返回null）
     */
    User getUserByEmail(String email);

    /**
     * 分批获取所有用户ID（避免一次性加载过多数据）
     * @param batchSize 每批查询的数量
     * @return 所有用户ID的列表
     */
    List<Integer> getAllUserIdsByBatch(int batchSize);
}