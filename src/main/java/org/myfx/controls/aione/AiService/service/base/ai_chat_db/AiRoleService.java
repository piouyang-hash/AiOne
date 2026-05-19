package org.myfx.controls.aione.AiService.service.base.ai_chat_db;

import org.myfx.controls.aione.AiService.dto.AiRoleAddDTO;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiRole;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * AI角色业务层接口
 */
public interface AiRoleService {

    /**
     * 根据角色编码查询AI角色
     * @param roleCode 角色编码（非空）
     * @return AI角色实体
     */
    AiRole getRoleByCode(String roleCode);

    /**
     * 根据角色ID查询AI角色（业务层方法）
     * @param roleId 角色ID（非空）
     * @return AI角色实体
     */
    AiRole getByRoleId(Integer roleId);

    // ==================== 新增业务方法 ====================
    /**
     * 新增AI角色
     * @param addDTO AI角色基础信息
     * @param roleAvatar 头像文件（可选）
     */
    void addAiRole(AiRoleAddDTO addDTO, MultipartFile roleAvatar);

    /**
     * 根据创建人ID查询AI角色列表
     * @param userId 创建人ID
     * @return 角色列表
     */
    List<AiRole> getRoleListByUserId(Integer userId);

    /**
     * 获取当前登录用户创建的AI角色列表
     * 只能获取创建的角色，不能获得用户所有角色（少了公共的角色）
     * @return 角色列表
     */
    List<AiRole> getMyRoleList();

    /**
     * 更新AI角色（根据 角色ID 条件更新）
     * @param aiRole AI角色实体
     * @return 影响行数
     */
    int updateAiRole(AiRole aiRole);
}