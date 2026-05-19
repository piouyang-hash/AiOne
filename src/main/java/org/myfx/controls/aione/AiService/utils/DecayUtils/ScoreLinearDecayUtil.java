package org.myfx.controls.aione.AiService.utils.DecayUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 分数线性衰减工具类（每5分钟衰减1分）
 * 核心规则：线性衰减，每固定周期衰减固定步长，分数最小为0（不出现负分）
 */
@Slf4j
public final class ScoreLinearDecayUtil {

    /** 衰减周期：5分钟（毫秒），final变量可直接修改 */
    public static final long DECAY_CYCLE = 5 * 60 * 1000L; // 300000毫秒

    /** 每周期衰减步长：1分（可根据需求调整） */
    public static final int DECAY_STEP = 1;

    /**
     * 方法1：计算分数衰减至0所需的总时间（毫秒）
     * @param initialScore 初始分数（≥0，负数按0处理）
     * @return 分数衰减到0的总时间（毫秒）；分数≤0时返回0
     */
    public static Long calculateZeroDecayTotalTime(Integer initialScore) {
        // 1. 分数参数处理（负数/空值按0处理）
        int score = initialScore == null ? 0 : Math.max(initialScore, 0);

        // 2. 分数为0 → 无需衰减，返回0毫秒
        if (score == 0) {
            log.info("初始分数为0，衰减至0所需总时间：0毫秒");
            return 0L;
        }

        // 3. 线性衰减计算：总衰减时间 = 分数 * 衰减周期（仅和分数相关，与时间戳无关）
        long totalDecayTime = (long) score * DECAY_CYCLE;

        log.info("初始分数：{}，衰减至0所需总时间：{}毫秒（{}分钟）",
                score, totalDecayTime, totalDecayTime / 60000.0);
        return totalDecayTime;
    }

    /**
     * 方法2：计算当前时间的衰减后分数
     * @param initialScore 初始分数（≥0，负数按0处理）
     * @param initialTimestamp 初始时间戳（毫秒级，需为有效时间戳）
     * @return 当前衰减后的分数（最小为0）
     * @throws IllegalArgumentException 初始时间戳无效时抛出
     */
    public static Integer calculateCurrentScore(Integer initialScore, Long initialTimestamp) {
        // 1. 参数校验
        if (initialTimestamp == null || initialTimestamp <= 0) {
            throw new IllegalArgumentException("初始时间戳无效！需传入毫秒级有效时间戳，当前值：" + initialTimestamp);
        }
        int score = initialScore == null ? 0 : Math.max(initialScore, 0); // 分数≤0按0处理

        // 2. 分数为0 → 直接返回0
        if (score == 0) {
            log.info("初始分数为0，当前衰减后分数仍为0");
            return 0;
        }

        // 3. 计算从初始时间到当前时间的间隔（毫秒）
        long currentTime = System.currentTimeMillis();
        long timeInterval = currentTime - initialTimestamp;

        // 4. 间隔≤0 → 未到衰减时间，返回初始分数
        if (timeInterval <= 0) {
            log.info("当前时间({})未超过初始时间({})，分数无衰减，返回初始分数：{}",
                    currentTime, initialTimestamp, score);
            return score;
        }

        // 5. 计算衰减次数：间隔 ÷ 衰减周期（向下取整，不足一个周期不衰减）
        long decayTimes = timeInterval / DECAY_CYCLE;
        // 总衰减分数 = 衰减次数 * 每周期衰减步长
        int totalDecayScore = (int) (decayTimes * DECAY_STEP);
        // 当前分数 = 初始分数 - 总衰减分数（最小为0）
        int currentScore = Math.max(score - totalDecayScore, 0);

        log.info("初始分数：{}，初始时间戳：{}，当前时间：{}，衰减次数：{}，总衰减分数：{}，当前分数：{}",
                score, initialTimestamp, currentTime, decayTimes, totalDecayScore, currentScore);
        return currentScore;
    }
}