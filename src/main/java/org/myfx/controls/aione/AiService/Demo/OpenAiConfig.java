package org.myfx.controls.aione.AiService.Demo;//package org.myfx.controls.SpringAiDemo.Demo;
//
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
//import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
//import org.springframework.ai.chat.memory.ChatMemory;
//import org.springframework.ai.chat.memory.ChatMemoryRepository;
//import org.springframework.ai.chat.memory.MessageWindowChatMemory;
//import org.springframework.ai.openai.OpenAiChatModel;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class OpenAiConfig {
//    // 在配置类中创建 Advisor 时，指定 responseSize（默认带多少条历史）
//    @Bean
//    public ChatClient chatClient(OpenAiChatModel chatModel, ChatMemory chatMemory) {
//        return ChatClient.builder(chatModel)
//                .defaultAdvisors(
//                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
//        new SimpleLoggerAdvisor()// chat-memory advisor
////                        QuestionAnswerAdvisor.builder(vectorStore).build()    // RAG advisor
//                )
//                .build();
//    }
//
//    // 1. 构建MongoDB版ChatMemoryRepository
//    // 不要重写Bean,你看看下面的,直接注入
////    @Bean
////    public ChatMemoryRepository chatMemoryRepository(MongoTemplate mongoTemplate) {
////        // 手动构建MongoChatMemoryRepository，关联MongoTemplate
////        return MongoChatMemoryRepository.builder()
////                .mongoTemplate(mongoTemplate)
////                .build();
////    }
//
//    @Bean
//    public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
//        // 注入自动配置的 JdbcChatMemoryRepository
//        return MessageWindowChatMemory.builder()
//                .chatMemoryRepository(chatMemoryRepository) // 关联持久化存储库
//                .maxMessages(5) // 设定每个会话保存的最大消息数
//                .build();
//    }
//
//    // ========== 以下是【可选】手动构建JdbcChatMemoryRepository的方式（替代自动注入） ==========
//    // 说明：Spring AI会自动配置JdbcChatMemoryRepository，无需手动构建；
//    // 仅当你需要自定义表名、方言（非MySQL）、JDBC模板时，才需要手动构建
//    /*
//    @Bean
//    public ChatMemoryRepository chatMemoryRepository(JdbcTemplate jdbcTemplate) {
//        return JdbcChatMemoryRepository.builder()
//                .jdbcTemplate(jdbcTemplate) // 注入Spring自动配置的JdbcTemplate（关联MySQL数据源）
//                // MySQL场景：无需自定义Dialect，默认会加载MySqlChatMemoryRepositoryDialect
//                // .dialect(new MySqlChatMemoryRepositoryDialect()) // 可显式指定，非必需
//                .tableName("chat_memory") // 自定义表名（默认就是chat_memory）
//                .build();
//    }
//    */
//}
