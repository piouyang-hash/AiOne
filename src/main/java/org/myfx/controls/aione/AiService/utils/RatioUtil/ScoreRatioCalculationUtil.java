package org.myfx.controls.aione.AiService.utils.RatioUtil;

import org.myfx.controls.aione.AiService.common.ScoreRatioLevelEnum;

import java.util.Objects;

/**
 * 分数比例&区间等级计算工具类
 * 核心功能：1. 计算变动分数的绝对值比例；2. 判断分数所属区间等级
 */
public final class ScoreRatioCalculationUtil {

    // 私有构造器：禁止实例化工具类
    private ScoreRatioCalculationUtil() {
        throw new UnsupportedOperationException("工具类禁止实例化");
    }

    /**
     * 计算变动分数值的绝对值比例（范围-100~100 → 0~100）
     * @param changeScore 变动的分数值（必填，范围[-100, 100]，包含两端）
     * @return 变动分数的绝对值（Integer类型，范围0~100）
     * @throws NullPointerException 参数为null时抛出
     * @throws IllegalArgumentException 参数超出[-100, 100]范围时抛出
     */
    public static Integer calculateAbsoluteRatio(Integer changeScore) {
        // 1. 非空校验
        Objects.requireNonNull(changeScore, "变动的分数值不能为空");
        // 2. 范围校验
        if (changeScore < -100 || changeScore > 100) {
            throw new IllegalArgumentException("变动的分数值必须在-100到100之间（包含两端），当前值：" + changeScore);
        }
        // 3. 转绝对值返回（无需小数，直接返回Integer）
        return Math.abs(changeScore);
    }

    /**
     * 判断分数所属区间等级（基于0~100的绝对值分数）
     * @param score 要判断的分数（必填，范围[0, 100]，建议传入calculateAbsoluteRatio的返回值）
     * @return 等级枚举：0~30→ScoreRatioLevelEnum.LOW，31~70→ScoreRatioLevelEnum.MEDIUM，71~100→ScoreRatioLevelEnum.HIGH
     * @throws NullPointerException 参数为null时抛出
     * @throws IllegalArgumentException 参数超出[0, 100]范围时抛出
     */
    public static ScoreRatioLevelEnum judgeScoreLevel(Integer score) {
        // 1. 非空校验（保留原有逻辑）
        Objects.requireNonNull(score, "要判断的分数不能为空");
        // 2. 范围校验（保留原有逻辑，仅接受0~100的绝对值分数）
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("要判断的分数必须在0到100之间（包含两端），当前值：" + score);
        }
        // 3. 区间判断返回对应枚举（核心修改：从字符串改为枚举）
        if (score <= 30) {
            return ScoreRatioLevelEnum.LOW;
        } else if (score <= 70) {
            return ScoreRatioLevelEnum.MEDIUM;
        } else { // 71~100
            return ScoreRatioLevelEnum.HIGH;
        }
    }

    /**
     * 简化整合方法：直接传入变动分数（-100~100），一步返回等级枚举
     * 核心逻辑：自动计算绝对值 → 判断区间等级 → 返回枚举，无需分步调用
     * @param changeScore 变动的分数值（必填，范围[-100, 100]，包含两端）
     * @return 最终的分数等级枚举
     * @throws NullPointerException 参数为null时抛出（由calculateAbsoluteRatio透传）
     * @throws IllegalArgumentException 参数超出[-100, 100]范围时抛出（由calculateAbsoluteRatio透传）
     */
    public static ScoreRatioLevelEnum calculateScoreRatioLevel(Integer changeScore) {
        // 1. 先计算绝对值比例
        Integer absoluteScore = calculateAbsoluteRatio(changeScore);
        // 2. 再判断等级并返回枚举（一步到位）
        return judgeScoreLevel(absoluteScore);
    }

}