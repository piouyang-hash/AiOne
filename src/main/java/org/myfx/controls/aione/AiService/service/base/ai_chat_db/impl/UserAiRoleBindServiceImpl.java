package org.myfx.controls.aione.AiService.service.base.ai_chat_db.impl;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.UserAiRoleBind;
import org.myfx.controls.aione.AiService.mapper.ai_chat_db.UserAiRoleBindMapper;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.UserAiRoleBindService;
import org.myfx.controls.aione.ServiceCommon.context.UserContext;
import org.myfx.controls.aione.ServiceCommon.utils.SnowflakeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAiRoleBindServiceImpl implements UserAiRoleBindService {

    @Resource
    private UserAiRoleBindMapper userAiRoleBindMapper;

    @Override
    public void bindUserAiRole(UserAiRoleBind bind) {
        // 非空校验
        Assert.notNull(bind, "绑定实体不能为空");
        Assert.notNull(bind.getUserId(), "用户ID不能为空");
        Assert.notNull(bind.getRoleId(), "角色ID不能为空");
        Assert.notNull(bind.getId(), "雪花主键不能为空");

        // 插入绑定关系
        userAiRoleBindMapper.insert(bind);
    }

    // ==================== 核心：初始化绑定方法 ====================
    @Override
    public void initUserRoleBind(Integer userId) {
        // 1. 非空校验
        Assert.notNull(userId, "用户ID不能为空！");

        // 2. 构建绑定关系
        UserAiRoleBind bind = new UserAiRoleBind();
        bind.setId(SnowflakeGenerator.generateId()); // 主键ID（对应表id字段）
        bind.setUserId(userId);            // 传入用户ID
        bind.setRoleId(1);                 // 硬编码：固定绑定角色ID=1

        // 3. 调用原有绑定方法（复用校验+插入）
        bindUserAiRole(bind);
        log.info("用户ID:{} 初始化默认AI角色绑定成功", userId);
    }

    // ==================== 获取当前用户绑定列表 ====================
    @Override
    public List<UserAiRoleBind> getMyUserAiRoleBindList() {
        // 1. 获取当前登录用户ID
        Integer userId = UserContext.getUserId();
        Assert.notNull(userId, "获取绑定关系失败：用户未登录");

        // 2. 查询完整绑定列表
        return userAiRoleBindMapper.selectListByUserId(userId);
    }

    @Override
    public void unbindUserAiRole(Integer userId, Integer roleId) {
        // 1. 入参非空校验（严格对齐你的规范）
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(roleId, "角色ID不能为空");

        // 2. 调用Mapper执行解除绑定
        int affectRows = userAiRoleBindMapper.deleteByUserIdAndRoleId(userId, roleId);

        // 3. 标准日志打印
        if (affectRows > 0) {
            log.info("[角色绑定解除] 用户{} 成功解除角色{}绑定", userId, roleId);
        } else {
            log.warn("[角色绑定解除] 用户{} 未找到角色{}的绑定关系，解除操作无效", userId, roleId);
        }
    }
}