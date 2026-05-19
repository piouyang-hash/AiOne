package org.myfx.controls.aione.SimulationGame.service;

/**
 * 全局游戏时间 服务接口
 */
public interface GameGlobalTimeService {

    /**
     * 获取当前全局游戏总秒数
     * @return 游戏时间（秒）
     */
    Integer getCurrentGameSeconds();

    /**
     * 扫描更新游戏时间（每次+60秒）
     */
    void updateGameTimeByScan();
}