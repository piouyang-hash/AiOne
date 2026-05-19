package org.myfx.controls.aione.AiService.Demo.FunctionCallDemo;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.myfx.controls.aione.AiService.aiClient.AMTest.AiModelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 阻塞式函数调用对话控制器
 * 核心功能：接收用户提问 → 调用FunctionGemma → 判断是否调用日期工具 → 返回结果
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/function-call") // 函数调用相关路径
public class FunctionCallController {

    // 注入FunctionGemma的ChatClient（按名称注入）
    @Resource(name = "functionGemmaClient")
    private AiModelClient chatClient;

    // 注入今日日期工具
    @Autowired
    private DateTool dateTool;

    /**
     * 阻塞式函数调用接口
     * @return 响应结果（包含answer：最终回复）
     */
    @PostMapping("/chat/{userMessage}")
    public String chatByPathParam(@PathVariable String userMessage) {
        // 1. 参数校验
        if (StringUtils.isBlank(userMessage)) {
            return "错误：用户提问不能为空";
        }

        // 2. 日志记录
        log.info("用户提问（路径参数）：{}", userMessage);

        // 4. 调用AI并自动处理工具调用，获取最终回复（String）
        // 传入日期工具
        // 阻塞式处理工具调用循环
        // 直接获取字符串回复

        // 5. 直接返回String类型的最终回答
        return chatClient.getChatClient()
                .prompt()
                .user(userMessage)
                .call()               // 阻塞式处理工具调用循环
                .content();
    }

}