package org.myfx.controls.aione.AiService.service.base.ai_chat_db.impl;

import cn.hutool.core.bean.BeanUtil;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.dto.AiRoleAddDTO;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiRole;
import org.myfx.controls.aione.AiService.mapper.ai_chat_db.AiRoleMapper;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiRoleService;
import org.myfx.controls.aione.ServiceCommon.context.UserContext;
import org.myfx.controls.aione.ServiceCommon.service.upper.ImageUploadService;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * AI角色业务层实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiRoleServiceImpl implements AiRoleService {

    @Resource
    private AiRoleMapper aiRoleMapper;

    @Resource
    private ImageUploadService imageUploadService;

    private static final long MAX_AVATAR_SIZE = 2 * 1024 * 1024;

    @Override
    public AiRole getRoleByCode(String roleCode) {
        // 非空校验：roleCode为空时抛出IllegalArgumentException
        Assert.notNull(roleCode, "角色编码（roleCode）不能为空！");

        // 调用Mapper查询
        return aiRoleMapper.selectByCode(roleCode);
    }

    /**
     * 根据角色ID查询AI角色（业务层实现）
     */
    @Override
    public AiRole getByRoleId(Integer roleId) {
        // 非空校验：roleId为空时抛出IllegalArgumentException（和getRoleByCode完全一致）
        Assert.notNull(roleId, "角色ID（roleId）不能为空！");

        // 调用Mapper的selectById方法查询
        return aiRoleMapper.selectById(roleId);
    }

    // ==================== 新增AI角色实现 ====================
    @Override
    public void addAiRole(AiRoleAddDTO addDTO, MultipartFile roleAvatar) {
        // 1. 入参非空校验
        Assert.notNull(addDTO, "AI角色新增参数不能为空！");

        // 2. DTO 转换为实体类
        AiRole aiRole = new AiRole();
        BeanUtil.copyProperties(addDTO, aiRole);

        // 3. 自动生成角色编码
        aiRole.setRoleCode("AGENT_" + System.currentTimeMillis());

        // 4. 赋值创建人ID
        Integer userId = UserContext.getUserId();
        aiRole.setCreateUserId(userId);

        // ==================== 头像上传逻辑（直接使用传入的文件） ====================
        if (roleAvatar != null && !roleAvatar.isEmpty()) {
            try {
                // 文件大小校验
                if (roleAvatar.getSize() > MAX_AVATAR_SIZE) {
                    log.warn("角色头像上传失败：文件超过2MB限制");
                } else {
                    // 上传头像
                    String avatarPath = imageUploadService.uploadAiRoleAvatar(roleAvatar, userId);
                    aiRole.setAvatarPath(avatarPath);
                    log.info("角色头像上传成功：{}", avatarPath);
                }
            } catch (Exception e) {
                // 上传失败不影响角色创建
                log.error("角色头像上传失败，已跳过", e);
            }
        }

        // 5. 必填字段校验
        Assert.notNull(aiRole.getRoleCode(), "角色编码不能为空！");
        Assert.notNull(aiRole.getPersonaCore(), "人设核心定义不能为空！");

        // 6. 入库
        aiRoleMapper.insert(aiRole);
    }

    // ==================== 根据用户ID查询角色列表 ====================
    @Override
    public List<AiRole> getRoleListByUserId(Integer userId) {
        Assert.notNull(userId, "用户ID不能为空！");
        return aiRoleMapper.selectListByUserId(userId);
    }

    // ==================== 获取当前登录用户的角色列表 ====================
    @Override
    public List<AiRole> getMyRoleList() {
        Integer userId = UserContext.getUserId();
        return getRoleListByUserId(userId);
    }

    // ==================== 更新AI角色实现 ====================
    @Override
    public int updateAiRole(AiRole aiRole) {
        // 1. 基础非空校验
        Assert.notNull(aiRole, "AI角色实体不能为空！");
        // 2. 主键校验
        Assert.notNull(aiRole.getRoleId(), "角色ID不能为空！");
        // 3. 调用Mapper主键更新
        return aiRoleMapper.updateById(aiRole);
    }
}