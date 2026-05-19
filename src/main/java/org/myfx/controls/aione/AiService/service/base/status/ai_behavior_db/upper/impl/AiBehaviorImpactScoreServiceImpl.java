package org.myfx.controls.aione.AiService.service.base.status.ai_behavior_db.upper.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.common.ai_chat_db.AiBehaviorEnum;
import org.myfx.controls.aione.AiService.common.ai_chat_db.EmotionTypeEnum;
import org.myfx.controls.aione.AiService.dto.AiEmotionScoreOperateDTO;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiBehaviorScore;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.BaseAiBehavior;
import org.myfx.controls.aione.AiService.service.base.status.ai_behavior_db.AiBehaviorScoreService;
import org.myfx.controls.aione.AiService.service.base.status.ai_behavior_db.AiEmotionLogDetailService;
import org.myfx.controls.aione.AiService.service.base.status.ai_behavior_db.AiEmotionRealStateService;
import org.myfx.controls.aione.AiService.service.base.status.ai_behavior_db.BaseAiBehaviorService;
import org.myfx.controls.aione.AiService.service.base.status.ai_behavior_db.upper.AiBehaviorImpactScoreService;
import org.myfx.controls.aione.AiService.utils.TimeWindowRedisUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

/**
 * AI行为影响分数业务实现类
 * 处理AI行为（如等待行为）触发的用户情绪分值变动
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiBehaviorImpactScoreServiceImpl implements AiBehaviorImpactScoreService {

    // 注入AI行为基础配置服务
    private final BaseAiBehaviorService baseAiBehaviorService;
    private final AiEmotionRealStateService aiEmotionRealStateService;
    private final AiEmotionLogDetailService aiEmotionLogDetailService;
    private final AiBehaviorScoreService aiBehaviorScoreService;

    // ====================== 【新增】注入时间窗口限流工具 ======================
    private final TimeWindowRedisUtil timeWindowRedisUtil;

    // 限流KEY前缀（静态常量，规范管理）
    private static final String LIKE_LIMIT_KEY_PREFIX = "ai:user:like:limit:";

    /**
     * 处理AI等待行为（触发用户情绪分值调整）
     * 注：AI等待行为会根据传入的分值调整用户情绪分（可正可负）
     *
     * @param userId 用户ID（明确指定受影响的用户）
     * @param score  AI等待行为对应的变动分值（可正可负，范围-100~100）
     * @return 操作结果：true=成功（分值有变动/执行完成），false=失败/无变动
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务兜底，异常回滚
    public boolean handleAiWaitBehavior(Integer userId, Integer score) {
        // 1. 核心参数合法性校验（优先校验userId，再校验score）
        Assert.notNull(userId, "用户ID不能为空");
        Assert.isTrue(userId > 0, "用户ID需为正整数");

        // 新增：分数参数校验（仅校验核心规则，符合要求）
        Assert.notNull(score, "AI等待行为变动分值不能为空");
        Assert.isTrue(score >= -100 && score <= 100, "AI等待行为分值需在-100~100范围内");

        // 2. 获取“AI等待”行为配置（对应AiBehaviorEnum.WAIT枚举）
        BaseAiBehavior waitBehavior = baseAiBehaviorService.getAiBehaviorByCode(AiBehaviorEnum.WAIT);
        if (waitBehavior == null) {
            log.warn("处理AI等待行为失败：未查询到【AI等待】行为配置（AiBehaviorEnum.WAIT），用户ID：{}，变动分值：{}", userId, score);
            return false; // 无行为配置，返回无变动
        }

        // 3. 执行行为分值影响逻辑（传入用户指定的score，无需预留计算）
        boolean executeResult = executeAiBehaviorImpact(userId, waitBehavior, score);

        // 4. 日志输出 & 返回结果（补充分值信息，便于排查）
        if (executeResult) {
            log.info("处理AI等待行为成功，用户ID：{}，行为配置ID：{}，变动分值：{}",
                    userId, waitBehavior.getBehaviorId(), score);
        } else {
            log.info("处理AI等待行为无变动，用户ID：{}，行为配置ID：{}，变动分值：{}",
                    userId, waitBehavior.getBehaviorId(), score);
        }
        return executeResult;
    }

    /**
     * 处理「AI接收用户发送聊天消息」行为（修改AI活跃度 + 喜爱值分值）
     * 注：
     * 1. 仅需用户ID，分值固定取数据库中 USER_SEND_MSG 行为配置的 ACTIVITY / LIKE 类型分值；
     * 2. 扣减/增加后分值不低于0（由executeAiBehaviorImpact方法兜底）；
     * 3. 老老实实调用两次方法，分别更新活跃度、喜爱值
     *
     * @param userId 用户ID（关联对应的AI实例，修改其情绪值）
     * @return 操作结果：true=至少一项成功，false=全部失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleAiUserSendMsgBehavior(Integer userId) {
        // 1. 核心参数合法性校验
        Assert.notNull(userId, "用户ID不能为空");
        Assert.isTrue(userId > 0, "用户ID需为正整数");

        // 2. 获取「AI接收用户发送聊天消息」行为基础配置
        BaseAiBehavior userSendMsgBehavior = baseAiBehaviorService.getAiBehaviorByCode(AiBehaviorEnum.USER_SEND_MSG);
        if (userSendMsgBehavior == null) {
            log.warn("处理AI接收用户消息行为失败：未查询到【AI接收用户发送聊天消息】行为配置，用户ID：{}", userId);
            return false;
        }

        // 3. 查询该行为的所有分值配置（活跃度+喜爱值）
        List<AiBehaviorScore> scoreList = aiBehaviorScoreService.getScoreListByBehaviorCode(AiBehaviorEnum.USER_SEND_MSG);
        if (scoreList == null || scoreList.isEmpty()) {
            log.warn("处理AI接收用户消息行为失败：未查询到【USER_SEND_MSG】对应的任何分值配置，用户ID：{}", userId);
            return false;
        }

        // 拆分获取 活跃度、喜爱值 配置
        AiBehaviorScore activityScore = scoreList.stream()
                .filter(score -> EmotionTypeEnum.ACTIVITY.equals(score.getScoreType()))
                .findFirst().orElse(null);
        AiBehaviorScore likeScore = scoreList.stream()
                .filter(score -> EmotionTypeEnum.LIKE.equals(score.getScoreType()))
                .findFirst().orElse(null);

        boolean finalResult = false;

        // ====================== 第一次调用：处理 活跃度（无限流） ======================
        if (activityScore != null && activityScore.getScoreVal() != null) {
            Integer scoreVal = activityScore.getScoreVal();
            Assert.isTrue(scoreVal >= -100 && scoreVal <= 100,
                    () -> String.format("【AI接收用户发送聊天消息】活跃度分值非法，值：%d，用户ID：%d", scoreVal, userId));
            boolean result = executeAiBehaviorImpact(userId, userSendMsgBehavior, scoreVal, EmotionTypeEnum.ACTIVITY);
            finalResult = result;
            log.info("处理AI接收用户消息-活跃度：用户ID={}，变动分值={}，结果={}", userId, scoreVal, result);
        } else {
            log.warn("【AI接收用户发送聊天消息】未配置活跃度分值，跳过处理，用户ID：{}", userId);
        }

        // ====================== 第二次调用：处理 喜爱值（新增Redis限流判断） ======================
        if (likeScore != null && likeScore.getScoreVal() != null) {
            Integer scoreVal = likeScore.getScoreVal();
            Assert.isTrue(scoreVal >= -100 && scoreVal <= 100,
                    () -> String.format("【AI接收用户发送聊天消息】喜爱值分值非法，值：%d，用户ID：%d", scoreVal, userId));

            // ====================== 【核心限流逻辑】 ======================
            // 构建用户唯一限流KEY
            String limitKey = LIKE_LIMIT_KEY_PREFIX + userId;
            // 校验是否允许上涨喜爱值：false=触发限流，直接跳过不处理
            if (!timeWindowRedisUtil.isAllowed(limitKey)) {
                log.info("【AI接收用户发送聊天消息】用户ID={} 喜爱值已触发时间窗口限流，跳过更新", userId);
                return finalResult;
            }

            // 限流通过 → 执行喜爱值更新
            boolean result = executeAiBehaviorImpact(userId, userSendMsgBehavior, scoreVal, EmotionTypeEnum.LIKE);
            finalResult = finalResult || result;
            log.info("处理AI接收用户消息-喜爱值：用户ID={}，变动分值={}，结果={}", userId, scoreVal, result);
        } else {
            log.warn("【AI接收用户发送聊天消息】未配置喜爱值分值，跳过处理，用户ID：{}", userId);
        }

        // 最终日志
        log.info("处理AI接收用户消息行为完成，用户ID：{}，行为配置ID：{}，最终结果：{}",
                userId, userSendMsgBehavior.getBehaviorId(), finalResult);
        return finalResult;
    }

    /**
     * 处理「AI主动发送聊天消息」行为（修改AI活跃度 + 喜爱值分值）
     * 注：
     * 1. 仅需用户ID，分值固定取数据库中 AI_ACTIVE_SEND_MSG 行为配置的 ACTIVITY / LIKE 类型分值；
     * 2. 扣减/增加后分值不低于0（由executeAiBehaviorImpact方法兜底）；
     * 3. 老老实实调用两次方法，分别更新活跃度、喜爱值
     *
     * @param userId 用户ID（关联对应的AI实例，修改其情绪值）
     * @return 操作结果：true=至少一项成功，false=全部失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleAiActiveSendMsgBehavior(Integer userId) {
        // 1. 核心参数合法性校验
        Assert.notNull(userId, "用户ID不能为空");
        Assert.isTrue(userId > 0, "用户ID需为正整数");

        // 2. 获取「AI主动发送聊天消息」行为基础配置
        BaseAiBehavior activeSendMsgBehavior = baseAiBehaviorService.getAiBehaviorByCode(AiBehaviorEnum.AI_ACTIVE_SEND_MSG);
        if (activeSendMsgBehavior == null) {
            log.warn("处理AI主动发送消息行为失败：未查询到【AI主动发送聊天消息】行为配置，用户ID：{}", userId);
            return false;
        }

        // 3. 查询该行为的所有分值配置（活跃度+喜爱值）
        List<AiBehaviorScore> scoreList = aiBehaviorScoreService.getScoreListByBehaviorCode(AiBehaviorEnum.AI_ACTIVE_SEND_MSG);
        if (scoreList == null || scoreList.isEmpty()) {
            log.warn("处理AI主动发送消息行为失败：未查询到【AI_ACTIVE_SEND_MSG】对应的任何分值配置，用户ID：{}", userId);
            return false;
        }

        // 拆分获取 活跃度、喜爱值 配置
        AiBehaviorScore activityScore = scoreList.stream()
                .filter(score -> EmotionTypeEnum.ACTIVITY.equals(score.getScoreType()))
                .findFirst().orElse(null);
        AiBehaviorScore likeScore = scoreList.stream()
                .filter(score -> EmotionTypeEnum.LIKE.equals(score.getScoreType()))
                .findFirst().orElse(null);

        boolean finalResult = false;

        // ====================== 第一次调用：处理 活跃度 ======================
        if (activityScore != null && activityScore.getScoreVal() != null) {
            Integer scoreVal = activityScore.getScoreVal();
            Assert.isTrue(scoreVal >= -100 && scoreVal <= 100,
                    () -> String.format("【AI主动发送聊天消息】活跃度分值非法，值：%d，用户ID：%d", scoreVal, userId));
            boolean result = executeAiBehaviorImpact(userId, activeSendMsgBehavior, scoreVal, EmotionTypeEnum.ACTIVITY);
            finalResult = result;
            log.info("处理AI主动发送消息-活跃度：用户ID={}，变动分值={}，结果={}", userId, scoreVal, result);
        } else {
            log.warn("【AI主动发送聊天消息】未配置活跃度分值，跳过处理，用户ID：{}", userId);
        }

        // ====================== 第二次调用：处理 喜爱值（保留Redis限流） ======================
        if (likeScore != null && likeScore.getScoreVal() != null) {
            Integer scoreVal = likeScore.getScoreVal();
            Assert.isTrue(scoreVal >= -100 && scoreVal <= 100,
                    () -> String.format("【AI主动发送聊天消息】喜爱值分值非法，值：%d，用户ID：%d", scoreVal, userId));

            // 构建用户唯一限流KEY
            String limitKey = LIKE_LIMIT_KEY_PREFIX + userId;
            // 校验是否允许上涨喜爱值：false=触发限流，直接跳过不处理
            if (!timeWindowRedisUtil.isAllowed(limitKey)) {
                log.info("【AI主动发送聊天消息】用户ID={} 喜爱值已触发时间窗口限流，跳过更新", userId);
                return finalResult;
            }

            // 限流通过 → 执行喜爱值更新
            boolean result = executeAiBehaviorImpact(userId, activeSendMsgBehavior, scoreVal, EmotionTypeEnum.LIKE);
            finalResult = finalResult || result;
            log.info("处理AI主动发送消息-喜爱值：用户ID={}，变动分值={}，结果={}", userId, scoreVal, result);
        } else {
            log.warn("【AI主动发送聊天消息】未配置喜爱值分值，跳过处理，用户ID：{}", userId);
        }

        // 最终日志
        log.info("处理AI主动发送消息行为完成，用户ID：{}，行为配置ID：{}，最终结果：{}",
                userId, activeSendMsgBehavior.getBehaviorId(), finalResult);
        return finalResult;
    }

    /**
     * 处理AI等待很久行为（补满该用户的AI活跃度分值）
     * 注：当AI等待用户交互时长超出阈值时，直接补满其活跃度分值（置为100）
     *
     * @param userId 用户ID（必传）
     * @return 操作结果：true=处理成功，异常直接抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleAiWaitTooLong(Integer userId) {
        // 1. 核心参数校验（仅校验userId，对齐原有风格，抛IllegalArgumentException）
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID不能为空且需为正整数");
        }

        // 2. 调用补满AI活跃度分值方法（替换原有清空方法，仅传userId）
        AiEmotionScoreOperateDTO fillResult = aiEmotionRealStateService.fillFullAiActivityScore(userId);

        // 3. 获取AI等待很久行为配置并设置行为ID（适配日志明细的行为关联）
        BaseAiBehavior waitTooLongBehavior = baseAiBehaviorService.getAiBehaviorByCode(AiBehaviorEnum.WAIT);
        if (waitTooLongBehavior == null) {
            throw new IllegalStateException("未配置WAIT_TOO_LONG行为基础信息");
        }
        fillResult.setAiBehaviorId(waitTooLongBehavior.getBehaviorId());

        // 4. 添加AI情绪分值明细日志（调用指定的addEmotionLogDetail方法）
        aiEmotionLogDetailService.addEmotionLogDetail(fillResult);

        // 5. 统一返回成功（异常已抛出，此处无需复杂判定）
        return true;
    }

    /**
     * 通用执行方法：封装AI行为影响情绪分值的核心逻辑（重载版本，支持自定义分值）
     *
     * @param userId         用户ID
     * @param baseAiBehavior AI行为配置实体
     * @param customScore    自定义变动分值（可传null，适配分值不确定的场景，如AI等待行为）
     * @return 操作结果：true=成功（分值有变动），false=失败/无变动
     */
    private boolean executeAiBehaviorImpact(Integer userId, BaseAiBehavior baseAiBehavior, Integer customScore) {
        // 1. 校验AI行为配置是否存在
        if (baseAiBehavior == null) {
            log.warn("【AI行为影响情绪分值】未查询到AI行为配置，用户ID：{}", userId);
            return false;
        }

        // 2. 提取AI行为ID，分值使用传入的自定义分值（可null）
        Integer aiBehaviorId = baseAiBehavior.getBehaviorId();

        // 3. 调用私有核心方法处理情绪分值变动（默认情绪类型为ACTIVITY-活跃度）
        return handleAiEmotionImpactScore(
                userId,
                aiBehaviorId,
                EmotionTypeEnum.ACTIVITY, // 默认修改类型为活跃度（可根据业务调整）
                customScore // 使用自定义分值，替代从baseAiBehavior获取的score
        );
    }

    /**
     * 通用执行方法：封装AI行为影响情绪分值的核心逻辑（重载版本，支持自定义分值+自定义情绪类型）
     *
     * @param userId         用户ID
     * @param baseAiBehavior AI行为配置实体
     * @param customScore    自定义变动分值（可传null，适配分值不确定的场景）
     * @param emotionType    自定义情绪类型（手动指定，替代默认的活跃度）
     * @return 操作结果：true=成功（分值有变动），false=失败/无变动
     */
    private boolean executeAiBehaviorImpact(Integer userId, BaseAiBehavior baseAiBehavior, Integer customScore, EmotionTypeEnum emotionType) {
        // 1. 校验AI行为配置是否存在
        if (baseAiBehavior == null) {
            log.warn("【AI行为影响情绪分值】未查询到AI行为配置，用户ID：{}", userId);
            return false;
        }

        // 2. 提取AI行为ID
        Integer aiBehaviorId = baseAiBehavior.getBehaviorId();

        // 3. 调用核心方法，使用【手动传入的情绪类型】
        return handleAiEmotionImpactScore(
                userId,
                aiBehaviorId,
                emotionType, // 手动传入的情绪类型
                customScore
        );
    }

    // ====================== 私有核心方法：处理AI行为影响情绪分值 ======================

    /**
     * 私有核心方法：处理AI行为影响情绪分值（参考handleBehaviorImpactScore改造，移除sessionId）
     */
    private boolean handleAiEmotionImpactScore(Integer userId, Integer aiBehaviorId,
                                               EmotionTypeEnum emotionType, Integer addScore) {
        // 1. 基础参数校验（前置兜底，移除sessionId校验，适配AI场景）
        Assert.notNull(userId, "用户ID不能为空");
        Assert.isTrue(userId > 0, "用户ID需为正整数");
        Assert.notNull(aiBehaviorId, "AI行为ID不能为空");
        Assert.isTrue(aiBehaviorId > 0, "AI行为ID需为正整数");
        Assert.notNull(emotionType, "情绪类型不能为空");
        Assert.notNull(addScore, "情绪变动分值不能为空");
        Assert.isTrue(addScore >= -100 && addScore <= 100, "情绪变动分值需在-100~100范围内");

        // 2. 构建AI情绪分值操作DTO（调用私有build方法）
        AiEmotionScoreOperateDTO operateDTO = buildAiEmotionScoreOperateDTO(
                userId, aiBehaviorId, emotionType, addScore
        );

        // 3. 第一步：更新AI情绪分值，获取变动前的旧分值
        Integer scoreBefore = aiEmotionRealStateService.updateAiEmotionScore(operateDTO);
        if (scoreBefore == null) {
            log.info("【AI行为影响情绪分值】用户[{}]AI行为[{}]未触发分值变动（无旧记录/分值无变化）",
                    userId, aiBehaviorId);
            return false; // 无变动，返回false
        }

        // 4. 第二步：补充变动前分数，新增AI情绪变动日志明细
        operateDTO.setScoreBefore(scoreBefore);
        aiEmotionLogDetailService.addEmotionLogDetail(operateDTO);

        log.info("【AI行为影响情绪分值】用户[{}]AI行为[{}]分值变动完成：情绪类型={}，变动前={}，变动值={}",
                userId, aiBehaviorId, emotionType.getDesc(), scoreBefore, addScore);
        return true; // 全部成功，返回true
    }

    // ====================== 私有辅助方法：构建AI情绪分值操作DTO ======================

    /**
     * 私有方法：构建AiEmotionScoreOperateDTO（适配AI行为场景，无sessionId）
     */
    private AiEmotionScoreOperateDTO buildAiEmotionScoreOperateDTO(Integer userId, Integer aiBehaviorId,
                                                                   EmotionTypeEnum emotionType, Integer addScore) {
        AiEmotionScoreOperateDTO operateDTO = new AiEmotionScoreOperateDTO();
        operateDTO.setUserId(userId);
        operateDTO.setAiBehaviorId(aiBehaviorId);
        operateDTO.setEmotionType(emotionType);
        operateDTO.setAddScore(addScore);
        // scoreBefore 先置空，后续更新分值后补充
        return operateDTO;
    }

}