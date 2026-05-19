package org.myfx.controls.aione.AiService.service.base.status.user_behavior_db.impl;

import cn.hutool.core.lang.Assert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.common.user_behavior_db.FeatureTypeEnum;
import org.myfx.controls.aione.AiService.dto.UserFeatureScoreOperateDTO;
import org.myfx.controls.aione.AiService.entity.user_behavior_db.UserFeatureScore;
import org.myfx.controls.aione.AiService.mapper.user_behavior_db.UserFeatureScoreMapper;
import org.myfx.controls.aione.AiService.service.base.status.user_behavior_db.UserFeatureScoreService;
import org.myfx.controls.aione.ServiceCommon.utils.SnowflakeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户特征分值业务实现类（核心CRUD，无SAGA/逻辑删除，基于【用户】维度）
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserFeatureScoreServiceImpl implements UserFeatureScoreService {

    private final UserFeatureScoreMapper userFeatureScoreMapper;

    /**
     * 初始化用户特征分值：默认各分值为0，基于【用户】维度保证幂等性
     */
    @Override
    public void initUserFeatureScore(Integer userId) {
        // 1. 简化参数校验（Hutool Assert 替代冗长if）
        Assert.notNull(userId, "参数不合法：用户ID（userId）不能为空");
        Assert.isTrue(userId > 0, "参数不合法：用户ID（userId）需为正整数，当前值：{}", userId);

        // 2. 幂等校验：查询【用户】是否已存在分值记录
        UserFeatureScore existScore = userFeatureScoreMapper.selectByUserId(userId);
        if (existScore != null) {
            throw new RuntimeException(String.format("用户ID[%d]特征分值已初始化，无需重复操作", userId));
        }

        // 3. 调用私有方法构建初始化实体
        UserFeatureScore score = buildInitUserFeatureScore(userId);

        // 4. 执行插入
        int insertCount = userFeatureScoreMapper.insert(score);
        Assert.isTrue(insertCount == 1, "用户ID[%d]特征分值初始化失败", userId);
        log.info("【用户特征分值】用户[{}]特征分值初始化成功，默认分值均为0", userId);
    }

    /**
     * 私有方法：构建初始化的用户特征分值实体（默认分值为0）
     * @param userId 用户ID
     * @return 初始化后的UserFeatureScore实体
     */
    private UserFeatureScore buildInitUserFeatureScore(Integer userId) {
        UserFeatureScore score = new UserFeatureScore();
        score.setId(SnowflakeGenerator.generateId()); // 雪花ID生成物理主键
        score.setUserId(userId);
        score.setActivityScore(0); // 活跃度默认0
        score.setFavorScore(0);    // 偏好分默认0
        score.setFamiliarScore(0); // 熟悉度默认0
        return score;
    }

    @Override
    public List<Integer> listLowActivityUserIds() {
        // 调用Mapper查询所有活跃度=0的用户ID
        return userFeatureScoreMapper.selectUserIdsByActivityScoreZero();
    }

    @Override
    public Integer updateUserFeatureScore(UserFeatureScoreOperateDTO operateDTO) {
        // 1. 简化参数校验（Hutool Assert 替代冗长if）
        Assert.notNull(operateDTO, "操作DTO不能为空");
        Integer userId = operateDTO.getUserId();
        FeatureTypeEnum featureType = operateDTO.getFeatureType();
        Integer addScore = operateDTO.getAddScore();
        Assert.notNull(userId, "参数不合法：用户ID（userId）不能为空");
        Assert.isTrue(userId > 0, "参数不合法：用户ID（userId）需为正整数，当前值：{}", userId);
        Assert.notNull(featureType, "参数不合法：特征类型（featureType）不能为空");
        Assert.notNull(addScore, "参数不合法：加分值（addScore）不能为空");

        // 2. 查询数据库中该用户的旧分值记录
        UserFeatureScore oldScore = userFeatureScoreMapper.selectByUserId(userId);
        if (oldScore == null) {
            log.warn("【用户特征分值】更新用户[{}]特征分值失败：无该用户的分值记录，返回null", userId);
            return null; // 无旧记录，返回null
        }

        // 3. 根据特征类型计算新分值（核心逻辑：旧值+加分值，按特征类型做范围兜底）
        Integer oldValue;
        int rawNewValue; // 原始计算值（未兜底）
        Integer newValue;
        UserFeatureScore updateObj = new UserFeatureScore();
        updateObj.setUserId(userId);

        // 3.1 按特征类型匹配旧值 + 计算原始新值
        oldValue = switch (featureType) {
            case ACTIVITY -> oldScore.getActivityScore() == null ? 0 : oldScore.getActivityScore();
            case FAVOR -> oldScore.getFavorScore() == null ? 0 : oldScore.getFavorScore();
            case FAMILIAR -> oldScore.getFamiliarScore() == null ? 0 : oldScore.getFamiliarScore();
        };
        rawNewValue = oldValue + addScore; // 先计算原始值（未兜底）

        // 3.2 按特征类型做范围兜底 + 新增超出范围日志
        newValue = switch (featureType) {
            case ACTIVITY, FAMILIAR -> {
                // 活跃度/熟悉度：0~100
                if (rawNewValue > 100) {
                    log.info("【用户特征分值】用户[{}]{}分值超出上限100（原始计算值={}），暂存为100",
                            userId, featureType.getDesc(), rawNewValue);
                    yield 100;
                } else if (rawNewValue < 0) {
                    log.info("【用户特征分值】用户[{}]{}分值超出下限0（原始计算值={}），暂存为0",
                            userId, featureType.getDesc(), rawNewValue);
                    yield 0;
                } else {
                    yield rawNewValue;
                }
            }
            case FAVOR -> {
                // 喜爱度：-100~100
                if (rawNewValue > 100) {
                    log.info("【用户特征分值】用户[{}]{}分值超出上限100（原始计算值={}），暂存为100",
                            userId, featureType.getDesc(), rawNewValue);
                    yield 100;
                } else if (rawNewValue < -100) {
                    log.info("【用户特征分值】用户[{}]{}分值超出下限-100（原始计算值={}），暂存为-100",
                            userId, featureType.getDesc(), rawNewValue);
                    yield -100;
                } else {
                    yield rawNewValue;
                }
            }
        };

        // 3.3 设置更新字段
        switch (featureType) {
            case ACTIVITY -> updateObj.setActivityScore(newValue);
            case FAVOR -> updateObj.setFavorScore(newValue);
            case FAMILIAR -> updateObj.setFamiliarScore(newValue);
        }

        // 4. 校验分值是否有变化（无变化则返回null）
        if (newValue.equals(oldValue)) {
            log.info("【用户特征分值】用户[{}]{}分值无变化（旧值={}+加分值={}=新值={}），返回null",
                    userId, featureType.getDesc(), oldValue, addScore, newValue);
            return null; // 分值无变化，返回null
        }

        // 5. 执行动态更新（仅更新选中的特征字段）
        int affectedRows = userFeatureScoreMapper.updateByUserId(updateObj);
        // 简化校验（Assert 替代if）
        Assert.isTrue(affectedRows == 1, "更新用户[{}]{}分值异常：预期影响1行，实际影响{}行",
                userId, featureType.getDesc(), affectedRows);

        log.info("【用户特征分值】更新用户[{}]{}分值完成：旧值={} + 加分值={} = 原始计算值={} → 兜底后新值={}，返回修改前分数={}",
                userId, featureType.getDesc(), oldValue, addScore, rawNewValue, newValue, oldValue);
        return oldValue; // 更新成功，返回修改前的分数
    }

    /**
     * 清空指定用户的某类特征分值（将该特征分值置为0）
     * 核心逻辑：查询旧分值 → 计算加分值（0 - 旧分值）→ 调用更新方法完成清空
     * @param userId 用户ID（必传，正整数）
     * @param featureType 特征类型（必传，指定要清空的分值类型）
     * @return 包含清空操作信息的DTO：
     *         - scoreBefore：清空前的原始分值（无旧记录为null，分值无变化为0）
     *         - 其他字段：userId/featureType/addScore 均为本次操作的参数
     * @throws IllegalArgumentException 入参不合法时抛出
     */
    public UserFeatureScoreOperateDTO clearUserFeatureScore(Integer userId, FeatureTypeEnum featureType) {
        // 1. 简化参数校验（Hutool Assert 替代冗长if）
        Assert.notNull(userId, "用户ID不能为空");
        Assert.isTrue(userId > 0, "用户ID需为正整数，当前值：{}", userId);
        Assert.notNull(featureType, "特征类型不能为空");

        // 2. 初始化返回的DTO对象
        UserFeatureScoreOperateDTO operateDTO = new UserFeatureScoreOperateDTO();
        operateDTO.setUserId(userId);
        operateDTO.setFeatureType(featureType);

        // 3. 查询数据库中该用户的旧分值记录
        UserFeatureScore oldScore = userFeatureScoreMapper.selectByUserId(userId);
        if (oldScore == null) {
            log.warn("【用户特征分值】清空用户[{}]{}分值失败：无该用户的分值记录",
                    userId, featureType.getDesc());
            operateDTO.setScoreBefore(null); // 无旧记录，scoreBefore设为null
            operateDTO.setAddScore(null);    // 无旧记录，加分值设为null
            return operateDTO;
        }

        // 4. 提取该特征类型的旧分值（null按0处理）
        Integer oldValue = switch (featureType) {
            case ACTIVITY -> oldScore.getActivityScore() == null ? 0 : oldScore.getActivityScore();
            case FAVOR -> oldScore.getFavorScore() == null ? 0 : oldScore.getFavorScore();
            case FAMILIAR -> oldScore.getFamiliarScore() == null ? 0 : oldScore.getFamiliarScore();
        };

        // 5. 设置DTO的scoreBefore（清空前的原始分值）
        operateDTO.setScoreBefore(oldValue);

        // 6. 计算清空所需的加分值（0 - 旧分值 → 旧值+加分值=0）
        Integer addScore = -oldValue;
        operateDTO.setAddScore(addScore);
        log.info("【用户特征分值】准备清空用户[{}]{}分值：旧值={}，需加分值={}",
                userId, featureType.getDesc(), oldValue, addScore);

        // 7. 调用更新方法完成清空
        Integer updateResult = updateUserFeatureScore(operateDTO);

        // 8. 日志补充（区分清空成功/无变化）
        if (updateResult != null) {
            log.info("【用户特征分值】清空用户[{}]{}分值成功：旧值={} → 新值=0",
                    userId, featureType.getDesc(), oldValue);
        } else {
            log.info("【用户特征分值】清空用户[{}]{}分值无变化：旧值已为0（旧值={}）",
                    userId, featureType.getDesc(), oldValue);
        }

        // 9. 返回包含完整操作信息的DTO
        return operateDTO;
    }

    /**
     * 实现：清空指定用户的「活跃度分值」（固定特征类型为ACTIVITY）
     */
    @Override
    public UserFeatureScoreOperateDTO clearUserActivityScore(Integer userId) {
        // 直接调用主方法，固定传入活跃度枚举，无需重复校验参数（主方法已做完整校验）
        return clearUserFeatureScore(userId, FeatureTypeEnum.ACTIVITY);
    }

    @Override
    public UserFeatureScore getUserFeatureScoreByUserId(Integer userId) {
        // 1. 核心参数校验（空/非正整数直接返回null）
        Assert.notNull(userId,"userId不能为空！");

        // 2. 直接调用Mapper查询（极简核心逻辑）
        UserFeatureScore userFeatureScore = userFeatureScoreMapper.selectByUserId(userId);

        // 3. 极简日志（可选，也可删除）
        log.info("查询用户{}特征分值：{}", userId, userFeatureScore == null ? "无数据" : "成功");

        return userFeatureScore;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAllUserFeatureScoreByUserId(Integer userId) {
        // 1. 简化参数校验（Hutool Assert 替代冗长if）
        Assert.notNull(userId, "参数不合法：用户ID（userId）不能为空");
        Assert.isTrue(userId > 0, "参数不合法：用户ID（userId）需为正整数，当前值：{}", userId);

        // 2. 执行批量物理删除
        int affectedRows = userFeatureScoreMapper.deleteByUserId(userId);
        boolean success = affectedRows > 0;
        log.info("【用户特征分值】批量删除用户[{}]所有特征分值，结果：{}（共删除{}条记录）",
                userId, success ? "成功" : "失败（无匹配记录）", affectedRows);
        return success;
    }
}