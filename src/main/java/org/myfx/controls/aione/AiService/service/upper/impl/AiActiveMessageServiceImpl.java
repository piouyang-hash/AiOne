package org.myfx.controls.aione.AiService.service.upper.impl;

import lombok.RequiredArgsConstructor;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiEmotionRealState;
import org.myfx.controls.aione.AiService.service.base.status.ai_behavior_db.AiEmotionRealStateService;
import org.myfx.controls.aione.AiService.service.upper.AiActiveMessageService;
import org.springframework.stereotype.Service;

/**
 * AI主动消息服务实现类（落地AI主动触达的主动性校验逻辑）
 */
@Service
@RequiredArgsConstructor
public class AiActiveMessageServiceImpl implements AiActiveMessageService {

    // 注入AI情绪状态核心服务
    private final AiEmotionRealStateService aiEmotionRealStateService;

    /**
     * 检查AI主动消息的主动性：获取情绪状态→打印实体→返回活跃度
     */
    @Override
    public int checkActiveInitiative(Integer userId) {
        // 1. 入参非空校验（保障主动校验逻辑的健壮性）
        if (userId == null) {
            throw new IllegalArgumentException("检查AI主动消息主动性失败：用户ID不能为空");
        }

        // 2. 调用情绪服务获取当前AI感情状态（核心依赖）
        AiEmotionRealState currentEmotion = aiEmotionRealStateService.getCurrentAiEmotion(userId);

        // 3. 空值处理：若未查询到情绪状态，抛出异常（或根据业务返回默认值50）
        if (currentEmotion == null) {
            throw new RuntimeException("检查AI主动消息主动性失败：未查询到用户[" + userId + "]的AI情绪状态");
            // 若业务允许默认值，可替换为：return 50;
        }

        // 5. 返回活跃度值（核心：活跃度直接体现AI主动消息的主动性）
        return currentEmotion.getActivityValue();
    }

    @Override
    public String checkAiFamiliarity(Integer userId) {
        // 1. 入参非空校验（保障熟悉度检查逻辑的健壮性）
        if (userId == null) {
            throw new IllegalArgumentException("检查AI熟悉度失败：用户ID不能为空");
        }

        // 2. 调用情绪服务获取当前AI感情状态
        AiEmotionRealState currentEmotion = aiEmotionRealStateService.getCurrentAiEmotion(userId);

        // 3. 空值处理：未查询到情绪状态时，默认熟悉度为0（执行「询问未知信息」逻辑）
        Integer familiarity;
        if (currentEmotion == null) {
            familiarity = 0;
            System.out.println("【AI主动消息-熟悉度检查】用户ID：" + userId + "，未查询到情绪状态，默认熟悉度0");
        } else {
            familiarity = currentEmotion.getFamiliarity();
        }

        // 4. 根据熟悉度生成对应的系统指令消息（核心：系统向AI提要求）
        String systemMessage;
        if (familiarity > 30) {
            // 熟悉度达标：下达「发起问候」的系统指令
            systemMessage = "【系统消息】：主动向用户发起亲切的问候，贴合用户已知的喜好和性格，语气自然不生硬！";
        } else {
            // 熟悉度未达标：下达「询问未知信息」的系统指令（优先名称/爱好）
            systemMessage = "【系统消息】：主动向用户询问未知的信息，名称或者爱好要优先！";
        }

        // 5. 打印检查结果+系统指令（便于调试）
        System.out.println("【AI主动消息-熟悉度检查】用户ID：" + userId +
                "，当前熟悉度：" + familiarity + "，生成系统指令：" + systemMessage);

        return systemMessage;
    }
}