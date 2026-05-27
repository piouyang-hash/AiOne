package org.myfx.controls.aione.SimulationGame.schedule;

import lombok.RequiredArgsConstructor;
import org.myfx.controls.aione.SimulationGame.service.GameGlobalTimeService;
import org.myfx.controls.aione.SimulationGame.service.upper.ScheduleEventLoopService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 模拟游戏 定时任务
 * 每分钟执行一次：更新游戏全局时间 + 后续事件循环逻辑
 */
@Component
@RequiredArgsConstructor
public class SimulationGameSchedule {

    // 注入全局时间服务
    private final GameGlobalTimeService gameGlobalTimeService;
    private final ScheduleEventLoopService scheduleEventLoopService;

    /**
     * 核心定时任务：每分钟执行一次 (1分钟 = 60000毫秒)
     * 仅做：更新游戏时间
     * 后续你自己在这里补充 事件判断、切换逻辑
     */
//    @Scheduled(initialDelay = 60000, fixedRate = 60000)
//    public void simulationGameTask() {
//        System.out.println("simulationGameTask");
//        // 1. 固定逻辑：每分钟更新游戏时间 +60秒
////        gameGlobalTimeService.updateGameTimeByScan();
//
//        // 2.每分钟扫描一次
////        scheduleEventLoopService.executeEventLoop();
//
//    }
}