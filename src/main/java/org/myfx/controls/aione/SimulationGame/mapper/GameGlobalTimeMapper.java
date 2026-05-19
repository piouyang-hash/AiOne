package org.myfx.controls.aione.SimulationGame.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.myfx.controls.aione.SimulationGame.entity.GameGlobalTime;

/**
 * 全局游戏时间 Mapper
 * 全表仅ID=1一条数据
 */
@Mapper
public interface GameGlobalTimeMapper {

    /**
     * 查询唯一的全局游戏时间
     */
    GameGlobalTime selectGlobalTime();

    /**
     * 游戏时间 +60 秒（每次扫描执行）
     */
    int updateGlobalTimeAdd60();
}