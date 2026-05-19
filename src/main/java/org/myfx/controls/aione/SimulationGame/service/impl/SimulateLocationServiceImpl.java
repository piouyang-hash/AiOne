package org.myfx.controls.aione.SimulationGame.service.impl;

import lombok.RequiredArgsConstructor;
import org.myfx.controls.aione.SimulationGame.entity.SimulateLocation;
import org.myfx.controls.aione.SimulationGame.mapper.SimulateLocationMapper;
import org.myfx.controls.aione.SimulationGame.service.SimulateLocationService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 模拟游戏-地点业务处理实现类
 */
@Service
@RequiredArgsConstructor
public class SimulateLocationServiceImpl implements SimulateLocationService {

    // 注入地点Mapper接口
    private final SimulateLocationMapper simulateLocationMapper;

    @Override
    public int addGameLocation(String locationCode, String locationDesc) {
        // assert校验：地点编码枚举不能为空
        assert locationCode != null : "新增地点失败：地点编码不能为空！";
        // 调用Mapper执行新增
        return simulateLocationMapper.insert(locationCode, locationDesc);
    }

    @Override
    public SimulateLocation getGameLocationById(Integer locationId) {
        // assert校验：地点ID不能为空且大于0
        assert locationId != null && locationId > 0 : "查询地点失败：地点ID必须为非空且大于0的整数！";
        // 调用Mapper执行查询
        return simulateLocationMapper.selectById(locationId);
    }

    @Override
    public List<SimulateLocation> listAllGameLocations() {
        // 无参数，无需assert校验，直接调用Mapper查询所有
        return simulateLocationMapper.selectAll();
    }

    @Override
    public SimulateLocation getGameLocationByCode(String locationCode) {
        // assert校验：地点编码枚举不能为空
        assert locationCode != null : "查询地点失败：地点编码不能为空！";
        // 调用Mapper执行查询
        return simulateLocationMapper.selectByCode(locationCode);
    }

    @Override
    public int removeGameLocationByCode(String locationCode) {
        // assert校验：地点编码枚举不能为空
        assert locationCode != null : "删除地点失败：地点编码不能为空！";
        // 调用Mapper执行删除
        return simulateLocationMapper.deleteByCode(locationCode);
    }
}