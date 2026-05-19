package org.myfx.controls.aione.AiService.service.upper;

/**
 * 上层对话前置检验服务接口
 * 核心功能：校验会话ID，按需创建新会话或激活已有会话
 */
public interface UpperChatPreCheckService {

    /**
     * 对话前置检验核心方法
     * @param userId 用户ID（非空）
     * @param sessionUuid 会话UUID（标准UUID v4格式，<font color="red">不能为空/空字符串</font>，否则抛运行时异常）
     *                  - 若UUID无匹配的会话ID：创建新会话并返回新生成的有效会话ID（雪花ID）
     *                  - 若UUID匹配到会话ID：校验会话激活状态，未激活则激活（激活失败抛异常），最终返回该会话ID（确保可用）
     * @param roleId 角色ID（非空，正整数）
     * @return Long 始终返回正确可用的会话ID（雪花ID，无null/无效值）
     */
    Long chatPreCheck(Integer userId, String sessionUuid, Integer roleId);

}