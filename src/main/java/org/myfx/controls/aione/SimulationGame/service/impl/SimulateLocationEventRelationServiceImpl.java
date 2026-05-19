package org.myfx.controls.aione.SimulationGame.service.impl;

import lombok.RequiredArgsConstructor;
import org.myfx.controls.aione.SimulationGame.dto.SimulateLocationEventRelationAddDTO;
import org.myfx.controls.aione.SimulationGame.entity.SimulateEvent;
import org.myfx.controls.aione.SimulationGame.entity.SimulateLocation;
import org.myfx.controls.aione.SimulationGame.entity.SimulateLocationEventRelation;
import org.myfx.controls.aione.SimulationGame.mapper.SimulateLocationEventRelationMapper;
import org.myfx.controls.aione.SimulationGame.service.SimulateEventService;
import org.myfx.controls.aione.SimulationGame.service.SimulateLocationEventRelationService;
import org.myfx.controls.aione.SimulationGame.service.SimulateLocationService;
import org.springframework.util.Assert;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 模拟游戏-地点与事件关联 业务层实现类
 * 核心逻辑：参数校验（Assert） + 校验地点/事件存在性 + 调用Mapper完成数据库操作
 * 【重要】循环依赖规避声明：
 * 不要将本关联业务（SimulateLocationEventRelationService）注入到 SimulateLocationService/SimulateEventService 中，
 * 否则会出现Spring循环依赖异常；仅允许在本类中注入地点/事件业务做存在性校验，反向注入禁止！
 */
@Service
@RequiredArgsConstructor
public class SimulateLocationEventRelationServiceImpl implements SimulateLocationEventRelationService {

    // 注入关联Mapper
    private final SimulateLocationEventRelationMapper simulateLocationEventRelationMapper;
    // 注入地点业务Service（仅用于校验地点编码是否存在，禁止反向注入本类）
    private final SimulateLocationService simulateLocationService;
    // 注入事件业务Service（仅用于校验事件编码是否存在，禁止反向注入本类）
    private final SimulateEventService simulateEventService;

    /**
     * 新增关联关系：
     * 1. 参数非空校验 → 2. 校验地点/事件编码存在性 → 3. 校验时长合法性 → 4. 调用Mapper执行新增
     */
    @Override
    public int addRelation(SimulateLocationEventRelationAddDTO relationAddDTO) {
        // 1. 基础参数非空校验（DTO入参）
        Assert.notNull(relationAddDTO, "新增关联请求参数不能为空！");
        Assert.hasText(relationAddDTO.getLocationCode(), "地点编码不能为空！");
        Assert.hasText(relationAddDTO.getEventCode(), "事件编码不能为空！");
        Assert.notNull(relationAddDTO.getEventDuration(), "事件持续时长不能为空！");

        // 2. 校验事件持续时长合法性（至少1秒）
        if (relationAddDTO.getEventDuration() < 1) {
            throw new RuntimeException("新增关联失败：事件持续时长不能小于1秒！");
        }

        // 3. 校验地点编码是否存在
        SimulateLocation location = simulateLocationService.getGameLocationByCode(relationAddDTO.getLocationCode());
        if (location == null) {
            throw new RuntimeException("新增关联失败：不存在编码为【" + relationAddDTO.getLocationCode() + "】的地点！");
        }

        // 4. 校验事件编码是否存在
        SimulateEvent event = simulateEventService.getGameEventByCode(relationAddDTO.getEventCode());
        if (event == null) {
            throw new RuntimeException("新增关联失败：不存在编码为【" + relationAddDTO.getEventCode() + "】的事件！");
        }

        // 5. 调用Mapper执行新增（传入三个参数）
        return simulateLocationEventRelationMapper.insert(
                relationAddDTO.getLocationCode(),
                relationAddDTO.getEventCode(),
                relationAddDTO.getEventDuration()
        );
    }

    // ========== 新增存在性校验：根据地点编码查询关联事件 ==========
    @Override
    public List<SimulateLocationEventRelation> getRelationsByLocationCode(String locationCode) {
        // 1. 参数非空校验
        Assert.hasText(locationCode, "查询关联事件失败：地点编码不能为空！");

        // 2. 校验地点编码是否存在（不存在则抛运行时异常）
        SimulateLocation location = simulateLocationService.getGameLocationByCode(locationCode);
        if (location == null) {
            throw new RuntimeException("查询关联事件失败：不存在编码为【" + locationCode + "】的地点！");
        }

        // 3. 地点存在，调用Mapper查询关联事件（无关联则返回空列表）
        return simulateLocationEventRelationMapper.selectByLocationCode(locationCode);
    }

