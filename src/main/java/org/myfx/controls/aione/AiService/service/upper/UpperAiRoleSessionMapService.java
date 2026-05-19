package org.myfx.controls.aione.AiService.service.upper;

import java.util.List;

public interface UpperAiRoleSessionMapService {

    /**
     * 删除角色聊天记录（非物理删除：解绑角色 + 会话回收）
     * 角色本身保留、会话数据保留、消息保留，仅解除绑定+移入回收站，用户可复原
     *
     * @param roleId 角色ID
     * @param sessionUuids 会话UUID数组
     */
    void deleteRoleChatRecords(Integer roleId, List<String> sessionUuids);

}
