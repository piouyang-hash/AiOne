package org.myfx.controls.aione.SimulationGame.service.upper;

/**
 * 事件接力棒服务接口
 * Event Relay Service Interface
 */
public interface EventRelayService {

    /**
     * 初始化事件接力棒
     * 系统启动时初始化事件执行状态，如果已有记录则直接返回（幂等性）
     * 无记录时创建首个事件的执行记录
     */
    void initializeEventRelay();

    /**
     * 中断事件接力棒
     * 中断当前执行中的事件，计算结束时间并保存为中断状态
     * 如果没有执行中任务则直接返回（幂等性）
     */
    void interruptEventRelay();

    /**
     * 传递事件接力棒
     * 将当前事件状态更新为已完成，并创建并启动下一个事件
     *
     * @param locationCode 地点编码
     * @param eventCode 事件编码
     * @param actualStart 当前事件的实际开始时间
     * @return 传递是否成功
     * @throws IllegalArgumentException 参数无效时抛出
     * @throws RuntimeException 传递过程中发生异常时抛出
     */
    boolean transferEventRelay(String locationCode, String eventCode, Integer actualStart);

    /**
     * 延续事件接力棒
     * 恢复被中断的事件接力流程，重新计算剩余时间并继续执行
     * 如果没有中断任务则直接返回（幂等性）
     */
    void continueEventRelay();

}