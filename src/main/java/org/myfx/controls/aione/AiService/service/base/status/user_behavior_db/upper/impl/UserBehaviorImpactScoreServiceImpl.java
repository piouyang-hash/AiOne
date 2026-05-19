package org.myfx.controls.aione.AiService.service.base.status.user_behavior_db.upper.impl;

import cn.hutool.core.lang.Assert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.common.user_behavior_db.BehaviorEnum;
import org.myfx.controls.aione.AiService.common.user_behavior_db.FeatureTypeEnum;
import org.myfx.controls.aione.AiService.dto.UserFeatureScoreOperateDTO;
import org.myfx.controls.aione.AiService.entity.user_behavior_db.BaseUserBehavior;
import org.myfx.controls.aione.AiService.entity.user_behavior_db.UserBehaviorScore;
import org.myfx.controls.aione.AiService.service.base.status.user_behavior_db.BaseUserBehaviorService;
import org.myfx.controls.aione.AiService.service.base.status.user_behavior_db.UserBehaviorScoreDetailService;
import org.myfx.controls.aione.AiService.service.base.status.user_behavior_db.UserBehaviorScoreService;
import org.myfx.controls.aione.AiService.service.base.status.user_behavior_db.UserFeatureScoreService;
import org.myfx.controls.aione.AiService.service.base.status.user_behavior_db.upper.UserBehaviorImpactScoreService;
import org.myfx.controls.aione.ServiceCommon.context.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户行为影响分值服务实现类
 * 封装底层分值更新和明细新增逻辑，提供统一的上层业务入口
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserBehaviorImpactScoreServiceImpl implements UserBehaviorImpactScoreService {

    // 注入底层服务
    private final UserFeatureScoreService userFeatureScoreService;
    private final UserBehaviorScoreDetailService userBehaviorScoreDetailService;
    private final BaseUserBehaviorService baseUserBehaviorService;
    private final UserBehaviorScoreService userBehaviorScoreService;

    // ====================== 实现公开接口方法（添加事务注解） ======================
    /**
     * 处理用户聊天行为触发的分值变动
     * @param userId 用户ID
     * @return 操作结果：true=成功，false=失败/无变动
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleChatBehavior(Integer userId) {
        // 简化参数校验（Assert 替代 if）
        Assert.notNull(userId, "用户ID不能为空");
        Assert.isTrue(userId > 0, "用户ID需为正整数，当前值：{}", userId);

        // 获取“发送AI聊天消息”行为配置
        BaseUserBehavior chatBehavior = baseUserBehaviorService.getBehaviorByCode(BehaviorEnum.CHAT_SEND_MSG);

        // ====================== 新增调试打印（关键！） ======================
        log.info("============= 调试打印 =============");
        log.info("chatBehavior 对象是否为空：{}", chatBehavior != null);
        log.info("chatBehavior 中的 behaviorCode 值：{}", chatBehavior.getBehaviorCode());
        log.info("===================================");

        return executeBehaviorImpact(userId, chatBehavior);
    }

    /**
     * 处理用户离线行为（根据离线时长计算分值并调整用户特征分）
     * 注：非离线行为本身扣分，而是根据用户离线时长/时期计算对应的分值调整（可正可负）
     * @param userId 用户ID（明确指定离线用户）
     * @param score 根据离线时长计算的分值（负数为扣分，正数为加分，null表示未计算时长分值）
     * @return 操作结果：true=成功，false=失败/无变动
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleUserOffline(Integer userId, Integer score) {
        // 简化参数校验（Assert 替代 if）
        Assert.notNull(userId, "用户ID不能为空");
        Assert.isTrue(userId > 0, "用户ID需为正整数，当前值：{}", userId);
        // 分值非空时校验范围
        if (score != null) {
            Assert.isTrue(score >= -100 && score <= 100, "离线行为分值需在-100~100范围内，当前值：{}", score);
        }

        // 获取“用户离线”行为配置（对应USER_OFFLINE枚举）
        BaseUserBehavior offlineBehavior = baseUserBehaviorService.getBehaviorByCode(BehaviorEnum.USER_OFFLINE);

        // 执行行为分值影响逻辑（调用重载方法，传入用户指定的分值）
        return executeBehaviorImpact(userId, offlineBehavior, score);
    }

    /**
     * 处理用户离线过久行为（清空该用户的活跃度分值）
     * 注：当用户离线时长超出阈值时，直接清空其活跃度分值（置为0）
     * @param userId 用户ID（必传）
     * @return 操作结果：true=处理成功，异常直接抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleUserOfflineTooLong(Integer userId) {
        // 简化参数校验（Assert 替代 if）
        Assert.notNull(userId, "用户ID不能为空");
        Assert.isTrue(userId > 0, "用户ID需为正整数，当前值：{}", userId);

        // 调用清空活跃度分值方法（仅传userId）
        UserFeatureScoreOperateDTO clearResult = userFeatureScoreService.clearUserActivityScore(userId);

        // 获取离线行为配置并设置行为ID
        BaseUserBehavior offlineBehavior = baseUserBehaviorService.getBehaviorByCode(BehaviorEnum.USER_OFFLINE);
        Assert.notNull(offlineBehavior, "未配置USER_OFFLINE行为基础信息");
        clearResult.setBehaviorId(offlineBehavior.getBehaviorId());

        // 添加用户行为分值明细
        userBehaviorScoreDetailService.addUserBehaviorScoreDetail(clearResult);

        // 统一返回成功（异常已抛出，此处无需复杂判定）
        return true;
    }

    /**
     * 处理用户点赞行为触发的分值变动
     * @return 操作结果：true=成功，false=失败/无变动
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleLikeBehavior() {
        // 从UserContext获取用户ID并校验
        Integer userId = UserContext.getUserId();
        Assert.notNull(userId, "上下文用户ID不能为空");
        Assert.isTrue(userId > 0, "上下文用户ID需为正整数，当前值：{}", userId);

        // 获取“点赞AI回复”行为配置
        BaseUserBehavior likeBehavior = baseUserBehaviorService.getBehaviorByCode(BehaviorEnum.CHAT_LIKE);
        return executeBehaviorImpact(userId, likeBehavior);
    }

    /**
     * 处理用户拓展通用行为触发的分值变动
     * @param behaviorEnum 拓展行为枚举
     * @return 操作结果：true=成功，false=失败/无变动
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleGeneralBehavior(BehaviorEnum behaviorEnum) {
        // 简化参数校验（Assert 替代 if）
        Assert.notNull(behaviorEnum, "拓展行为枚举不能为空");

        // 从UserContext获取用户ID并校验
        Integer userId = UserContext.getUserId();
        Assert.notNull(userId, "上下文用户ID不能为空");
        Assert.isTrue(userId > 0, "上下文用户ID需为正整数，当前值：{}", userId);

        // 获取通用行为配置
        BaseUserBehavior generalBehavior = baseUserBehaviorService.getBehaviorByCode(behaviorEnum);
        return executeBehaviorImpact(userId, generalBehavior);
    }

    // ====================== 私有通用执行方法（复用逻辑） ======================
    /**
     * 通用执行方法：封装行为影响分值的核心逻辑
     * @param userId         用户ID
     * @param baseUserBehavior 行为配置实体
     * @return 操作结果
     */
    private boolean executeBehaviorImpact(Integer userId, BaseUserBehavior baseUserBehavior) {
        // 校验行为配置是否存在
        if (baseUserBehavior == null) {
            log.warn("【用户行为影响分值】未查询到行为配置，用户ID：{}", userId);
            return false;
        }

        // 1. 获取行为编码枚举
        BehaviorEnum behaviorCode = baseUserBehavior.getBehaviorCode();
        // 2. 查询【默认活跃度】维度的分值配置
        UserBehaviorScore scoreConfig = userBehaviorScoreService.getScoreByCodeAndType(behaviorCode, FeatureTypeEnum.ACTIVITY);

        // 校验分值配置是否存在
        if (scoreConfig == null) {
            log.warn("【用户行为影响分值】未查询到行为【{}】的活跃度分值配置，用户ID：{}", behaviorCode, userId);
            return false;
        }

        // 3. 提取变动分值
        Integer addScore = scoreConfig.getScoreVal();

        // 4. 调用核心方法处理分值变动
        return handleBehaviorImpactScore(
                userId,
                baseUserBehavior.getBehaviorId(),
                FeatureTypeEnum.ACTIVITY,
                addScore
        );
    }

    /**
     * 通用执行方法：封装行为影响分值的核心逻辑（重载版本，支持自定义分值）
     * @param userId         用户ID
     * @param baseUserBehavior 行为配置实体
     * @param customScore    自定义加分值（可传null，适配分值不确定的场景，如用户离线）
     * @return 操作结果
     */
    private boolean executeBehaviorImpact(Integer userId, BaseUserBehavior baseUserBehavior, Integer customScore) {
        // 校验行为配置是否存在
        if (baseUserBehavior == null) {
            log.warn("【用户行为影响分值】未查询到行为配置，用户ID：{}", userId);
            return false;
        }

        // 提取行为ID，分值使用传入的自定义分值（可null）
        Integer behaviorId = baseUserBehavior.getBehaviorId();

        // 调用私有核心方法处理分值变动（默认特征类型为ACTIVITY）
        return handleBehaviorImpactScore(
                userId,
                behaviorId,
                FeatureTypeEnum.ACTIVITY, // 默认修改类型为活跃度
                customScore // 使用自定义分值，替代从baseUserBehavior获取的score
        );
    }

    /**
     * 通用执行方法：封装行为影响分值的核心逻辑（重载版本，支持自定义分值类型）
     * @param userId         用户ID
     * @param baseUserBehavior 行为配置实体
     * @param featureType    自定义分值类型（活跃度/喜爱度/熟悉度）
     * @return 操作结果
     */
    private boolean executeBehaviorImpact(Integer userId, BaseUserBehavior baseUserBehavior, FeatureTypeEnum featureType) {
        // 校验行为配置是否存在
        if (baseUserBehavior == null) {
            log.warn("【用户行为影响分值】未查询到行为配置，用户ID：{}", userId);
            return false;
        }
        // 校验分值类型不能为空
        if (featureType == null) {
            log.warn("【用户行为影响分值】分值类型不能为空，用户ID：{}", userId);
            return false;
        }

        // 1. 获取行为编码枚举
        BehaviorEnum behaviorCode = baseUserBehavior.getBehaviorCode();
        // 2. 查询【自定义】维度的分值配置
        UserBehaviorScore scoreConfig = userBehaviorScoreService.getScoreByCodeAndType(behaviorCode, featureType);

        // 校验分值配置是否存在
        if (scoreConfig == null) {
            log.warn("【用户行为影响分值】未查询到行为【{}】的【{}】分值配置，用户ID：{}", behaviorCode, featureType.getCode(), userId);
            return false;
        }

        // 3. 提取变动分值
        Integer addScore = scoreConfig.getScoreVal();

        // 4. 调用核心方法处理分值变动
        return handleBehaviorImpactScore(
                userId,
                baseUserBehavior.getBehaviorId(),
                featureType,
                addScore
        );
    }

    /**
     * 通用执行方法：封装行为影响分值的核心逻辑（重载版本，支持自定义分值类型+自定义分值）
     * @param userId         用户ID
     * @param baseUserBehavior 行为配置实体
     * @param featureType    自定义分值类型（活跃度/喜爱度/熟悉度）
     * @param customScore    自定义加分值（可传null）
     * @return 操作结果
     */
    private boolean executeBehaviorImpact(Integer userId, BaseUserBehavior baseUserBehavior, FeatureTypeEnum featureType, Integer customScore) {
        // 校验行为配置是否存在
        if (baseUserBehavior == null) {
            log.warn("【用户行为影响分值】未查询到行为配置，用户ID：{}", userId);
            return false;
        }
        // 校验分值类型不能为空
        if (featureType == null) {
            log.warn("【用户行为影响分值】分值类型不能为空，用户ID：{}", userId);
            return false;
        }

        // 提取行为ID
        Integer behaviorId = baseUserBehavior.getBehaviorId();

        // 调用核心方法处理分值变动
        return handleBehaviorImpactScore(
                userId,
                behaviorId,
                featureType,
                customScore
        );
    }

    // ====================== 原有核心逻辑改为私有方法（移除事务注解） ======================
    /**
     * 私有核心方法：处理行为影响分值（原公开方法改为私有，移除事务注解）
     */
    private boolean handleBehaviorImpactScore(Integer userId, Integer behaviorId,
                                              FeatureTypeEnum featureType, Integer addScore) {
        // 简化参数校验（Assert 替代 if）
        Assert.notNull(userId, "用户ID不能为空");
        Assert.isTrue(userId > 0, "用户ID需为正整数，当前值：{}", userId);
        Assert.notNull(behaviorId, "行为ID不能为空");
        Assert.isTrue(behaviorId > 0, "行为ID需为正整数，当前值：{}", behaviorId);
        Assert.notNull(featureType, "特征类型不能为空");
        Assert.notNull(addScore, "加分值不能为空");
        Assert.isTrue(addScore >= -100 && addScore <= 100, "加分值需在-100~100范围内，当前值：{}", addScore);

        // 构建分值操作DTO（调用私有build方法）
        UserFeatureScoreOperateDTO operateDTO = buildUserFeatureScoreOperateDTO(
                userId, behaviorId, featureType, addScore
        );

        // 第一步：更新用户特征分值，获取加分前分数
        Integer scoreBefore = userFeatureScoreService.updateUserFeatureScore(operateDTO);
        if (scoreBefore == null) {
            log.info("【用户行为影响分值】用户[{}]行为[{}]未触发分值变动（无旧记录/分值无变化）",
                    userId, behaviorId);
            return false; // 无变动，返回失败
        }

        // 第二步：补充加分前分数，新增行为加分明细
        operateDTO.setScoreBefore(scoreBefore);
        userBehaviorScoreDetailService.addUserBehaviorScoreDetail(operateDTO);

        log.info("【用户行为影响分值】用户[{}]行为[{}]分值变动完成：特征类型={}，加分前={}，加分值={}",
                userId, behaviorId, featureType.getDesc(), scoreBefore, addScore);
        return true; // 全部成功，返回true
    }

    /**
     * 私有构建方法：构造UserFeatureScoreOperateDTO
     * 封装DTO构建逻辑，避免重复代码
     */
    private UserFeatureScoreOperateDTO buildUserFeatureScoreOperateDTO(Integer userId, Integer behaviorId,
                                                                       FeatureTypeEnum featureType, Integer addScore) {
        UserFeatureScoreOperateDTO dto = new UserFeatureScoreOperateDTO();
        dto.setUserId(userId);
        dto.setBehaviorId(behaviorId);
        dto.setFeatureType(featureType);
        dto.setAddScore(addScore);
        // scoreBefore先不设置，待分值更新后补充
        return dto;
    }
}