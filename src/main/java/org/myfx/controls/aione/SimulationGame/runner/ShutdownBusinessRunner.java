package org.myfx.controls.aione.SimulationGame.runner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.SimulationGame.service.upper.EventRelayService;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;

/**
 * 模拟游戏-应用关闭时执行的Runner
 * 仿照StartupBusinessRunner风格，在应用优雅关闭时执行清理/打印逻辑
 */
@Component // 交给Spring容器管理，确保销毁逻辑触发
@Slf4j
@RequiredArgsConstructor
public class ShutdownBusinessRunner {


    private final EventRelayService eventRelayService;

    /**
     * 应用优雅关闭时执行（Spring Bean销毁阶段触发）
     * 注：仅在应用优雅关闭时生效（Ctrl+C、kill -15、IDE停止按钮），kill -9强制关闭不会触发
     */
    @PreDestroy
    public void onApplicationShutdown() {
        try {
            // 调用中断事件接力棒方法（已包含幂等性判断）
            // 如果有执行中任务，会中断并保存状态；如果没有执行中任务，直接返回
           // eventRelayService.interruptEventRelay();

            log.info("===== 模拟游戏应用关闭前逻辑执行完成 =====");
        } catch (Exception e) {
            // 捕获异常，避免关闭逻辑报错导致应用关闭失败
            log.error("模拟游戏应用关闭前执行逻辑出错", e);
        }
    }

}