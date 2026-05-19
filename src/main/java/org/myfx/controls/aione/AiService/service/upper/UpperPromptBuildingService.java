package org.myfx.controls.aione.AiService.service.upper;

/**
 * 上层提示词构建服务接口
 * 负责拼接用户画像、AI性格/心情等提示词，生成完整的AI对话提示词
 */
public interface UpperPromptBuildingService {

    /**
     * 构建完整的提示词（基于用户ID和会话ID）
     * @param userId 用户ID（用于获取用户基础/爱好画像提示词）
     * @param sessionId 会话ID（用于获取AI性格和心情提示词）
     * @return 拼接后的完整提示词（包含用户画像+AI性格/心情+要求语句）
     */
    String buildFullSystemPrompt(Integer userId, Long sessionId);

}