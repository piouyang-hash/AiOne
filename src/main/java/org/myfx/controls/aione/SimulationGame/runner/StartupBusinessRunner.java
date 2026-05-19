package org.myfx.controls.aione.SimulationGame.runner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.SimulationGame.service.upper.EventRelayService;
import org.myfx.controls.aione.SimulationGame.service.upper.ScheduleEventLoopService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component // 标记为Spring组件，让容器扫描并管理
@Slf4j
@RequiredArgsConstructor
public class StartupBusinessRunner implements CommandLineRunner {

    private final EventRelayService eventRelayService;
    private final ScheduleEventLoopService scheduleEventLoopService;

    @Override
    public void run(String... args) {
        // 第一步：调用initializeEventRelay方法（已包含幂等性判断）
        // 如果有记录，该方法会直接返回；如果没有记录，会执行首次初始化
      //  eventRelayService.initializeEventRelay();
//
//        // 第二步：调用continueEventRelay方法（已包含幂等性判断）
//        // 如果有中断任务，会恢复执行；如果没有中断任务，直接返回
   //     eventRelayService.continueEventRelay();
       // scheduleEventLoopService.initEventLoop();

    }

}