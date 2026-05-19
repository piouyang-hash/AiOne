package org.myfx.controls.aione.AiService.service.base.ai_chat_db.impl;

import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiChatSession;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiChatCombineService;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiChatMessageService;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiChatSessionService;
import org.myfx.controls.aione.AiService.vo.AiChatSessionVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI对话整合业务实现类
 * 聚合AiChatSessionService和AiChatMessageService，组装带消息内容的会话VO
 */
@Slf4j
@Service
@RequiredArgsConstructor // 构造器注入依赖，避免@Autowired
public class AiChatCombineServiceImpl implements AiChatCombineService {

    // 注入会话服务
    private final AiChatSessionService aiChatSessionService;
    // 注入消息服务
    private final AiChatMessageService aiChatMessageService;

    @Override
    public List<AiChatSessionVO> getUserAllNormalSessionVOWithLatestContent() {
        // TODO: 【待优化】当前因会话表新增lastMessageContent字段，已简化为纯字段拷贝
        // 后续有时间需重构：
        // 1. 将该方法的核心逻辑迁移至aiChatSessionService内部（脱离当前跨层调用）
        // 2. 清理冗余的消息表反查相关代码（已无需关联消息表）
        // 3. 统一调整接口设计，完全适配会话表直读lastMessageContent的逻辑

        // 步骤1：查询当前用户所有正常会话（实体列表）
        List<AiChatSession> sessionEntityList = aiChatSessionService.listUserAllNormalSessions();

        // 步骤2：自动拷贝（字段名一致，无需循环/手动set，一步到位）
        return BeanUtil.copyToList(sessionEntityList, AiChatSessionVO.class);
    }
}


