package org.myfx.controls.aione.AiService.engineering.sliding_window;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 滑动窗口配置类
 * 支持Nacos/Apollo等配置中心动态刷新（@RefreshScope）
 * 配置前缀：chat.sliding.window
 */
@Setter
@Getter
@Component
public class SlidingWindowConfig {

    /**
     * 滑动窗口最大轮数（一轮对话 = 用户消息 + AI回复，对应2条记录）
     */
    @Value("${sliding.window.max-round:10}") // 冒号后为默认值，配置缺失时使用10轮
    private Integer windowMaxRound;

}