package org.myfx.controls.aione.AiService.service.base.status.ai_behavior_db.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.common.ai_chat_db.AiBehaviorEnum;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.BaseAiBehavior;
import org.myfx.controls.aione.AiService.mapper.ai_chat_db.BaseAiBehaviorMapper;
import org.myfx.controls.aione.AiService.service.base.status.ai_behavior_db.BaseAiBehaviorService;
import org.springframework.util.Assert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 基础AI行为字典业务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BaseAiBehaviorServiceImpl implements BaseAiBehaviorService {

    private final BaseAiBehaviorMapper baseAiBehaviorMapper;

    // ========== 核心方法：新增AI行为（适配新表结构） ==========
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addAiBehavior(AiBehaviorEnum aiBehaviorCode, String behaviorName) {
        // 1. 基础参数校验
        Assert.notNull(aiBehaviorCode, "参数不合法错误：AI行为编码不能为空");
        Assert.hasText(behaviorName, "参数不合法错误：AI行为名称不能为空");

        // 2. 幂等校验：查询是否已存在该AI行为
        BaseAiBehavior existAiBehavior = baseAiBehaviorMapper.selectByCode(aiBehaviorCode);
        if (existAiBehavior != null) {
            throw new RuntimeException("运行时错误：AI行为【" + aiBehaviorCode.getDesc() + "】已存在，禁止重复新增");
        }

        // 3. 组装实体（仅包含编码+名称，无score）
        BaseAiBehavior baseAiBehavior = new BaseAiBehavior();
        baseAiBehavior.setBehaviorCode(aiBehaviorCode);
        baseAiBehavior.setBehaviorName(behaviorName);

        // 4. 执行插入
        int affectedRows = baseAiBehaviorMapper.insert(baseAiBehavior);
        boolean success = affectedRows > 0;
        log.info("【基础AI行为】新增AI行为[{}][{}]，结果：{}",
                aiBehaviorCode, behaviorName, success ? "成功" : "失败");
        return success;
    }

    // ========== 查询方法（保留，小幅优化） ==========
    @Override
    public BaseAiBehavior getAiBehaviorById(Integer aiBehaviorId) {
        // 1. 基础参数校验
        Assert.notNull(aiBehaviorId, "参数不合法错误：AI行为ID不能为空");
        Assert.isTrue(aiBehaviorId > 0, "参数不合法错误：AI行为ID需为正整数");

        // 2. 执行查询
        BaseAiBehavior aiBehavior = baseAiBehaviorMapper.selectById(aiBehaviorId);
        log.info("【基础AI行为】查询ID[{}]的AI行为信息，结果：{}",
                aiBehaviorId, aiBehavior != null ? "存在" : "不存在");
        return aiBehavior;
    }

    @Override
    public BaseAiBehavior getAiBehaviorByCode(AiBehaviorEnum aiBehaviorCode) {
        // 基础参数校验
        Assert.notNull(aiBehaviorCode, "参数不合法错误：AI行为编码不能为空");

        // 调用Mapper查询
        BaseAiBehavior aiBehavior = baseAiBehaviorMapper.selectByCode(aiBehaviorCode);
        log.info("【基础AI行为】查询编码[{}]的AI行为信息，结果：{}",
                aiBehaviorCode, aiBehavior != null ? "存在" : "不存在");
        return aiBehavior;
    }

    @Override
    public List<BaseAiBehavior> listAllAiBehaviors() {
        // 无参数校验，直接调用Mapper，保证返回非null
        List<BaseAiBehavior> aiBehaviorList = baseAiBehaviorMapper.selectAll();
        log.info("【基础AI行为】查询所有AI行为列表，共{}条记录",
                aiBehaviorList == null ? 0 : aiBehaviorList.size());
        return aiBehaviorList == null ? List.of() : aiBehaviorList;
    }

    // ========== 更新方法（适配新表：更新名称） ==========
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAiBehaviorNameByCode(AiBehaviorEnum aiBehaviorCode, String behaviorName) {
        // 1. 基础参数校验
        Assert.notNull(aiBehaviorCode, "参数不合法错误：AI行为编码不能为空");
        Assert.hasText(behaviorName, "参数不合法错误：AI行为名称不能为空");

        // 2. 校验AI行为是否存在
        BaseAiBehavior existAiBehavior = baseAiBehaviorMapper.selectByCode(aiBehaviorCode);
        if (existAiBehavior == null) {
            log.warn("【基础AI行为】更新编码[{}]的名称失败：AI行为不存在", aiBehaviorCode);
            return false;
        }

        // 3. 组装参数执行更新
        BaseAiBehavior updateParam = new BaseAiBehavior();
        updateParam.setBehaviorCode(aiBehaviorCode);
        updateParam.setBehaviorName(behaviorName);
        int affectedRows = baseAiBehaviorMapper.updateNameByCode(updateParam);
        boolean success = affectedRows > 0;
        log.info("【基础AI行为】更新编码[{}]的名称为[{}]，结果：{}",
                aiBehaviorCode, behaviorName, success ? "成功" : "失败（无匹配记录）");
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAiBehaviorById(BaseAiBehavior aiBehavior) {
        // 1. 基础参数校验
        Assert.notNull(aiBehavior, "参数不合法错误：AI行为实体不能为空");
        Assert.notNull(aiBehavior.getBehaviorId(), "参数不合法错误：AI行为实体必须包含AI行为ID");

        // 2. 校验AI行为是否存在
        BaseAiBehavior existAiBehavior = baseAiBehaviorMapper.selectById(aiBehavior.getBehaviorId());
        if (existAiBehavior == null) {
            log.warn("【基础AI行为】更新ID[{}]的AI行为失败：AI行为不存在", aiBehavior.getBehaviorId());
            return false;
        }

        // 3. 执行动态更新（无score校验）
        int affectedRows = baseAiBehaviorMapper.updateById(aiBehavior);
        boolean success = affectedRows > 0;
        log.info("【基础AI行为】更新ID[{}]的AI行为，结果：{}",
                aiBehavior.getBehaviorId(), success ? "成功" : "失败（无匹配记录）");
        return success;
    }

    // ========== 删除方法（完全保留，无修改） ==========
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAiBehaviorByCode(AiBehaviorEnum aiBehaviorCode) {
        // 1. 基础参数校验
        Assert.notNull(aiBehaviorCode, "参数不合法错误：AI行为编码不能为空");

        // 2. 执行删除
        int affectedRows = baseAiBehaviorMapper.deleteByCode(aiBehaviorCode);
        boolean success = affectedRows > 0;
        log.info("【基础AI行为】删除编码[{}]的AI行为，结果：{}",
                aiBehaviorCode, success ? "成功" : "失败（无匹配记录）");
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAiBehaviorById(Integer aiBehaviorId) {
        // 1. 基础参数校验
        Assert.notNull(aiBehaviorId, "参数不合法错误：AI行为ID不能为空");
        Assert.isTrue(aiBehaviorId > 0, "参数不合法错误：AI行为ID需为正整数");

        // 2. 执行删除
        int affectedRows = baseAiBehaviorMapper.deleteById(aiBehaviorId);
        boolean success = affectedRows > 0;
        log.info("【基础AI行为】删除ID[{}]的AI行为，结果：{}",
                aiBehaviorId, success ? "成功" : "失败（无匹配记录）");
        return success;
    }
}