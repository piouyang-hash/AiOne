package org.myfx.controls.aione.AiService.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import java.time.Duration;

@RestController
public class StreamController {

    /**
     * 流式输出：每秒返回一个数字（MediaType.TEXT_EVENT_STREAM表示SSE流）
     */
    @GetMapping(value = "/stream/numbers", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Integer> streamNumbers() {
        // 生成1到10的序列，每秒发射一个元素
        return Flux.range(1, 10)
                .delayElements(Duration.ofSeconds(1)) // 每秒发一个
                .doOnNext(num -> System.out.println("发射数据：" + num)); // 日志
    }
}
