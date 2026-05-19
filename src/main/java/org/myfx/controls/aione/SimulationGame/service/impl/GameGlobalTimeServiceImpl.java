package org.myfx.controls.aione.SimulationGame.service.impl;

import lombok.RequiredArgsConstructor;
import org.myfx.controls.aione.SimulationGame.mapper.GameGlobalTimeMapper;
import org.myfx.controls.aione.SimulationGame.service.GameGlobalTimeService;
import org.springframework.stereotype.Service;

/**
 * 全局游戏时间 服务实现
 */
@Service
@RequiredArgsConstructor
public class GameGlobalTimeServiceImpl implements GameGlobalTimeService {

    // 注入Mapper
    private final GameGlobalTimeMapper gameGlobalTimeMapper;

    @Override
    public Integer getCurrentGameSeconds() {
        // 直接调用Mapper，返回核心时间秒数
        return gameGlobalTimeMapper.selectGlobalTime().getGlobalGameSeconds();
    }

    @Override
    public void updateGameTimeByScan() {
        // 直接调用Mapper，更新+60秒
        gameGlobalTimeMapper.updateGlobalTimeAdd60();
    }
}