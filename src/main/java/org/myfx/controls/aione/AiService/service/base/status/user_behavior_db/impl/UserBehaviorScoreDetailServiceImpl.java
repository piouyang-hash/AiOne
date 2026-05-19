package org.myfx.controls.aione.AiService.service.base.status.user_behavior_db.impl;

import cn.hutool.core.lang.Assert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.common.user_behavior_db.FeatureTypeEnum;
import org.myfx.controls.aione.AiService.dto.UserFeatureScoreOperateDTO;
import org.myfx.controls.aione.AiService.entity.user_behavior_db.UserBehaviorScoreDetail;
import org.myfx.controls.aione.AiService.mapper.user_behavior_db.UserBehaviorScoreDetailMapper;
import org.myfx.controls.aione.AiService.service.base.status.user_behavior_db.UserBehaviorScoreDetailService;
import org.myfx.controls.aione.ServiceCommon.utils.SnowflakeGenerator;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 用户行为积分明细业务服务实现类
 * 基础版：仅封装Mapper调用，后续可扩展业务逻辑（如参数校验、事务、日志等）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserBehaviorScoreDetailServiceImpl implements UserBehaviorScoreDetailService {

    // 注入Mapper接口（Spring自动装配，也可使用@Autowired）
    private final UserBehaviorScoreDetailMapper userBehaviorScoreDetailMapper;

    /**
     * 新增用户行为积分明细
     * 核心逻辑：参数校验 → 计算加分后分值 → 组装明细 → 插入数据库
     */
    @Override
    public int addUserBehaviorScoreDetail(UserFeatureScoreOperateDTO operateDTO) {
        // 1. 参数校验（Assert简化，补充所有必填项校验）
        Integer userId = operateDTO.getUserId();
        Integer behaviorId = operateDTO.getBehaviorId();
        FeatureTypeEnum featureType = operateDTO.getFeatureType();
        Integer addScore = operateDTO.getAddScore();
        Integer scoreBefore = operateDTO.getScoreBefore();

        // 核心参数非空+合法性校验（Assert自动抛IllegalArgumentException）
        Assert.notNull(userId, "参数不合法：用户ID（userId）不能为空");
        Assert.isTrue(userId > 0, "参数不合法：用户ID（userId）需为正整数，当前值：{}", userId);
        Assert.notNull(behaviorId, "参数不合法：行为ID（behaviorId）不能为空");
        Assert.isTrue(behaviorId > 0, "参数不合法：行为ID（behaviorId）需为正整数（Integer类型）");
        Assert.notNull(featureType, "参数不合法：特征类型（featureType）不能为空");
        Assert.notNull(addScore, "参数不合法：加分值（addScore）不能为空");
        Assert.notNull(scoreBefore, "参数不合法：加分前分值（scoreBefore）不能为空");

        // 2. 计算加分后分值（按特征类型做范围兜底，逻辑不变）
        int scoreAfter = scoreBefore + addScore;
        scoreAfter = switch (featureType) {
            case ACTIVITY, FAMILIAR -> Math.max(0, Math.min(100, scoreAfter));
            case FAVOR -> Math.max(-100, Math.min(100, scoreAfter));
        };

        // 3. 组装明细对象（移除sessionId赋值）
        UserBehaviorScoreDetail detail = new UserBehaviorScoreDetail();
        detail.setId(SnowflakeGenerator.generateId()); // 生成雪花ID（主键）
        detail.setUserId(userId);
        detail.setBehaviorId(behaviorId);
        detail.setFeatureType(featureType);
        detail.setAddScore(addScore);
        detail.setScoreAfter(scoreAfter);
        detail.setBehaviorTime(System.currentTimeMillis());

        // 4. 执行插入（日志移除sessionId）
        log.info("开始新增用户行为积分明细，用户ID：{}，行为ID：{}，特征类型：{}，加分前分值：{}，加分值：{}，加分后分值：{}",
                userId, behaviorId, featureType.getDesc(), scoreBefore, addScore, scoreAfter);
        int affectedRows = userBehaviorScoreDetailMapper.insert(detail);

        // 5. 校验插入结果（Assert简化，日志移除sessionId）
        Assert.isTrue(affectedRows == 1, "新增用户行为积分明细失败，用户ID：{}，预期影响1行，实际影响{}行",
                userId, affectedRows);
        log.info("新增用户行为积分明细成功，明细ID：{}", detail.getId());
        return affectedRows;
    }

    /**
     * 查询指定用户下的所有行为积分明细
     * 基础逻辑：直接调用Mapper查询，后续可扩展空值处理、数据过滤等
     */
    @Override
    public List<UserBehaviorScoreDetail> queryUserBehaviorScoreDetailsByUserId(Integer userId) {
        // 参数校验（Assert简化）
        Assert.notNull(userId, "查询行为积分明细失败：用户ID（userId）不能为空");
        Assert.isTrue(userId > 0, "查询行为积分明细失败：用户ID（userId）需为正整数，当前值：{}", userId);

        List<UserBehaviorScoreDetail> detailList = userBehaviorScoreDetailMapper.selectListByUserId(userId);
        log.info("查询用户行为积分明细完成，用户ID：{}，共查询到{}条记录", userId, detailList.size());
        return detailList;
    }

    /**
     * 查询指定用户+指定特征类型下的所有行为积分明细
     * （业务场景：查看某用户某类特征（如活跃度）的积分变动记录）
     * @param userId 用户ID
     * @param featureType 特征类型枚举（ACTIVITY-活跃度/FAVOR-喜爱度/FAMILIAR-熟悉度）
     * @return 积分明细列表（无匹配记录则返回空列表，不会返回null）
     */
    @Override
    public List<UserBehaviorScoreDetail> queryUserBehaviorScoreDetailsByUserIdAndFeatureType(
            Integer userId,
            FeatureTypeEnum featureType
    ) {
        // 1. 前置参数校验（Assert简化，移除sessionId校验）
        try {
            Assert.notNull(userId, "查询行为积分明细失败：用户ID（userId）不能为空");
            Assert.isTrue(userId > 0, "查询行为积分明细失败：用户ID（userId）需为正整数，当前值：{}", userId);
            Assert.notNull(featureType, "查询行为积分明细失败：特征类型（featureType）不能为空");
        } catch (IllegalArgumentException e) {
            log.warn("查询行为积分明细失败：{}", e.getMessage());
            return Collections.emptyList(); // 返回空列表，避免下游处理null
        }

        // 2. 调用Mapper查询（移除sessionId参数）
        List<UserBehaviorScoreDetail> detailList = userBehaviorScoreDetailMapper
                .selectListByUserIdAndFeatureType(userId, featureType);

        // 3. 兜底处理：确保返回空列表而非null
        List<UserBehaviorScoreDetail> result = detailList == null ? Collections.emptyList() : detailList;
        log.info("查询行为积分明细完成！条件：用户{}+特征类型{}，匹配记录数：{}",
                userId, featureType.name(), result.size());
        return result;
    }

    /**
     * 查询指定用户+指定特征类型下的最新一条行为积分明细
     * （业务场景：获取某用户某类特征（如活跃度）的最新积分变动）
     * @param userId 用户ID
     * @param featureType 特征类型枚举（ACTIVITY-活跃度/FAVOR-喜爱度/FAMILIAR-熟悉度）
     * @return 最新积分明细记录（无匹配记录则返回null）
     */
    @Override
    public UserBehaviorScoreDetail queryLatestUserBehaviorScoreDetailByUserIdAndFeatureType(
            Integer userId,
            FeatureTypeEnum featureType
    ) {
        // 1. 前置参数校验（Assert简化，移除sessionId校验）
        try {
            Assert.notNull(userId, "查询最新行为积分明细失败：用户ID（userId）不能为空");
            Assert.isTrue(userId > 0, "查询最新行为积分明细失败：用户ID（userId）需为正整数，当前值：{}", userId);
            Assert.notNull(featureType, "查询最新行为积分明细失败：特征类型（featureType）不能为空");
        } catch (IllegalArgumentException e) {
            log.warn("查询最新行为积分明细失败：{}", e.getMessage());
            return null;
        }

        // 2. 调用Mapper查询（仅查1条，性能最优，移除sessionId参数）
        UserBehaviorScoreDetail latestDetail = userBehaviorScoreDetailMapper
                .selectLatestByUserIdAndFeatureType(userId, featureType);

        // 3. 日志提示（移除sessionId）
        if (latestDetail != null) {
            log.info("查询最新行为积分明细完成！条件：用户{}+特征类型{}，最新记录分值：{}",
                    userId, featureType.name(), latestDetail.getScoreAfter());
        } else {
            log.info("查询最新行为积分明细完成！条件：用户{}+特征类型{}，无匹配记录",
                    userId, featureType.name());
        }
        return latestDetail;
    }

    /**
     * 查询指定用户下活跃度特征的最新一条行为积分明细（快捷方法）
     * @param userId 用户ID
     * @return 最新活跃度明细记录（无匹配则返回null）
     */
    @Override
    public UserBehaviorScoreDetail queryLatestActivityScoreDetail(Integer userId) {
        // 直接转发调用核心方法，固定传入ACTIVITY枚举，移除sessionId
        return queryLatestUserBehaviorScoreDetailByUserIdAndFeatureType(
                userId, FeatureTypeEnum.ACTIVITY);
    }

    /**
     * 查询指定用户下喜爱度特征的最新一条行为积分明细（快捷方法）
     * @param userId 用户ID
     * @return 最新喜爱度明细记录（无匹配则返回null）
     */
    @Override
    public UserBehaviorScoreDetail queryLatestFavorScoreDetail(Integer userId) {
        // 直接转发调用核心方法，固定传入FAVOR枚举，移除sessionId
        return queryLatestUserBehaviorScoreDetailByUserIdAndFeatureType(
                userId, FeatureTypeEnum.FAVOR);
    }

    /**
     * 查询指定用户下熟悉度特征的最新一条行为积分明细（快捷方法）
     * @param userId 用户ID
     * @return 最新熟悉度明细记录（无匹配则返回null）
     */
    @Override
    public UserBehaviorScoreDetail queryLatestFamiliarScoreDetail(Integer userId) {
        // 直接转发调用核心方法，固定传入FAMILIAR枚举，移除sessionId
        return queryLatestUserBehaviorScoreDetailByUserIdAndFeatureType(
                userId, FeatureTypeEnum.FAMILIAR);
    }

    /**
     * 删除指定用户的所有行为积分明细
     * 基础逻辑：直接调用Mapper删除，后续可扩展批量删除、数据备份等
     */
    @Override
    public int removeAllUserBehaviorScoreDetailsByUserId(Integer userId) {
        log.info("开始删除用户所有行为积分明细，用户ID：{}", userId);
        int affectRows = userBehaviorScoreDetailMapper.deleteByUserId(userId);
        log.info("删除用户所有行为积分明细完成，影响行数：{}", affectRows);
        return affectRows;
    }
}