    // ========== 新增存在性校验：根据事件编码查询关联地点 ==========
    @Override
    public List<SimulateLocationEventRelation> getRelationsByEventCode(String eventCode) {
        // 1. 参数非空校验
        Assert.hasText(eventCode, "查询关联地点失败：事件编码不能为空！");

        // 2. 校验事件编码是否存在（不存在则抛运行时异常）
        SimulateEvent event = simulateEventService.getGameEventByCode(eventCode);
        if (event == null) {
            throw new RuntimeException("查询关联地点失败：不存在编码为【" + eventCode + "】的事件！");
        }

        // 3. 事件存在，调用Mapper查询关联地点（无关联则返回空列表）
        return simulateLocationEventRelationMapper.selectByEventCode(eventCode);
    }

    // ========== 新增存在性校验：精准查询关联关系 ==========
    @Override
    public SimulateLocationEventRelation getRelationByTwoCode(String locationCode, String eventCode) {
        // 1. 参数非空校验
        Assert.hasText(locationCode, "精准查询关联关系失败：地点编码不能为空！");
        Assert.hasText(eventCode, "精准查询关联关系失败：事件编码不能为空！");

        // 2. 校验地点编码是否存在（不存在则抛运行时异常）
        SimulateLocation location = simulateLocationService.getGameLocationByCode(locationCode);
        if (location == null) {
            throw new RuntimeException("精准查询关联关系失败：不存在编码为【" + locationCode + "】的地点！");
        }

        // 3. 校验事件编码是否存在（不存在则抛运行时异常）
        SimulateEvent event = simulateEventService.getGameEventByCode(eventCode);
        if (event == null) {
            throw new RuntimeException("精准查询关联关系失败：不存在编码为【" + eventCode + "】的事件！");
        }

        // 4. 地点/事件都存在，调用Mapper精准查询（无关联则返回null）
        return simulateLocationEventRelationMapper.selectByTwoCode(locationCode, eventCode);
    }

    @Override
    public Integer getDurationByTwoCode(String locationCode, String eventCode) {
        // 1. 调用原有方法查询关联关系
        SimulateLocationEventRelation relation = getRelationByTwoCode(locationCode, eventCode);

        // 2. 复用你原来的校验逻辑
        if (relation == null) {
            throw new RuntimeException("未查询到[地点:" + locationCode + "][事件:" + eventCode + "]的关联关系");
        }

        Integer expireSeconds = relation.getEventDuration();
        if (expireSeconds == null || expireSeconds <= 0) {
            throw new RuntimeException("[地点:" + locationCode + "][事件:" + eventCode + "]的持续秒数无效：" + expireSeconds);
        }

        // 3. 返回合法时长
        return expireSeconds;
    }

    // ========== 无校验：查询所有关联关系（无需校验主体，直接返回） ==========
    @Override
    public List<SimulateLocationEventRelation> getAllRelations() {
        return simulateLocationEventRelationMapper.selectAll();
    }

    // ========== 新增存在性校验：精准删除关联关系 ==========
    @Override
    public int removeRelationByTwoCode(String locationCode, String eventCode) {
        // 1. 参数非空校验
        Assert.hasText(locationCode, "删除关联关系失败：地点编码不能为空！");
        Assert.hasText(eventCode, "删除关联关系失败：事件编码不能为空！");

        // 2. 校验地点编码是否存在（不存在则抛运行时异常）
        SimulateLocation location = simulateLocationService.getGameLocationByCode(locationCode);
        if (location == null) {
            throw new RuntimeException("删除关联关系失败：不存在编码为【" + locationCode + "】的地点！");
        }

        // 3. 校验事件编码是否存在（不存在则抛运行时异常）
        SimulateEvent event = simulateEventService.getGameEventByCode(eventCode);
        if (event == null) {
            throw new RuntimeException("删除关联关系失败：不存在编码为【" + eventCode + "】的事件！");
        }

        // 4. 校验关联关系是否存在（无关联则抛运行时异常）
        SimulateLocationEventRelation relation = simulateLocationEventRelationMapper.selectByTwoCode(locationCode, eventCode);
        if (relation == null) {
            throw new RuntimeException("删除关联关系失败：编码【" + locationCode + "】的地点与编码【" + eventCode + "】的事件无关联关系！");
        }

        // 5. 所有校验通过，调用Mapper执行删除
        return simulateLocationEventRelationMapper.deleteByTwoCode(locationCode, eventCode);
    }

}