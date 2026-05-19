package org.myfx.controls.aione.AiService.service.base.status.user_behavior_db.upper.impl;

import cn.hutool.core.lang.Assert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.common.user_behavior_db.FeatureTypeEnum;
import org.myfx.controls.aione.AiService.dto.redis.UserActivityScoreRedisDTO;
import org.myfx.controls.aione.AiService.entity.user_behavior_db.UserBehaviorScoreDetail;
import org.myfx.controls.aione.AiService.entity.user_behavior_db.UserFeatureScore;
import org.myfx.controls.aione.AiService.service.base.status.user_behavior_db.UserBehaviorScoreDetailService;
import org.myfx.controls.aione.AiService.service.base.status.user_behavior_db.UserFeatureScoreService;
import org.myfx.controls.aione.AiService.service.base.status.user_behavior_db.upper.IUserFeatureScoreUpperQueryService;
import org.springframework.stereotype.Service;

/**
 * 用户特征分值「上一次变动」查询服务实现（关联Redis DTO + 调用明细查询服务）
 */
@Slf4j
@Service
@RequiredArgsConstructor // 自动注入依赖，替代@Autowired
public class UserFeatureScoreUpperQueryServiceImpl implements IUserFeatureScoreUpperQueryService {

    // 注入行为明细查询服务（核心依赖：调用最新活跃度明细方法）
    private final UserBehaviorScoreDetailService userBehaviorScoreDetailService;

    // 注入用户特征分值服务
    private final UserFeatureScoreService userFeatureScoreService;

    /**
     * 查询指定用户下「上一次活跃度变动」的完整信息（无数据返回null，无默认值）
     * @param userId 用户ID（非空）
     * @return Redis适配的特征分值DTO（有数据时返回，无数据返回null）
     * @throws RuntimeException 参数为空时抛出
     */
    @Override
    public UserActivityScoreRedisDTO queryLastActivityChangeInfo(Integer userId) {
        // 1. 参数非空校验：Assert简化，自动抛异常+格式化错误信息
        Assert.notNull(userId, "查询上一次活跃度变动失败：用户ID为空！userId=%s", userId);

        // 2. 调用明细服务：查询最新活跃度变动记录（仅传userId，移除sessionId）
        UserBehaviorScoreDetail latestDetail = userBehaviorScoreDetailService.queryLatestActivityScoreDetail(userId);

        // 3. 无记录打info日志（正常行为），直接返回null
        if (latestDetail == null) {
            String infoMsg = String.format("用户%d无活跃度变动记录（正常行为）", userId);
            log.info(infoMsg);
            return null;
        }

        // 4. 有记录时构建DTO（移除sessionId赋值，完全复用明细数据）
        UserActivityScoreRedisDTO redisDTO = new UserActivityScoreRedisDTO();
        redisDTO.setUserId(userId);
        redisDTO.setActivityScore(latestDetail.getScoreAfter()); // 仅赋值查到的分值，无默认
        redisDTO.setLastUpdateTimestamp(latestDetail.getBehaviorTime()); // 仅赋值查到的时间戳，无默认
        redisDTO.setUserOfflineTimestamp(System.currentTimeMillis()); // 新增：用户下线时间

        return redisDTO;
    }

    /**
     * 实现：根据特征类型枚举查询指定用户下的具体特征分值
     * 核心：严格参数校验（Assert简化）、保留原始值（无默认0，null即返回null），便于问题排查
     */
    @Override
    public Integer queryUserFeatureScoreByType(Integer userId, FeatureTypeEnum featureType) {
        // 1. 简化参数校验（Assert替代冗长if，自动抛IllegalArgumentException，日志更简洁）
        Assert.notNull(userId, "参数不合法：用户ID（userId）不能为空！");
        Assert.isTrue(userId > 0, "参数不合法：用户ID（userId）需为正整数！当前值：{}", userId);
        Assert.notNull(featureType, "参数不合法：特征类型（featureType）不能为空！userId={}", userId);

        // 2. 查询数据库中该用户的分值记录（纯用户维度，复用已有服务逻辑）
        UserFeatureScore scoreRecord = userFeatureScoreService.getUserFeatureScoreByUserId(userId);
        if (scoreRecord == null) {
            log.warn("【用户特征分值】查询用户[{}]{}分值：无该用户的分值记录，返回null",
                    userId, featureType.getDesc());
            return null; // 无记录返回null，不设默认0，保留真实状态
        }

        // 3. 提取对应特征类型的具体分值（保留原始值，null即返回null，无默认值）
        Integer featureScore = switch (featureType) {
            case ACTIVITY -> scoreRecord.getActivityScore();
            case FAVOR -> scoreRecord.getFavorScore();
            case FAMILIAR -> scoreRecord.getFamiliarScore();
        };

        // 4. 日志输出（移除会话ID，如实打印原始值，包括null，便于排查问题）
        log.info("【用户特征分值】查询用户[{}]{}分值成功：原始分值={}",
                userId, featureType.getDesc(), featureScore);

        // 5. 返回原始分值（null则返回null，无任何默认值）
        return featureScore;
    }

    /**
     * 实现：查询指定用户下的「活跃度分值」（固定特征类型为ACTIVITY）
     */
    @Override
    public Integer queryUserActivityScore(Integer userId) {
        // 直接调用已有方法，固定传入活跃度枚举，无需重复校验参数（底层方法已做严格校验）
        return queryUserFeatureScoreByType(userId, FeatureTypeEnum.ACTIVITY);
    }

}