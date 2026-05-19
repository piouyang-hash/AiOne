package org.myfx.controls.aione.AiService.Demo;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.myfx.controls.aione.AiService.utils.PromptTemplateReader;
import org.myfx.controls.aione.ConnectService.utils.ChannelManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@RestController
public class FixedPromptController {

    private final MustacheFactory mustacheFactory = new DefaultMustacheFactory();

    // ====================== 新增：注入长连接管理器 ======================
    @Autowired
    private ChannelManager channelManager;

    @Autowired
    private PromptTemplateReader promptTemplateReader;

    @GetMapping("/test-prompt-fixed")
    public String testPromptFixed(
            @RequestParam String role,
            @RequestParam boolean hasWeapon) {

        // 获取模板字符串
        String template = promptTemplateReader.readTemplateFile();
        // =================================================================

        // 原有Mustache渲染逻辑（完全不变）
        Mustache mustache = mustacheFactory.compile(new StringReader(template), "prompt");
        Map<String, Object> data = new HashMap<>();
        data.put("role", role);
        data.put("hasWeapon", hasWeapon);
        StringWriter writer = new StringWriter();
        mustache.execute(writer, data);
        String renderedPrompt = writer.toString().trim();

        return "========== 渲染结果 ==========\n" +
                renderedPrompt +
                "\n\n========== 调试信息 ==========\n" +
                "传入参数: role=" + role + ", hasWeapon=" + hasWeapon;
    }

    // ====================== 修复后的无参GET接口（AI消息推送） ======================
    /**
     * 无参数接口，快速测试：主动给【用户ID=2】发送 AI 推送消息
     * 访问地址：<a href="http://localhost:8086/test-push-user2">...</a>
     */
//    @GetMapping("/test-push-user2")
//    public String testPushToUser2() {
//        Channel user2Channel = channelManager.getChannel(2);
//
//        if (user2Channel != null && user2Channel.isActive()) {
//            // 🔥 只需要这一行！无JSON、无转换、无封装
//            user2Channel.writeAndFlush(WebSocketMessage.aiPush("🤖 AI测试消息，极简发送成功！"));
//
//            return "✅ AI推送成功！极简模式生效";
//        }
//        return "❌ 用户2不在线";
//    }
}