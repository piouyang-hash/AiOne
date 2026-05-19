package org.myfx.controls.aione.SimulationGame.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.SimulationGame.dto.SimulateEventSequenceAddDTO;
import org.myfx.controls.aione.SimulationGame.entity.SimulateEventSequence;
import org.myfx.controls.aione.SimulationGame.entity.SimulateLocationEventRelation;
import org.myfx.controls.aione.SimulationGame.mapper.SimulateEventSequenceMapper;
import org.myfx.controls.aione.SimulationGame.service.SimulateEventSequenceService;
import org.myfx.controls.aione.SimulationGame.service.SimulateLocationEventRelationService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;

/**
 * 模拟游戏-事件执行序列规则 业务层实现类
 * 【循环依赖声明】：
 * 1. 本类注入 {@link SimulateLocationEventRelationService}（地点-事件关联业务类）的原因：
 *    事件执行序列规则依赖「已存在的地点-事件关联关系」，新增序列前需校验 locationCode + eventCode 组合是否已配置关联，保证数据合法性；
 * 2. 避免循环依赖的约束：
 *    关联业务类（SimulateLocationEventRelationService）是事件和地点的关联层，一旦在本类注入该关联类，
 *    禁止将「事件序列相关组件（如本类/SimulateEventSequenceMapper）」单独注入到地点子类（如SimulateLocationServiceImpl）
 *    或事件子类（如SimulateEventServiceImpl）中，否则会触发Spring循环依赖异常。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SimulateEventSequenceServiceImpl implements SimulateEventSequenceService {

    /** 注入事件序列Mapper（核心数据操作） */
    private final SimulateEventSequenceMapper eventSequenceMapper;

    /** 注入地点-事件关联业务类（用于校验关联关系是否存在） */
    private final SimulateLocationEventRelationService simulateLocationEventRelationService;

    @Override
    public boolean saveEventSequence(SimulateEventSequenceAddDTO addDTO) {
        // ========== 1. 基础参数校验（增强：覆盖所有必填字段+格式） ==========
        Assert.notNull(addDTO, "事件序列规则新增DTO不能为空");
        Assert.notNull(addDTO.getVersion(), "版本号不能为空（需为≥1的正整数）");
        Assert.isTrue(addDTO.getVersion() >= 1, "版本号必须为≥1的正整数");
        Assert.hasText(addDTO.getLocationCode(), "地点编码不能为空（仅支持大写字母+下划线）");
        Assert.hasText(addDTO.getEventCode(), "事件编码不能为空（仅支持大写字母+下划线）");
        Assert.notNull(addDTO.getSeqNum(), "执行次序不能为空（需为≥1的正整数）");
        Assert.isTrue(addDTO.getSeqNum() >= 1, "执行次序必须为≥1的正整数");

        // ========== 2. 核心校验：调用关联业务类，检查地点+事件是否已配置关联 ==========
        SimulateLocationEventRelation relation = simulateLocationEventRelationService.getRelationByTwoCode(
                addDTO.getLocationCode().trim(),
                addDTO.getEventCode().trim()
        );
        Assert.notNull(relation,
                String.format("新增失败：地点编码【%s】和事件编码【%s】未配置关联关系，请先在「场景-事件关联配置」中添加",
                        addDTO.getLocationCode(), addDTO.getEventCode())
        );

        // ========== 3. DTO 转换为实体类（自动拷贝属性） ==========
        SimulateEventSequence sequence = new SimulateEventSequence();
        BeanUtils.copyProperties(addDTO, sequence);
        // 补充：去除编码首尾空格，保证数据一致性
        sequence.setLocationCode(addDTO.getLocationCode().trim());
        sequence.setEventCode(addDTO.getEventCode().trim());

        // ========== 4. 调用Mapper新增，影响行数>0则返回成功 ==========
        int affectRows = eventSequenceMapper.insert(sequence);
        return affectRows > 0;
    }

    @Override
    public List<SimulateEventSequence> listEventSequenceByVersion(Integer version) {
        // 基础参数校验：版本号不能为空
        Assert.notNull(version, "版本号不能为空");

        // 调用Mapper查询，无数据返回空列表（避免返回null）
        List<SimulateEventSequence> sequenceList = eventSequenceMapper.selectByVersion(version);
        return sequenceList == null ? Collections.emptyList() : sequenceList;
    }

    @Override
    public List<SimulateEventSequence> listAllEventSequence() {
        List<SimulateEventSequence> sequenceList = eventSequenceMapper.listAllEventSequence();
        // 空值处理：返回空列表而非null，避免前端空指针
        return sequenceList == null ? Collections.emptyList() : sequenceList;
    }

    @Override
    public SimulateEventSequence getFirstEventSequence() {
        // 首事件固定规则：版本1 + 执行序号1（直白清晰，无任何诡异逻辑）
        int firstVersion = 1;
        int firstSeqNum = 1;

        // 直接按固定参数查询
        SimulateEventSequence firstEvent = eventSequenceMapper.selectByVersionAndSeqNum(firstVersion, firstSeqNum);

        // 查不到则抛出异常（保持原有异常逻辑）
        if (firstEvent == null) {
            throw new RuntimeException("查找第一个事件失败：未查询到版本=" + firstVersion + "，序号=" + firstSeqNum + "的事件数据");
        }

        return firstEvent;
    }

    @Override
    public SimulateEventSequence getEventSequenceByVersionAndSeqNum(Integer version, Integer seqNum) {
        assert version != null : "获取事件队列失败：版本号不能为空";
        assert version > 0 : "获取事件队列失败：版本号必须大于0，当前值：" + version;
        assert seqNum != null : "获取事件队列失败：执行次序不能为空";
        assert seqNum > 0 : "获取事件队列失败：执行次序必须大于0，当前值：" + seqNum;

        SimulateEventSequence eventSequence = eventSequenceMapper.selectByVersionAndSeqNum(version, seqNum);

        // ========== 查不到数据抛运行时异常 ==========
        if (eventSequence == null) {
            throw new RuntimeException(String.format("获取事件队列失败：未找到版本号[%d]、执行次序[%d]对应的事件数据", version, seqNum));
        }

        return eventSequence;
    }

    @Override
    public boolean removeEventSequenceByVersion(Integer version) {
        // 基础参数校验：版本号不能为空
        Assert.notNull(version, "版本号不能为空");

        // 调用Mapper删除，影响行数>0则返回成功
        int affectRows = eventSequenceMapper.deleteByVersion(version);
        return affectRows > 0;
    }
}