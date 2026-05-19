package org.myfx.controls.aione.AiService.engineering.summary_sliding_window;

/**
 * 总结型滑动窗口Spring AI提示词构建工具类
 */
public class SummarySlidingSpringAiPromptBuildUtil {

    /**
     * 计算总结型滑动窗口的查询limitNum
     * @param totalMessageNum 总消息条数（从countChatMessagesBySessionIdAndUserId获取）
     * @param summaryMessageNum 总结的消息数（总结轮数×2）
     * @param maxWindowLimit 窗口最大限制数（窗口最大轮数×2）
     * @return 最终查询的limitNum
     * @throws IllegalArgumentException 参数异常或计算逻辑异常时抛出
     */
    public static Integer calculateSlidingWindowLimitNum(Integer totalMessageNum,
                                                         Integer summaryMessageNum,
                                                         Integer maxWindowLimit) {
        // 1. 基础参数校验
        if (totalMessageNum == null || summaryMessageNum == null || maxWindowLimit == null) {
            throw new IllegalArgumentException("计算limitNum的参数不能为空");
        }
        if (summaryMessageNum == 0) {
            throw new IllegalArgumentException("总结的消息数不能为0");
        }
        if (maxWindowLimit < 0) {
            throw new IllegalArgumentException("窗口最大限制数不能为负数");
        }
        if (totalMessageNum < 0) {
            throw new IllegalArgumentException("总消息条数不能为负数");
        }

        // 2. 初始化当前计算值为总消息数
        int currentNum = totalMessageNum;

        // 3. 循环减法：直到currentNum落在[0, maxWindowLimit]范围内
        while (true) {
            // 终止条件：当前值在0到窗口最大限制数之间（包含两头）
            if (currentNum <= maxWindowLimit) {
                return currentNum;
            }
            // 若当前值超出上限，减去总结的消息数
            currentNum -= summaryMessageNum;
            // 极端情况：若减到负数（理论上不会发生，做兜底返回0）
            if (currentNum < 0) {
                return 0;
            }
        }
    }

    // ========== 计算减法执行次数 ==========
    /**
     * 计算总结型滑动窗口减法执行的次数
     * @param totalMessageNum 总消息条数
     * @param summaryMessageNum 总结的消息数（总结轮数×2）
     * @param maxWindowLimit 窗口最大限制数（窗口最大轮数×2）
     * @return 减法执行的次数
     * @throws IllegalArgumentException 参数异常时抛出
     */
    public static int calculateSlidingWindowSubtractCount(Integer totalMessageNum,
                                                          Integer summaryMessageNum,
                                                          Integer maxWindowLimit) {
        // 复用基础参数校验逻辑
        if (totalMessageNum == null || summaryMessageNum == null || maxWindowLimit == null) {
            throw new IllegalArgumentException("计算减法次数的参数不能为空");
        }
        if (summaryMessageNum == 0) {
            throw new IllegalArgumentException("总结的消息数不能为0");
        }
        if (maxWindowLimit < 0) {
            throw new IllegalArgumentException("窗口最大限制数不能为负数");
        }
        if (totalMessageNum < 0) {
            throw new IllegalArgumentException("总消息条数不能为负数");
        }

        int currentNum = totalMessageNum;
        int subtractCount = 0; // 初始化减法次数为0

        while (true) {
            // 终止条件：当前值≤窗口最大限制，直接返回次数（未执行减法）
            if (currentNum <= maxWindowLimit) {
                return subtractCount;
            }
            // 执行减法，次数+1
            currentNum -= summaryMessageNum;
            subtractCount++;
            // 减到负数，返回当前次数（最后一次减法已执行）
            if (currentNum < 0) {
                return subtractCount;
            }
        }
    }

}
