package org.myfx.controls.aione.AiService.service.base.ai_chat_db;

import org.myfx.controls.aione.AiService.entity.ai_chat_db.UserAiRoleBind;

import java.util.List;

public interface UserAiRoleBindService {

    /**
     * 绑定用户与AI角色（新增关联）
     */
    void bindUserAiRole(UserAiRoleBind bind);

    // ==================== 初始化绑定（硬编码角色1） ====================
    /**
     * 初始化用户默认AI角色绑定（固定绑定角色ID=1）
     * @param userId 用户ID
     */
    void initUserRoleBind(Integer userId);

    /**
     * 获取当前登录用户的 AI角色绑定关系列表
     * @return 用户-角色绑定关系集合
     */
    List<UserAiRoleBind> getMyUserAiRoleBindList();

    /**
     * 解除用户与AI角色的绑定关系
     * @param userId 用户ID
     * @param roleId 角色ID
     */
    void unbindUserAiRole(Integer userId, Integer roleId);

}