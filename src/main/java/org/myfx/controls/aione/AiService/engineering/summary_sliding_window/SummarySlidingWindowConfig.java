package org.myfx.controls.aione.AiService.engineering.summary_sliding_window;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 总结型滑动窗口配置类
 * 配置前缀：chat.sliding.window.summary
 */
@Setter
@Getter
@Component
// 去掉 @ConfigurationProperties 注解
public class SummarySlidingWindowConfig {

    /**
     * 窗口对话最大轮数
     */
    @Value("${sliding.window.summary.window-max-round:10}") // 冒号后是默认值，防止配置缺失
    private Integer windowMaxRound;

    /**
     * 总结的对话轮数
     */
    @Value("${sliding.window.summary.summary-round:5}") // 默认值5，避免null
    private Integer summaryRound;

}