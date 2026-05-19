package org.myfx.controls.aione.AiService.aiClient.openAi.spark;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 控制器：仅暴露HTTP接口，调用业务层方法
 * 职责：接收请求参数，转发到业务层，返回响应结果
 */
@RestController
public class SparkChatController {

    private final SparkChatBizService sparkChatBizService;

    // 注入业务工具类
    public SparkChatController(SparkChatBizService sparkChatBizService) {
        this.sparkChatBizService = sparkChatBizService;
    }

    // ========== 1. 普通聊天 - 阻塞调用 ==========
    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        return sparkChatBizService.chatWithSparkReturnString(message);
    }

    // ========== 2. 普通聊天 - 流式调用（指定event-stream媒体类型） ==========
    @GetMapping(
            value = "/stream/chat",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=utf-8"
    )
    public Flux<String> streamChat(@RequestParam String message) {
        return sparkChatBizService.chatWithSpark(message);
    }

    // ========== 3. 文本总结 - 流式调用 ==========
    @GetMapping(
            value = "/stream/summarize",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=utf-8"
    )
    public Flux<String> streamSummarize(@RequestParam String text) {
        return sparkChatBizService.summarizeText(text);
    }

    // ========== 4. 文本总结 - 阻塞调用 ==========
    @GetMapping("/summarize")
    public String summarize(@RequestParam String text) {
        return sparkChatBizService.summarizeTextReturnString(text);
    }

    // ========== 5. 文本翻译 - 流式调用 ==========
    @GetMapping(
            value = "/stream/translate",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=utf-8"
    )
    public Flux<String> streamTranslate(@RequestParam String text) {
        return sparkChatBizService.translateText(text);
    }

    // ========== 6. 文本翻译 - 阻塞调用 ==========
    @GetMapping("/translate")
    public String translate(@RequestParam String text) {
        return sparkChatBizService.translateTextReturnString(text);
    }

    // ========== 7. 代码解释 - 流式调用 ==========
    @GetMapping(
            value = "/stream/explain-code",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=utf-8"
    )
    public Flux<String> streamExplainCode(@RequestParam String code) {
        return sparkChatBizService.explainCode(code);
    }

    // ========== 8. 代码解释 - 阻塞调用 ==========
    @GetMapping("/explain-code")
    public String explainCode(@RequestParam String code) {
        return sparkChatBizService.explainCodeReturnString(code);
    }
}