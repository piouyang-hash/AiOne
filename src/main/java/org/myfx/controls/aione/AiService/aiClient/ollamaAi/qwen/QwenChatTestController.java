package org.myfx.controls.aione.AiService.aiClient.ollamaAi.qwen;

import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.aiClient.openAi.spark.SparkChatBizService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * 千问(Qwen)模型测试控制器
 * 功能：提供简单接口测试阻塞式/流式对话，固定chatId=999，系统提示词简化
 */
@RestController
@RequestMapping("/api/qwen") // 统一接口前缀
@Slf4j
public class QwenChatTestController {

    // 注入千问模型核心客户端
    private final OllamaQwenAiCoreClient qwenAiCoreClient;
    // 新增：注入SparkChatBizService（用于文本总结）
    private final SparkChatBizService sparkChatBizService;

    // 构造器注入（新增SparkChatBizService，保持Spring推荐的注入风格）
    public QwenChatTestController(OllamaQwenAiCoreClient qwenAiCoreClient, SparkChatBizService sparkChatBizService) {
        this.qwenAiCoreClient = qwenAiCoreClient;
        this.sparkChatBizService = sparkChatBizService;
    }

    // ====================== 测试接口1：阻塞式对话（固定chatId=999） ======================
    /**
     * 千问模型阻塞式对话测试（新增消息总结）
     * 示例请求：http://localhost:8080/api/qwen/chat/block?msg=你好
     * @param msg 用户输入的消息（必填）
     * @return AI的完整回复
     */
    @GetMapping("/chat/block")
    public String testQwenChatBlock(@RequestParam String msg) {
        // ====================== 新增：用户消息总结逻辑 ======================
        if (msg != null && !msg.isBlank()) {
            // 1. 调用SparkChatBizService做文本总结（内部自动500字截断）
          //  String summaryResult = sparkChatBizService.summarizeTextReturnString(msg);
            // 2. 打印总结结果（日志+控制台双打印，直观看效果）
           // log.info("千问测试-会话[997]用户消息总结结果：{}", summaryResult);
        //    System.out.printf("【千问测试-会话997】用户消息总结：%s%n", summaryResult);

            // 可选扩展：同时打印主题标签（如需看标签效果可启用）
            String topicTags = sparkChatBizService.generateTopicTagsReturnString(msg);
            log.info("千问测试-会话[997]用户消息主题标签：{}", topicTags.isEmpty() ? "无核心主题" : topicTags);
           System.out.printf("【千问测试-会话997】用户消息主题标签：%s%n", topicTags.isEmpty() ? "无核心主题" : topicTags);
        }

        // ====================== 原有逻辑（保留不变） ======================
        // 固定配置：chatId=997，系统提示词（简洁版）
        String fixedChatId = "997";
        String simpleSystemPrompt = "你是友好的千问AI助手，回答简洁易懂。";

        // 调用核心客户端的阻塞式方法
        return "nihao";
                //qwenAiCoreClient.qwenChatBlock(msg, simpleSystemPrompt, fixedChatId);
    }

    // ====================== 测试接口2：流式对话（固定chatId=999） ======================
    /**
     * 千问模型流式对话测试
     * 示例请求：http://localhost:8080/api/qwen/chat/stream?msg=介绍一下自己
     * @param msg 用户输入的消息（必填）
     * @return 流式返回的AI回复片段（逐字/逐段输出）
     */
    @GetMapping(value = "/chat/stream", produces = "text/event-stream") // 声明流式响应类型
    public Flux<String> testQwenChatStream(@RequestParam String msg) {
        // 固定配置：chatId=999，系统提示词（简洁版）
        String fixedChatId = "999";
        String simpleSystemPrompt = "你是友好的千问AI助手，回答简洁易懂。";

        // 调用核心客户端的流式方法
        return qwenAiCoreClient.qwenChatStream(msg, simpleSystemPrompt, fixedChatId);
    }
}