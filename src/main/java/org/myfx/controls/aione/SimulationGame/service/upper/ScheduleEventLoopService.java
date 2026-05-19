package org.myfx.controls.aione.SimulationGame.service.upper;

/**
 * 定时事件循环服务
 * 基于SpringBoot定时任务，驱动事件自动接力、周循环执行
 */
public interface ScheduleEventLoopService {

    /**
     * 初始化事件循环
     * 系统启动时执行：
     * 1. 查询是否存在执行中的事件，有则直接跳过
     * 2. 无则创建周一第一个事件，启动循环
     * 幂等安全，可重复调用
     */
    void initEventLoop();

    /**
     * 执行事件循环（核心接力方法）
     * 定时任务每分钟调用：
     * 1. 判断当前事件是否到期
     * 2. 到期则结束当前事件，创建下一个事件
     * 3. 支持周日→周一自动周循环
     */
    void executeEventLoop();

}
