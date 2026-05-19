package org.myfx.controls.aione.AiService.Demo.FunctionCallDemo;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 今日日期工具
 * 专门供AI调用，返回当前系统的日期
 */
@Component("dateTool")  // bean 名称保持 dateTool，方便 .defaultFunctions("dateTool")
public class DateTool {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Tool(
            name = "get_today_date",
            description = "获取当前系统的日期，返回格式为 yyyy-MM-dd。该工具不需要任何输入参数。"
    )
    public String getTodayDate(ToolContext toolContext) {
        LocalDate today = LocalDate.now();
        String dateStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // 1. 直接从 ThreadLocal 获取（原有逻辑）
        Integer threadLocalValue = ThreadLocalTestHolder.get();

        // 2. 从 ToolContext 获取预存的值（假设存入时使用 key="originalThreadLocalValue"）
        Integer contextValue = null;
        if (toolContext != null && toolContext.getContext().containsKey("originalThreadLocalValue")) {
            contextValue = (Integer) toolContext.getContext().get("originalThreadLocalValue");
        }

        // 打印对比
        System.out.println("【ThreadLocal值】: " + threadLocalValue);
        System.out.println("【ToolContext值】: " + contextValue);

        // 返回信息中包含两者，方便接口查看
        return String.format(
                "今日日期：%s | ThreadLocal值：%s | ToolContext值：%s",
                dateStr,
                threadLocalValue == null ? "null" : threadLocalValue,
                contextValue == null ? "null" : contextValue
        );
    }
}