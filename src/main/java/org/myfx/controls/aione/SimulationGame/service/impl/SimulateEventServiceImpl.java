package org.myfx.controls.aione.SimulationGame.service.impl;

import lombok.RequiredArgsConstructor;
import org.myfx.controls.aione.SimulationGame.dto.SimulateEventAddDTO;
import org.myfx.controls.aione.SimulationGame.entity.SimulateEvent;
import org.myfx.controls.aione.SimulationGame.mapper.SimulateEventMapper;
import org.myfx.controls.aione.SimulationGame.service.SimulateEventService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 模拟游戏-事件业务处理实现类
 */
@Service
@RequiredArgsConstructor
public class SimulateEventServiceImpl implements SimulateEventService {

    // 注入事件Mapper接口
    private final SimulateEventMapper simulateEventMapper;

    @Override
    public int addGameEvent(SimulateEventAddDTO eventAddDTO) {
        // 校验DTO非空（兜底校验，防止空指针）
        assert eventAddDTO != null : "新增事件失败：请求参数不能为空！";
        // 从DTO中提取三个参数
        String eventCode = eventAddDTO.getEventCode();
        String eventDesc = eventAddDTO.getEventDesc();

        // 原有校验：事件编码非空（兜底，对应DTO的@NotBlank校验）
        assert eventCode != null && !eventCode.trim().isEmpty() : "新增事件失败：事件编码不能为空！";

        // 调用Mapper执行新增，传递两个独立参数（不再传递DTO）
        return simulateEventMapper.insert(eventCode, eventDesc);
    }

    @Override
    public SimulateEvent getGameEventById(Integer eventId) {
        // assert校验：事件ID不能为空且大于0
        assert eventId != null && eventId > 0 : "查询事件失败：事件ID必须为非空且大于0的整数！";
        // 调用Mapper执行查询
        return simulateEventMapper.selectById(eventId);
    }

    @Override
    public List<SimulateEvent> listAllGameEvents() {
        // 无参数，无需assert校验，直接调用Mapper查询所有
        return simulateEventMapper.selectAll();
    }

    @Override
    public SimulateEvent getGameEventByCode(String eventCode) {
        // assert校验：事件编码枚举不能为空
        assert eventCode != null : "查询事件失败：事件编码不能为空！";
        // 调用Mapper执行查询
        return simulateEventMapper.selectByCode(eventCode);
    }

    @Override
    public int removeGameEventByCode(String eventCode) {
        // assert校验：事件编码枚举不能为空
        assert eventCode != null : "删除事件失败：事件编码不能为空！";
        // 调用Mapper执行删除
        return simulateEventMapper.deleteByCode(eventCode);
    }
}