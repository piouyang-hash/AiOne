//package org.myfx.controls.aione.AiService.job;
//
//import com.xxl.job.core.context.XxlJobHelper;
//import com.xxl.job.core.handler.annotation.XxlJob;
//import lombok.extern.slf4j.Slf4j;
//import org.myfx.controls.aione.AiService.service.schedule.AiActiveChatAggregateService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//import java.time.LocalTime;
//import java.util.Calendar;
//import java.util.List;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//
//@Component
//@Slf4j
//public class AiTimedSendHandler {
//
//    // 线程池，用于处理一分钟内的随机任务
//    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
//    // 注入AI主动消息总业务接口（核心：调用完整的主动消息流程）
//    @Autowired
//    private AiActiveChatAggregateService aiActiveChatAggregateService;
//
//    @XxlJob("randomFiveTimesJobHandler")
//    public void randomFiveTimesJobHandler() {
//        XxlJobHelper.log("开始计算本分钟内的随机执行计划...");
//
//        // 1. 生成当前分钟内的 5 个随机秒数 (0-59秒)
//        List<Integer> seconds = generateRandomSecondsInMinute(5, 0, 59);
//        XxlJobHelper.log("随机秒数已生成: " + seconds);
//
//        // 2. 获取当前分钟的起点
//        long now = System.currentTimeMillis();
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.SECOND, 0);
//        calendar.set(Calendar.MILLISECOND, 0);
//        long startOfMinute = calendar.getTimeInMillis();
//
//        // 3. 循环调度这 5 个任务
//        for (Integer sec : seconds) {
//            long targetTime = startOfMinute + (sec * 1000L);
//            long delay = targetTime - now;
//
//            if (delay > 0) {
//                // 提交到调度线程池，在指定秒数执行
//                scheduler.schedule(() -> {
//                    runActualBusinessLogic(sec);
//                }, delay, TimeUnit.MILLISECONDS);
//            } else {
//                // 如果随机秒数已经过去了（比如随机到了第1秒，但任务第2秒才启动），立即执行
//                runActualBusinessLogic(sec);
//            }
//        }
//    }
//
//    // 真正的业务逻辑
//    private void runActualBusinessLogic(int second) {
//        System.out.println("随机任务执行！触发秒数：" + second + "，当前时间：" + LocalDateTime.now());
//        // 如果需要，可以在这里调用 XxlJobHelper.log，但注意异步线程中日志归属问题
//    }
//
//    /**
//     * 闹钟报时+AI主动消息推送任务Handler（XXL-Job配置的任务名）
//     * Cron表达式配置：0 0 10,22 * * ?（每天10:00、22:00执行）
//     */
//    @XxlJob("aiAlarmClockJobHandler")
//    public void aiAlarmClockJobHandler() {
//        XxlJobHelper.log("开始执行AI闹钟报时+主动消息推送任务...");
//        // 固定测试用户ID=1
//        Integer testUserId = 1;
//        XxlJobHelper.log("本次测试目标用户ID：{}", testUserId);
//
//        // 1. 获取当前小时，判断执行时段
//        LocalTime currentTime = LocalTime.now();
//        int currentHour = currentTime.getHour();
//        XxlJobHelper.log("当前执行时间：{}，小时数：{}", LocalDateTime.now(), currentHour);
//
//        // 2. 根据小时执行「提醒+AI主动消息」逻辑
//        try {
//            if (currentHour == 10) {
//                executeMorningAlarmAndActiveMessage(testUserId); // 早上10点：起床提醒+AI主动消息
//            } else if (currentHour == 22) {
//                executeNightAlarmAndActiveMessage(testUserId);   // 晚上10点：睡觉提醒+AI主动消息
//            } else {
//                // 兜底：Cron配置仅触发10/22点，防止异常
//                XxlJobHelper.log("非预期执行时间（仅10点/22点触发），本次无提醒");
//                XxlJobHelper.handleSuccess("AI闹钟任务执行完成：非预期时间，无提醒");
//                return;
//            }
//            // 3. 任务执行成功反馈
//            XxlJobHelper.handleSuccess("AI闹钟报时+主动消息推送任务执行完成（用户ID=1）");
//        } catch (Exception e) {
//            // 4. 异常处理（XXL-Job标记任务失败，打印异常日志）
//            String errorMsg = String.format("AI闹钟任务执行失败（用户ID=1）：%s", e.getMessage());
//            XxlJobHelper.log(errorMsg);
//            log.error("[AI闹钟任务] 执行失败", e);
//            XxlJobHelper.handleFail(errorMsg);
//        }
//    }
//
//    /**
//     * 早上10点：起床提醒 + 调用AI主动消息总业务接口
//     * @param userId 固定测试用户ID=1
//     */
//    private void executeMorningAlarmAndActiveMessage(Integer userId) {
//        // 1. 基础起床提醒文案
//        String baseReminder = "☀️ 早上好呀～已经10点啦，该起床活动一下啦，记得吃早餐哦～";
//        XxlJobHelper.log(baseReminder);
//        log.info("[AI闹钟] {}", baseReminder);
//
//        // 2. 调用总业务接口，执行AI主动消息完整流程（userId=1）
//        XxlJobHelper.log("开始调用AI主动消息总业务接口（用户ID=1，早上10点场景）...");
//        String aiReply = aiActiveChatAggregateService.executeAiActiveMessageDispatch(userId);
//
//        // 3. 打印AI主动回复（核心测试结果）
//        String activeMsgLog = String.format("【AI主动消息-早间】用户ID=%s，AI回复：%s", userId, aiReply);
//        XxlJobHelper.log(activeMsgLog);
//        log.info("[AI闹钟-主动消息] {}", activeMsgLog);
//    }
//
//    /**
//     * 晚上10点：睡觉提醒 + 调用AI主动消息总业务接口
//     * @param userId 固定测试用户ID=1
//     */
//    private void executeNightAlarmAndActiveMessage(Integer userId) {
//        // 1. 基础睡觉提醒文案
//        String baseReminder = "🌙 晚上好呀～已经22点啦，该准备休息咯，早睡早起身体好～";
//        XxlJobHelper.log(baseReminder);
//        log.info("[AI闹钟] {}", baseReminder);
//
//        // 2. 调用总业务接口，执行AI主动消息完整流程（userId=1）
//        XxlJobHelper.log("开始调用AI主动消息总业务接口（用户ID=1，晚上22点场景）...");
//        String aiReply = aiActiveChatAggregateService.executeAiActiveMessageDispatch(userId);
//
//        // 3. 打印AI主动回复（核心测试结果）
//        String activeMsgLog = String.format("【AI主动消息-晚间】用户ID=%s，AI回复：%s", userId, aiReply);
//        XxlJobHelper.log(activeMsgLog);
//        log.info("[AI闹钟-主动消息] {}", activeMsgLog);
//    }
//}
