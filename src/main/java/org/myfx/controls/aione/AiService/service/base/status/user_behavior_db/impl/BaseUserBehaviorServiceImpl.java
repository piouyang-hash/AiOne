package org.myfx.controls.aione.AiService.service.base.status.user_behavior_db.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.common.user_behavior_db.BehaviorEnum;
import org.myfx.controls.aione.AiService.entity.user_behavior_db.BaseUserBehavior;
import org.myfx.controls.aione.AiService.mapper.user_behavior_db.BaseUserBehaviorMapper;
import org.myfx.controls.aione.AiService.service.base.status.user_behavior_db.BaseUserBehaviorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BaseUserBehaviorServiceImpl implements BaseUserBehaviorService {

    private final BaseUserBehaviorMapper baseUserBehaviorMapper;

    // ===================== 核心新增 =====================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addBehavior(BehaviorEnum behaviorCode, String behaviorName) {
        // 1. 参数校验
        if (behaviorCode == null) {
            throw new IllegalArgumentException("参数错误：行为编码不能为空");
        }
        if (behaviorName == null || behaviorName.isBlank()) {
            throw new IllegalArgumentException("参数错误：行为名称不能为空");
        }

        // 2. 幂等校验：行为编码已存在则禁止新增
        BaseUserBehavior exist = baseUserBehaviorMapper.selectByCode(behaviorCode);
        if (exist != null) {
            throw new RuntimeException("行为已存在：" + behaviorCode.getDesc());
        }

        // 3. 组装实体
        BaseUserBehavior behavior = new BaseUserBehavior();
        behavior.setBehaviorCode(behaviorCode);
        behavior.setBehaviorName(behaviorName);

        // 4. 执行插入
        int rows = baseUserBehaviorMapper.insert(behavior);
        boolean success = rows > 0;
        log.info("【用户行为字典】新增行为：code={}, name={}, 结果={}",
                behaviorCode, behaviorName, success ? "成功" : "失败");
        return success;
    }

    // ===================== 查询方法 =====================
    @Override
    public BaseUserBehavior getBehaviorById(Integer behaviorId) {
        if (behaviorId == null || behaviorId <= 0) {
            throw new IllegalArgumentException("参数错误：行为ID必须为正整数");
        }
        BaseUserBehavior behavior = baseUserBehaviorMapper.selectById(behaviorId);
        log.info("【用户行为字典】根据ID查询：id={}, 结果={}", behaviorId, behavior != null);
        return behavior;
    }

    @Override
    public BaseUserBehavior getBehaviorByCode(BehaviorEnum behaviorCode) {
        if (behaviorCode == null) {
            throw new IllegalArgumentException("参数错误：行为编码不能为空");
        }
        BaseUserBehavior behavior = baseUserBehaviorMapper.selectByCode(behaviorCode);
        log.info("【用户行为字典】根据编码查询：code={}, 结果={}", behaviorCode, behavior != null);
        return behavior;
    }

    @Override
    public List<BaseUserBehavior> listAllBehaviors() {
        List<BaseUserBehavior> list = baseUserBehaviorMapper.selectAll();
        log.info("【用户行为字典】查询全部行为，总数：{}", list == null ? 0 : list.size());
        return list == null ? List.of() : list;
    }

    // ===================== 更新方法 =====================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateBehaviorById(BaseUserBehavior behavior) {
        if (behavior == null || behavior.getBehaviorId() == null) {
            throw new IllegalArgumentException("参数错误：必须携带行为ID");
        }
        // 校验数据存在
        BaseUserBehavior exist = baseUserBehaviorMapper.selectById(behavior.getBehaviorId());
        if (exist == null) {
            log.warn("【用户行为字典】更新失败：ID={} 不存在", behavior.getBehaviorId());
            return false;
        }
        int rows = baseUserBehaviorMapper.updateById(behavior);
        boolean success = rows > 0;
        log.info("【用户行为字典】更新ID={}，结果={}", behavior.getBehaviorId(), success ? "成功" : "失败");
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateBehaviorNameByCode(BehaviorEnum behaviorCode, String behaviorName) {
        if (behaviorCode == null) {
            throw new IllegalArgumentException("参数错误：行为编码不能为空");
        }
        if (behaviorName == null || behaviorName.isBlank()) {
            throw new IllegalArgumentException("参数错误：行为名称不能为空");
        }
        // 校验存在
        BaseUserBehavior exist = baseUserBehaviorMapper.selectByCode(behaviorCode);
        if (exist == null) {
            log.warn("【用户行为字典】更新名称失败：编码={} 不存在", behaviorCode);
            return false;
        }
        BaseUserBehavior update = new BaseUserBehavior();
        update.setBehaviorCode(behaviorCode);
        update.setBehaviorName(behaviorName);
        int rows = baseUserBehaviorMapper.updateNameByCode(update);
        boolean success = rows > 0;
        log.info("【用户行为字典】更新编码={} 名称，结果={}", behaviorCode, success ? "成功" : "失败");
        return success;
    }

    // ===================== 删除方法 =====================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBehaviorByCode(BehaviorEnum behaviorCode) {
        if (behaviorCode == null) {
            throw new IllegalArgumentException("参数错误：行为编码不能为空");
        }
        int rows = baseUserBehaviorMapper.deleteByCode(behaviorCode);
        boolean success = rows > 0;
        log.info("【用户行为字典】删除编码={}，结果={}", behaviorCode, success ? "成功" : "不存在");
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBehaviorById(Integer behaviorId) {
        if (behaviorId == null || behaviorId <= 0) {
            throw new IllegalArgumentException("参数错误：行为ID必须为正整数");
        }
        int rows = baseUserBehaviorMapper.deleteById(behaviorId);
        boolean success = rows > 0;
        log.info("【用户行为字典】删除ID={}，结果={}", behaviorId, success ? "成功" : "不存在");
        return success;
    }
}