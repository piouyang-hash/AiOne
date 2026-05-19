package org.myfx.controls.aione.AiService.service.base.status.ai_behavior_db.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.common.ai_chat_db.AiBehaviorEnum;
import org.myfx.controls.aione.AiService.common.ai_chat_db.EmotionTypeEnum;
import org.myfx.controls.aione.AiService.dto.AiBehaviorScoreAddDTO;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiBehaviorScore;
import org.myfx.controls.aione.AiService.mapper.ai_chat_db.AiBehaviorScoreMapper;
import org.myfx.controls.aione.AiService.service.base.status.ai_behavior_db.AiBehaviorScoreService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import java.util.List;

/**
 * AI行为分值配置 业务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiBehaviorScoreServiceImpl implements AiBehaviorScoreService {

    private final AiBehaviorScoreMapper aiBehaviorScoreMapper;

    // ====================== 新增 ======================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addAiBehaviorScore(AiBehaviorScoreAddDTO addDTO) {
        // 1. 从DTO中提取参数
        AiBehaviorEnum behaviorCode = addDTO.getAiBehaviorCode();
        EmotionTypeEnum scoreType = addDTO.getScoreType();
        Integer scoreVal = addDTO.getScoreVal();

        // 2. 参数校验（完全保留原有逻辑）
        Assert.notNull(behaviorCode, "参数错误：行为编码不能为空");
        Assert.notNull(scoreType, "参数错误：分值类型不能为空");
        if (scoreVal != null) {
            Assert.isTrue(scoreVal >= -100 && scoreVal <= 100, "参数错误：分值必须在-100~100之间");
        }

        // 3. 组装实体（MP枚举自动映射）
        AiBehaviorScore score = new AiBehaviorScore();
        score.setBehaviorCode(behaviorCode);
        score.setScoreType(scoreType);
        score.setScoreVal(scoreVal);

        // 4. 执行插入
        int rows = aiBehaviorScoreMapper.insert(score);
        boolean success = rows > 0;
        log.info("【AI行为分值】新增配置：行为={}，类型={}，分值={}，结果={}",
                behaviorCode, scoreType, scoreVal, success ? "成功" : "失败");
        return success;
    }

    // ====================== 更新 ======================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAiBehaviorScoreById(AiBehaviorScore score) {
        // 1. 参数校验
        Assert.notNull(score, "参数错误：实体不能为空");
        Assert.notNull(score.getId(), "参数错误：ID不能为空");
        if (score.getScoreVal() != null) {
            Assert.isTrue(score.getScoreVal() >= -100 && score.getScoreVal() <= 100, "参数错误：分值范围异常");
        }

        // 2. 校验存在
        AiBehaviorScore exist = aiBehaviorScoreMapper.selectById(score.getId());
        if (exist == null) {
            log.warn("【AI行为分值】更新失败，ID={} 不存在", score.getId());
            return false;
        }

        // 3. 执行更新
        int rows = aiBehaviorScoreMapper.updateById(score);
        boolean success = rows > 0;
        log.info("【AI行为分值】更新ID={}，结果={}", score.getId(), success ? "成功" : "失败");
        return success;
    }

    // ====================== 删除 ======================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAiBehaviorScoreById(Integer id) {
        Assert.notNull(id, "参数错误：ID不能为空");
        int rows = aiBehaviorScoreMapper.deleteById(id);
        boolean success = rows > 0;
        log.info("【AI行为分值】删除ID={}，结果={}", id, success ? "成功" : "失败");
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAiBehaviorScoreByCode(AiBehaviorEnum behaviorCode) {
        Assert.notNull(behaviorCode, "参数错误：行为编码不能为空");
        int rows = aiBehaviorScoreMapper.deleteByBehaviorCode(behaviorCode);
        boolean success = rows > 0;
        log.info("【AI行为分值】删除行为={} 的所有分值，结果={}", behaviorCode, success ? "成功" : "失败");
        return success;
    }

    // ====================== 查询 ======================
    @Override
    public AiBehaviorScore getAiBehaviorScoreById(Integer id) {
        Assert.notNull(id, "参数错误：ID不能为空");
        AiBehaviorScore score = aiBehaviorScoreMapper.selectById(id);
        log.info("【AI行为分值】查询ID={}，结果={}", id, score != null ? "存在" : "不存在");
        return score;
    }

    @Override
    public List<AiBehaviorScore> getScoreListByBehaviorCode(AiBehaviorEnum behaviorCode) {
        Assert.notNull(behaviorCode, "参数错误：行为编码不能为空");
        List<AiBehaviorScore> list = aiBehaviorScoreMapper.selectListByBehaviorCode(behaviorCode);
        log.info("【AI行为分值】查询行为={} 的分值列表，共{}条", behaviorCode, list.size());
        return list;
    }

    @Override
    public AiBehaviorScore getScoreByCodeAndType(AiBehaviorEnum behaviorCode, EmotionTypeEnum scoreType) {
        Assert.notNull(behaviorCode, "参数错误：行为编码不能为空");
        Assert.notNull(scoreType, "参数错误：分值类型不能为空");
        AiBehaviorScore score = aiBehaviorScoreMapper.selectByCodeAndType(behaviorCode, scoreType);
        log.info("【AI行为分值】精准查询：行为={}，类型={}，结果={}",
                behaviorCode, scoreType, score != null ? "存在" : "不存在");
        return score;
    }
}