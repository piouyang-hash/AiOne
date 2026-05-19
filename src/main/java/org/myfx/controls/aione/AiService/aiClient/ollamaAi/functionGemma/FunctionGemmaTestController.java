package org.myfx.controls.aione.AiService.aiClient.ollamaAi.functionGemma;

import jakarta.annotation.Resource;
import org.myfx.controls.aione.AiService.Demo.FunctionCallDemo.ThreadLocalTestHolder;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * FunctionGemma模型测试控制器
 * 简化版：仅msg必传，systemPrompt默认"你是工具助手"，chatId默认"222"
 */
@RestController
@RequestMapping("/api/ai/function-gemma")
public class FunctionGemmaTestController {

    @Resource
    private OllamaFunctionGemmaAiCoreClient functionGemmaAiCoreClient;

    // ====================== 测试接口1：阻塞式对话（GET） ======================
    /**
     * FunctionGemma模型阻塞式对话测试接口（简化版）
     * @param msg 必传：用户输入的消息
     * @param systemPrompt 可选：系统提示词，默认值为"你是工具助手"
     * @param chatId 可选：会话唯一标识，默认值为"222"
     * @return AI完整响应内容
     */
    @GetMapping("/chat-block")
    public String testChatBlock(
            @RequestParam(required = true) String msg,
            @RequestParam(required = false, defaultValue = "你是工具助手") String systemPrompt,
            @RequestParam(required = false, defaultValue = "222") String chatId
    ) {
        try {
            // ========== 核心操作：设置 ThreadLocal 值为 10 ==========
            ThreadLocalTestHolder.set(10);

            // 调用核心客户端（模拟）
            return functionGemmaAiCoreClient.functionGemmaChatBlock(msg, systemPrompt, chatId);
        } catch (IllegalArgumentException e) {
            return "❌ 调用失败：" + e.getMessage();
        } catch (Exception e) {
            return "❌ 调用失败：未知异常 - " + e.getMessage();
        } finally {
            // 【可选】如果接口执行完不需要保留值，建议finally中删除（避免内存泄漏）
            // ThreadLocalTestHolder.delete();
        }
    }

    // ====================== 测试接口2：流式对话（GET） ======================
    /**
     * FunctionGemma模型流式对话测试接口（简化版）
     * 注意：返回SSE流式响应，需用Postman/浏览器开发者工具测试
     * @param msg 必传：用户输入的消息
     * @param systemPrompt 可选：系统提示词，默认值为"你是工具助手"
     * @param chatId 可选：会话唯一标识，默认值为"222"
     * @return 流式响应的AI内容片段
     */
    @GetMapping(value = "/chat-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> testChatStream(
            @RequestParam(required = true) String msg,
            @RequestParam(required = false, defaultValue = "你是工具助手") String systemPrompt,
            @RequestParam(required = false, defaultValue = "222") String chatId
    ) {
        try {
            // 调用核心客户端，使用默认/传入的参数
            return functionGemmaAiCoreClient.functionGemmaChatStream(msg, systemPrompt, chatId);
        } catch (IllegalArgumentException e) {
            return Flux.just("❌ 调用失败：" + e.getMessage());
        } catch (Exception e) {
            return Flux.just("❌ 调用失败：未知异常 - " + e.getMessage());
        }
    }
}