package org.myfx.controls.aione.AiService.utils;

import org.springframework.stereotype.Component;

/**
 * 指数退避算法工具类
 * 用于支付轮询、状态查询等重试/轮询策略计算
 * 常量直接修改final值即可调整策略
 */
@Component
public class ExponentialBackoffUtil {

    // ======================== 【可调整常量】final 静态常量 ========================
    /** 初始延迟时间（毫秒）- 第一次轮询等待时间 */
    private static final long INIT_DELAY_MILLIS = 1000L;

    /** 最大延迟时间（毫秒）- 防止等待时间过长 */
    private static final long MAX_DELAY_MILLIS = 10000L;

    /** 指数倍增因子 - 每次延迟 = 上次 * 因子 */
    private static final double MULTIPLIER = 2.0;

    /** 最大轮询/重试次数 - 超过则终止轮询 */
    private static final int MAX_POLL_COUNT = 15;

    // ======================== 核心算法方法 ========================

    /**
     * 根据当前轮询次数，计算下一次的延迟时间（指数退避）
     * @param pollCount 当前已轮询次数（从 0 开始计数）
     * @return 最终延迟毫秒数（不会超过最大延迟）
     */
    public long calculateBackoffDelay(int pollCount) {
        // 非法次数直接返回初始值
        if (pollCount < 0) {
            return INIT_DELAY_MILLIS;
        }

        // 指数退避核心公式：延迟 = 初始值 * 因子^轮询次数
        long delay = (long) (INIT_DELAY_MILLIS * Math.pow(MULTIPLIER, pollCount));

        // 限制最大值，避免等待过久
        return Math.min(delay, MAX_DELAY_MILLIS);
    }

    /**
     * 判断是否达到最大轮询次数（达到则停止轮询）
     * @param pollCount 当前已轮询次数
     * @return true=达到上限，false=可继续轮询
     */
    public boolean isMaxPollCount(int pollCount) {
        return pollCount >= MAX_POLL_COUNT;
    }

    // ======================== 对外暴露常量（可选） ========================
    public int getMaxPollCount() {
        return MAX_POLL_COUNT;
    }

    public long getInitDelayMillis() {
        return INIT_DELAY_MILLIS;
    }
}