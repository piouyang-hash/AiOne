package org.myfx.controls.aione.AiService.service.upper.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiChatSessionService;
import org.myfx.controls.aione.AiService.service.upper.UpperChatPreCheckService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 上层对话前置检验服务实现类
 * 实现会话校验、创建、激活的核心逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UpperChatPreCheckServiceImpl implements UpperChatPreCheckService {

    // 注入底层会话服务（依赖原有AiChatSessionService）
    private final AiChatSessionService aiChatSessionService;


    @Override
    @Transactional(rollbackFor = Exception.class) // 补充rollbackFor，确保所有异常回滚
    public Long chatPreCheck(Integer userId, String sessionUuid, Integer roleId) {
        // 第一步：入参校验（强化版）
        // 1.1 校验用户ID非空且为正整数
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID不能为空且需为正整数");
        }
        // 1.2 校验会话UUID非空（null/空字符串抛RuntimeException，严格匹配你的要求）
        if (sessionUuid == null || sessionUuid.isBlank()) {
            throw new RuntimeException("会话UUID不能为空（必须为标准UUIDv4格式）");
        }

        // 🔥 新增：roleId为空则默认赋值为1（无校验，允许空）
        roleId = (roleId == null) ? 1 : roleId;

        // 第二步：调用getSessionIdByUUID，将UUID转换为会话ID（雪花ID）
        Long sessionId = aiChatSessionService.getSessionIdByUUID(sessionUuid, userId);

        // 第三步：根据转换结果分支处理
        if (sessionId == null) {
            // 3.1 UUID无匹配的会话ID → 创建新会话（核心修改：传入sessionUuid，填充UUID字段）
            Long newSessionId = aiChatSessionService.createUserChatSession(userId, sessionUuid, roleId);
            log.info("用户[{}]的会话UUID[{}]无匹配会话，已创建新会话 | 新会话ID：{} | 绑定UUID：{}",
                    userId, sessionUuid, newSessionId, sessionUuid);
            return newSessionId; // 返回新创建的有效会话ID
        } else {
            // 3.2 UUID匹配到会话ID → 校验并激活会话（复用原有逻辑）
            boolean isSessionActive = aiChatSessionService.checkSessionIsActive(userId, sessionId);
            if (!isSessionActive) {
                // 3.2.1 会话未激活 → 关闭当前用户所有会话
                int closeRows = aiChatSessionService.closeAllSessionsByUserId(userId);
                log.info("用户[{}]批量关闭所有会话，影响行数：{}", userId, closeRows);

                // 执行激活操作（失败抛异常，保证事务回滚）
                boolean activateSuccess = aiChatSessionService.activateSessionByUserAndSession(userId, sessionId);
                if (activateSuccess) {
                    log.info("用户[{}]通过UUID[{}]激活会话ID：{}", userId, sessionUuid, sessionId);
                } else {
                    String errorMsg = "用户[" + userId + "]尝试通过UUID[" + sessionUuid + "]激活会话" + sessionId + "失败";
                    log.error(errorMsg);
                    throw new RuntimeException(errorMsg); // 激活失败抛异常，触发事务回滚
                }
            } else {
                // 3.2.2 会话已激活 → 无操作，直接返回
                log.info("用户[{}]的UUID[{}]对应的会话ID{}已激活，无需执行额外操作",
                        userId, sessionUuid, sessionId);
            }
            // 非空分支：返回转换后的有效会话ID
            return sessionId;
        }
    }

}