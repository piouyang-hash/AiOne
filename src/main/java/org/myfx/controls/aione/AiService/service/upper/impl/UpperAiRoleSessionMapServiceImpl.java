package org.myfx.controls.aione.AiService.service.upper.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiChatSessionService;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.UserAiRoleBindService;
import org.myfx.controls.aione.AiService.service.upper.UpperAiRoleSessionMapService;
import org.myfx.controls.aione.ServiceCommon.context.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * AI角色与会话映射 顶层业务实现
 * 【核心说明】：
 * 1. 并非真正物理删除任何数据
 * 2. 角色本身正常保留
 * 3. 仅解除用户与角色的绑定关系
 * 4. 会话仅移入回收站，数据/消息完整保留
 * 5. 用户支持后续复原操作
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UpperAiRoleSessionMapServiceImpl implements UpperAiRoleSessionMapService {

    private final AiChatSessionService aiChatSessionService;
    private final UserAiRoleBindService userAiRoleBindService;

    /**
     * 事务保障：解绑 + 会话回收 原子性执行
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoleChatRecords(Integer roleId, List<String> sessionUuids) {
        // 获取当前登录用户ID
        Integer userId = UserContext.getUserId();

        // 1. 解除用户与AI角色的绑定关系
        userAiRoleBindService.unbindUserAiRole(userId, roleId);
        log.info("[角色会话清理] 用户{} 已解除角色{}的绑定关系", userId, roleId);

        // 2. 根据UUID数组批量查询会话ID
        List<Long> sessionIds = aiChatSessionService.batchGetSessionIdsByUuids(userId, sessionUuids);
        log.info("[角色会话清理] 用户{} 匹配到待回收会话{}个", userId, sessionIds.size());

        // 3. 批量将会话移入回收站（非物理删除）
        aiChatSessionService.batchRecycleSessions(userId, sessionIds);
        log.info("[角色会话清理] 用户{} 角色{}的会话已全部回收完成", userId, roleId);
    }
}