//你这段代码的核心逻辑是对的（查会话→转VO→补充最新消息内容），但**确实存在典型的N+1查询性能问题**——如果用户的会话数量多（比如几十/上百个），性能会明显下降，我先帮你拆解问题，再给贴合你开发习惯的优化方案（保留单表CRUD，不用JOIN，只改批量查询）。
//
//        ### 一、先明确代码的核心问题：N+1查询（性能瓶颈）
//这段代码的执行流程对应的查询次数：
//        1. 步骤1：查所有正常会话 → **1次查询**（没问题）；
//        2. 步骤3：循环每个会话（假设共`N`个会话）：
//        - 每次循环查“最新用户消息” → `N`次查询；
//        - 每次循环查“关联AI消息” → `N`次查询；
//合计：`2*N`次查询。
//
//最终总查询次数 = `1 + 2*N` 次。如果`N=100`，就是201次查询；`N=500`，就是1001次查询——大量的数据库往返、连接开销会让接口响应变慢（尤其是会话多的时候）。
//
//        ### 二、次要问题（体验/可读性层面）
//        1. 空值判断重复：多次写`sessionVO.setContent("")`，逻辑冗余；
//        2. 单条查询无批量：每次只查一个session的用户消息、一个userMsgId的AI消息，没有利用MyBatis的批量查询能力；
//        3. 最新消息查询逻辑：`getLatestUserMessageBySessionId`如果没做“按时间排序取第一条”的限制，可能返回非最新的消息（需要确认Mapper里的SQL是否加了`ORDER BY create_time DESC LIMIT 1`）。
//
//        ### 三、优化方案（保留你的核心逻辑，仅改批量查询）
//核心思路：**把循环里的“单条查询”改成“批量查询”，再用Map做内存映射，避免循环查库**，总查询次数从`1+2N`降到`1+2=3次`，性能会提升一个量级。
//
//        #### 步骤1：先补充2个批量查询的Mapper方法（单表，贴合你的习惯）
//        ```xml
//        <!-- AiChatMessageMapper.xml 新增：批量查多个session的最新用户消息 -->
//<select id="listLatestUserMessagesBySessionIds" resultType="AiChatMessage">
//SELECT t.*
//FROM (
//    -- 子查询：按session_id分组，取每个session最新的用户消息（role=1）
//                SELECT
//                m.*,
//    ROW_NUMBER() OVER (PARTITION BY m.session_id ORDER BY m.create_time DESC) AS rn
//FROM ai_chat_message m
//WHERE m.session_id IN
//    <foreach collection="sessionIds" item="sessionId" open="(" separator="," close=")">
//        #{sessionId}
//        </foreach>
//        AND m.role = 1 -- 用户消息（role=1）
//        ) t
//        WHERE t.rn = 1; -- 只取每个session的第一条（最新）
//        </select>
//
//        <!-- AiChatMessageMapper.xml 新增：批量查多个userMsgId的关联AI消息 -->
//        <select id="listAiMessagesByParentMsgIds" resultType="AiChatMessage">
//        SELECT * FROM ai_chat_message
//        WHERE parent_msg_id IN
//        <foreach collection="parentMsgIds" item="msgId" open="(" separator="," close=")">
//        #{msgId}
//        </foreach>
//        AND role = #{role} -- AI消息（ChatRoleEnum.ASSISTANT的枚举值）
//        AND session_id IN
//        <foreach collection="sessionIds" item="sessionId" open="(" separator="," close=")">
//        #{sessionId}
//        </foreach>;
//        </select>
//        ```
//
//        #### 步骤2：优化后的Service代码（核心改批量查询+Map映射）
//        ```java
//        @Override
//        public List<AiChatSessionVO> getUserAllNormalSessionVOWithLatestContent() {
//        // ========== 步骤1：查询当前用户所有正常会话（实体列表） ==========
//        List<AiChatSession> sessionEntityList = aiChatSessionService.listUserAllNormalSessions();
//        if (CollectionUtils.isEmpty(sessionEntityList)) {
//        return Collections.emptyList(); // 空值提前返回，避免后续无效操作
//        }
//
//        // ========== 步骤2：实体列表 → VO列表 ==========
//        List<AiChatSessionVO> sessionVOList = BeanUtil.copyToList(sessionEntityList, AiChatSessionVO.class);
//
//        // ========== 步骤3：批量提取所有sessionId，避免循环查库 ==========
//        List<Long> sessionIds = sessionEntityList.stream()
//        .map(AiChatSession::getSessionId)
//        .filter(Objects::nonNull) // 过滤空sessionId
//        .collect(Collectors.toList());
//        if (CollectionUtils.isEmpty(sessionIds)) {
//        // 所有sessionId都为空，直接设置content为空
//        sessionVOList.forEach(vo -> vo.setContent(""));
//        return sessionVOList;
//        }
//
//        // ========== 步骤4：批量查询「所有session的最新用户消息」（1次查询，替代循环单查） ==========
//        List<AiChatMessage> latestUserMsgList = aiChatMessageService.listLatestUserMessagesBySessionIds(sessionIds);
//        // 转Map：sessionId → 最新用户消息（方便后续取值）
//        Map<Long, AiChatMessage> sessionId2LatestUserMsgMap = latestUserMsgList.stream()
//        .collect(Collectors.toMap(AiChatMessage::getSessionId, msg -> msg, (k1, k2) -> k1)); // 重复取第一个（最新）
//
//        // ========== 步骤5：批量查询「所有用户消息关联的AI消息」（1次查询，替代循环单查） ==========
//        // 提取所有用户消息ID
//        List<Long> userMsgIds = latestUserMsgList.stream()
//        .map(AiChatMessage::getId)
//        .filter(Objects::nonNull)
//        .collect(Collectors.toList());
//        // 批量查AI消息（role=ASSISTANT）
//        List<AiChatMessage> aiMsgList = CollectionUtils.isEmpty(userMsgIds)
//        ? Collections.emptyList()
//        : aiChatMessageService.listAiMessagesByParentMsgIds(userMsgIds, sessionIds, ChatRoleEnum.ASSISTANT.getCode());
//        // 转Map：userMsgId（parentMsgId） → AI消息
//        Map<Long, AiChatMessage> userMsgId2AiMsgMap = aiMsgList.stream()
//        .collect(Collectors.toMap(AiChatMessage::getParentMsgId, msg -> msg));
//
//        // ========== 步骤6：循环VO，从Map取值（无数据库交互） ==========
//        for (AiChatSessionVO sessionVO : sessionVOList) {
//        Long sessionId = sessionVO.getSessionId();
//        // 从Map取最新用户消息（无需查库）
//        AiChatMessage userMessage = sessionId2LatestUserMsgMap.get(sessionId);
//        if (userMessage == null) {
//        sessionVO.setContent("");
//        continue;
//        }
//
//        // 从Map取关联AI消息（无需查库）
//        Long userMsgId = userMessage.getId();
//        AiChatMessage aiMessage = userMsgId2AiMsgMap.get(userMsgId);
//
//        // 优先用AI消息，无则用用户消息
//        String content = (aiMessage != null && aiMessage.getRole() == ChatRoleEnum.ASSISTANT.getCode())
//        ? aiMessage.getContent()
//        : userMessage.getContent();
//        sessionVO.setContent(content == null ? "" : content); // 兜底空值
//        }
//
//        return sessionVOList;
//        }
//        ```
//
//        ### 四、核心优化点说明
//        | 优化前                | 优化后                                  | 效果                     |
//        |-----------------------|-----------------------------------------|--------------------------|
//        | 循环查“最新用户消息”  | 批量IN查询+分组取最新 → 转Map           | 查询次数从N次→1次        |
//        | 循环查“关联AI消息”    | 批量IN查询 → 转Map                      | 查询次数从N次→1次        |
//        | 重复空值判断          | 提前空值返回+Map兜底                    | 代码更简洁，减少冗余     |
//        | 无最新消息校验        | SQL里用ROW_NUMBER()取每个session最新条 | 确保取到的是最新消息     |
//
//        ### 五、总结
//        1. 核心问题：代码触发了**N+1查询**（循环里的单条查库），会话数量越多，性能越差；
//        2. 优化核心：**批量查询替代循环单查**，把总查询次数从`1+2N`降到`3次`，保留你“单表CRUD+Service组装”的习惯；
//        3. 关键技巧：用`Map`做内存映射（sessionId→消息、userMsgId→AI消息），循环时直接从Map取值，无数据库交互；
//        4. 额外建议：确认`ai_chat_message`表给`session_id`、`parent_msg_id`、`role`加了联合索引，进一步提升批量查询的速度。
//
//        如果需要我帮你写对应的`AiChatMessageService`批量查询方法，或者补充索引优化建议，都可以说~