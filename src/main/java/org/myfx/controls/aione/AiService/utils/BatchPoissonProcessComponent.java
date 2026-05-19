package org.myfx.controls.aione.AiService.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * 泊松过程批量计算组件：从指定初始值递增到100（1~2天完成）
 * 固定规则：步长=1，最小冷却=120秒，终止点=100，λ=0.00087（1~2天达标）
 */
@Slf4j
@Component
public class BatchPoissonProcessComponent {

    // 随机数生成器
    private final Random random = new Random();
    // 泊松速率λ：0.00087次/秒（100次≈32小时，1~2天区间内）
    private final double lambda = 0.00087;
    // 固定配置
    private static final int FIXED_INCREMENT_STEP = 1;        // 固定步长1
    private static final int FIXED_MIN_COOL_DOWN = 120;       // 固定最小冷却120秒
    private static final int FIXED_TERMINATE_VALUE = 100;     // 固定终止点100

    /**
     * 批量计算：指定时间后，从指定初始值开始递增的结果（最终不超过100）
     * 修正方法名：原名称包含FromZero不符合实际逻辑
     * @param initialNumber 初始数值（需≥0且≤100）
     * @param timeSeconds 指定计算的时间长度（秒），比如1天=86400秒，2天=172800秒
     * @return 指定时间后的数值（initialNumber~100之间）
     */
    public int calculateIncrementTo100(int initialNumber, int timeSeconds) {
        // 1. 参数校验（使用Spring Assert替代冗长if，不符合条件直接抛IllegalArgumentException）
        Assert.isTrue(initialNumber <= FIXED_TERMINATE_VALUE,
                () -> "初始值" + initialNumber + "不能超过终止点" + FIXED_TERMINATE_VALUE);
        Assert.isTrue(initialNumber >= 0,
                () -> "初始值" + initialNumber + "不能为负数");
        Assert.isTrue(timeSeconds >= 0,
                () -> "时间参数" + timeSeconds + "秒不能为负数");

        // 时间为0直接返回初始值
        if (timeSeconds == 0) {
            log.info("时间为0秒，返回初始值：{}", initialNumber);
            return initialNumber;
        }
        // 初始值已达100直接返回
        if (initialNumber >= FIXED_TERMINATE_VALUE) {
            log.info("初始值{}≥终止点{}，无需计算，直接返回{}", initialNumber, FIXED_TERMINATE_VALUE, FIXED_TERMINATE_VALUE);
            return FIXED_TERMINATE_VALUE;
        }

        // 2. 计算指定时间内的最大可能递增次数（受最小冷却120秒限制）
        int maxPossibleEvents = FIXED_MIN_COOL_DOWN == 0 ? Integer.MAX_VALUE : timeSeconds / FIXED_MIN_COOL_DOWN;

        // 3. 生成泊松分布的事件数（核心：指定时间内会递增多少次）
        double lambdaT = lambda * timeSeconds; // 泊松分布参数（λ×时间）
        int eventCount = generatePoissonRandomNumber(lambdaT);

        // 4. 修正事件数：不超冷却限制 + 不超终止点（最多只能涨到100）
        // 剩余可递增次数 = 100 - 初始值（步长固定1）
        int maxAllowEvents = FIXED_TERMINATE_VALUE - initialNumber;
        int actualEventCount = Math.min(eventCount, Math.min(maxPossibleEvents, maxAllowEvents));

        // 5. 计算最终值（递增）
        int finalValue = initialNumber + actualEventCount * FIXED_INCREMENT_STEP;
        finalValue = Math.min(finalValue, FIXED_TERMINATE_VALUE); // 兜底：不超过100

        // 打印计算过程（日志适配初始值参数）
        log.info("===== 泊松递增计算结果 =====");
        log.info("初始值={}，计算时间：{}秒（≈{}小时），λ×T={}",
                initialNumber, timeSeconds, String.format("%.1f", timeSeconds / 3600.0), lambdaT);
        log.info("泊松事件数={}，实际有效递增次数={}", eventCount, actualEventCount);
        log.info("{}秒后数值：{} → {}", timeSeconds, initialNumber, finalValue);

        return finalValue;
    }

    /**
     * 新增方法：计算从指定初始值递增到100所需的时间（秒数）
     * 核心逻辑：泊松过程的间隔时间为指数分布，每次递增需满足最小冷却时间
     * @param initialNumber 初始数值（需≥0且≤100）
     * @return 从初始值到100所需的总时间（秒）
     */
    public long calculateTimeToReach100(int initialNumber) {
        // 1. 参数校验
        Assert.isTrue(initialNumber >= 0 && initialNumber <= FIXED_TERMINATE_VALUE,
                () -> "初始值必须在0~100之间，当前值：" + initialNumber);

        // 初始值已达100，返回0秒
        if (initialNumber >= FIXED_TERMINATE_VALUE) {
            log.info("初始值{}已达到终止点100，所需时间：0秒", initialNumber);
            return 0L;
        }

        // 2. 计算需要递增的步数
        int remainingSteps = FIXED_TERMINATE_VALUE - initialNumber;
        log.info("从{}递增到100需要{}步（步长=1）", initialNumber, remainingSteps);

        // 3. 累加每一步的时间（指数分布间隔 + 最小冷却，取最大值）
        long totalSeconds = 0L;
        for (int step = 1; step <= remainingSteps; step++) {
            // 生成指数分布的间隔时间（泊松过程的事件间隔服从指数分布）
            double exponentialInterval = -Math.log(1 - random.nextDouble()) / lambda;
            // 实际间隔时间 = 指数分布时间 和 最小冷却时间 取最大值
            double actualInterval = Math.max(exponentialInterval, FIXED_MIN_COOL_DOWN);
            totalSeconds += Math.round(actualInterval);

       }

        return totalSeconds;
    }

    /**
     * Knuth算法生成泊松分布随机数（核心不变）
     */
    private int generatePoissonRandomNumber(double lambdaT) {
        if (lambdaT <= 0) {
            return 0;
        }

        int k = 0;
        double p = 1.0;
        double expLambdaT = Math.exp(-lambdaT);

        do {
            k++;
            p *= random.nextDouble();
        } while (p > expLambdaT);

        return k - 1;
    }

}