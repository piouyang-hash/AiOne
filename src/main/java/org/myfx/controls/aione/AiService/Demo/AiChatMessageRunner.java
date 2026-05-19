package org.myfx.controls.aione.AiService.Demo;

import jakarta.annotation.Resource;
import org.myfx.controls.aione.AiService.aiClient.DeepSeekTokenizer;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiChatMessageService;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiRoleService;
import org.myfx.controls.aione.AiService.service.base.status.ai_behavior_db.AiEmotionRealStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * AI聊天消息测试 - 启动时自动执行
 * 实现CommandLineRunner接口，Spring Boot启动后会自动调用run方法
 */
@Component // 必须加，让Spring扫描并管理这个Bean
public class AiChatMessageRunner implements CommandLineRunner {

    // 注入Service（@Resource和@Autowired都可以，选其一即可）
    @Resource
    private AiChatMessageService aiChatMessageService;


    @Autowired
    private DeepSeekTokenizer tokenizer;

    // 注入 AI角色业务服务
    @Autowired
    private AiRoleService aiRoleService;


    // 注入 AI情绪状态服务
    @Autowired
    private AiEmotionRealStateService aiEmotionRealStateService;

    /**
     * 项目启动成功后自动执行
     */
    @Override
    public void run(String... args) throws Exception {
        // 调用AI活跃度递增方法，传入固定用户ID = 2
//        aiEmotionRealStateService.startAiActivityScoreIncrement(2);
//        System.out.println("=== 项目启动完成：已对用户ID=2 执行AI活跃度初始化 ===");
    }
}