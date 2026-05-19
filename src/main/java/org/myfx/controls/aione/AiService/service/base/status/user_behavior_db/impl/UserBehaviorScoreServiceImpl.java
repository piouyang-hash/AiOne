package org.myfx.controls.aione.AiService.service.base.status.user_behavior_db.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.common.user_behavior_db.BehaviorEnum;
import org.myfx.controls.aione.AiService.common.user_behavior_db.FeatureTypeEnum;
import org.myfx.controls.aione.AiService.entity.user_behavior_db.UserBehaviorScore;
import org.myfx.controls.aione.AiService.mapper.user_behavior_db.UserBehaviorScoreMapper;
import org.myfx.controls.aione.AiService.service.base.status.user_behavior_db.UserBehaviorScoreService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserBehaviorScoreServiceImpl implements UserBehaviorScoreService {

    private final UserBehaviorScoreMapper scoreMapper;

    // 分值范围
    private static final int MIN_SCORE = -100;
    private static final int MAX_SCORE = 100;

    // ===================== 核心：保存/更新分值配置 =====================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveScoreConfig(BehaviorEnum behaviorCode, FeatureTypeEnum scoreType, Integer scoreVal) {
        // 1. 基础参数校验
        validateParam(behaviorCode, scoreType, scoreVal);

        // 2. 查询是否已存在（唯一索引：code + type）
        UserBehaviorScore exist = scoreMapper.selectByCodeAndType(behaviorCode, scoreType);
        int rows;

        if (exist == null) {
            // 新增
            UserBehaviorScore score = new UserBehaviorScore();
            score.setBehaviorCode(behaviorCode);
            score.setScoreType(scoreType);
            score.setScoreVal(scoreVal);
            rows = scoreMapper.insert(score);
            log.info("【行为分值配置】新增：code={}, type={}, val={}", behaviorCode, scoreType, scoreVal);
        } else {
            // 更新
            rows = scoreMapper.updateScoreByCodeAndType(behaviorCode, scoreType, scoreVal);
            log.info("【行为分值配置】更新：code={}, type={}, val={}", behaviorCode, scoreType, scoreVal);
        }

        boolean success = rows > 0;
        log.info("【行为分值配置】操作结果：{}", success ? "成功" : "失败");
        return success;
    }

    // ===================== 查询方法 =====================
    @Override
    public UserBehaviorScore getScoreByCodeAndType(BehaviorEnum behaviorCode, FeatureTypeEnum scoreType) {
        validateParam(behaviorCode, scoreType, null);
        UserBehaviorScore score = scoreMapper.selectByCodeAndType(behaviorCode, scoreType);
        log.info("【行为分值配置】查询：code={}, type={}, 结果={}", behaviorCode, scoreType, score != null);
        return score;
    }

    @Override
    public List<UserBehaviorScore> listScoresByBehaviorCode(BehaviorEnum behaviorCode) {
        if (behaviorCode == null) {
            throw new IllegalArgumentException("参数错误：行为编码不能为空");
        }
        List<UserBehaviorScore> list = scoreMapper.selectListByCode(behaviorCode);
        log.info("【行为分值配置】查询编码={} 所有分值，总数：{}", behaviorCode, list.size());
        return list;
    }

    // ===================== 更新方法 =====================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateScoreValue(BehaviorEnum behaviorCode, FeatureTypeEnum scoreType, Integer scoreVal) {
        validateParam(behaviorCode, scoreType, scoreVal);
        int rows = scoreMapper.updateScoreByCodeAndType(behaviorCode, scoreType, scoreVal);
        boolean success = rows > 0;
        log.info("【行为分值配置】单独更新分值：code={}, type={}, val={}, 结果={}",
                behaviorCode, scoreType, scoreVal, success ? "成功" : "不存在");
        return success;
    }

    // ===================== 删除方法 =====================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteScoreById(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("参数错误：ID必须为正整数");
        }
        int rows = scoreMapper.deleteById(id);
        boolean success = rows > 0;
        log.info("【行为分值配置】删除ID={}，结果={}", id, success ? "成功" : "不存在");
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteScoreByCodeAndType(BehaviorEnum behaviorCode, FeatureTypeEnum scoreType) {
        validateParam(behaviorCode, scoreType, null);
        int rows = scoreMapper.deleteByCodeAndType(behaviorCode, scoreType);
        boolean success = rows > 0;
        log.info("【行为分值配置】删除：code={}, type={}, 结果={}",
                behaviorCode, scoreType, success ? "成功" : "不存在");
        return success;
    }

    // ===================== 私有参数校验 =====================
    private void validateParam(BehaviorEnum behaviorCode, FeatureTypeEnum scoreType, Integer scoreVal) {
        if (behaviorCode == null) {
            throw new IllegalArgumentException("参数错误：行为编码不能为空");
        }
        // 枚举类型直接判空即可，无需isBlank
        if (scoreType == null) {
            throw new IllegalArgumentException("参数错误：分值类型不能为空");
        }
        // 分值范围校验（非空时）
        if (scoreVal != null && (scoreVal < MIN_SCORE || scoreVal > MAX_SCORE)) {
            throw new IllegalArgumentException("参数错误：分值必须在 " + MIN_SCORE + " ~ " + MAX_SCORE + " 之间");
        }
    }
}