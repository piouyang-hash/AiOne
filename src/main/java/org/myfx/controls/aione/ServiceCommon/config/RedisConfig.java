package org.myfx.controls.aione.ServiceCommon.config;

import org.myfx.controls.aione.AiService.dto.ChatChunkDTO;
import org.myfx.controls.aione.AiService.dto.redis.AiActivityScoreRedisDTO;
import org.myfx.controls.aione.AiService.dto.redis.AiChatQueueTask;
import org.myfx.controls.aione.AiService.dto.redis.UserActivityScoreRedisDTO;
import org.myfx.controls.aione.ConnectService.entity.UserStatus;
import org.myfx.controls.aione.SimulationGame.entity.SimulateEventRecordRedis;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;
import tools.jackson.databind.DefaultTyping;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;

@Configuration
public class RedisConfig {

    // 读取配置文件中的Redis Key前缀
    @Value("${simulate.redis.event.record.prefix}")
    private String eventRecordKeyPrefix;

    /**
     * 将Key前缀封装为Bean，供工具类注入使用
     */
    @Bean("eventRecordKeyPrefix")
    public String eventRecordKeyPrefix() {
        return eventRecordKeyPrefix;
    }

    // 1. 抽离公共的 ObjectMapper（Jackson 3 规范）
    private ObjectMapper createJackson3Mapper() {
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(Object.class).build();

        return JsonMapper.builder()
                .changeDefaultVisibility(checker ->
                        checker.withVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY))
                .activateDefaultTyping(typeValidator, DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY)
                .build();
    }

    // 2. 抽离通用的模板构建工具方法（泛型方法）
    private <T> RedisTemplate<String, T> createTemplate(RedisConnectionFactory factory, Class<T> clazz) {
        RedisTemplate<String, T> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 传入当前类的 Class 即可
        JacksonJsonRedisSerializer<T> serializer = new JacksonJsonRedisSerializer<>(createJackson3Mapper(), clazz);

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }

    // 3. 抽离通用的响应式模板构建工具方法
    private <T> ReactiveRedisTemplate<String, T> createReactiveTemplate(
            ReactiveRedisConnectionFactory factory, Class<T> clazz) {

        JacksonJsonRedisSerializer<T> jsonSerializer = new JacksonJsonRedisSerializer<>(createJackson3Mapper(), clazz);
        StringRedisSerializer keySerializer = new StringRedisSerializer();

        RedisSerializationContext<String, T> serializationContext =
                RedisSerializationContext.<String, T>newSerializationContext(keySerializer)
                        .value(jsonSerializer)
                        .hashKey(keySerializer)
                        .hashValue(jsonSerializer)
                        .build();

        return new ReactiveRedisTemplate<>(factory, serializationContext);
    }

    /* ==================== 以后新增实体类，只需要在这里加一个 3 行的 Bean 声明 ==================== */

    @Bean("eventRecordRedisTemplate")
    public RedisTemplate<String, SimulateEventRecordRedis> eventRecordRedisTemplate(RedisConnectionFactory factory) {
        return createTemplate(factory, SimulateEventRecordRedis.class); // 极简！
    }

    @Bean("userActivityScoreRedisDTOTemplate")
    public RedisTemplate<String, UserActivityScoreRedisDTO> userActivityScoreRedisDTOTemplate(RedisConnectionFactory factory) {
        return createTemplate(factory, UserActivityScoreRedisDTO.class);
    }

    @Bean("aiActivityScoreRedisDTOTemplate")
    public RedisTemplate<String, AiActivityScoreRedisDTO> aiActivityScoreRedisDTOTemplate(RedisConnectionFactory factory) {
        return createTemplate(factory, AiActivityScoreRedisDTO.class);
    }

    @Bean("aiChatQueueTaskRedisTemplate")
    public RedisTemplate<String, AiChatQueueTask> aiChatQueueTaskRedisTemplate(RedisConnectionFactory factory) {
        return createTemplate(factory, AiChatQueueTask.class);
    }

    @Bean("userStatusRedisTemplate")
    public RedisTemplate<String, UserStatus> userStatusRedisTemplate(RedisConnectionFactory factory) {
        return createTemplate(factory, UserStatus.class);
    }

    // ==================== 响应式 RedisTemplate ====================
    @Bean("aiChatQueueReactiveRedisTemplate")
    public ReactiveRedisTemplate<String, AiChatQueueTask> aiChatQueueReactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory) {
        // 极简一行，直接复用！
        return createReactiveTemplate(factory, AiChatQueueTask.class);
    }

    @Bean("aiChatChunkReactiveRedisTemplate")
    public ReactiveRedisTemplate<String, ChatChunkDTO> aiChatChunkReactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory) {
        // 极简一行，直接复用！
        return createReactiveTemplate(factory, ChatChunkDTO.class);
    }

    /**
     * 配置Redis消息监听容器（核心，必须）
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory factory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        // 这里只负责容器本身的配置，不要在这里改 Redis 服务端的配置
        // 亲自去Redis中修改
        // 给出Redis键值过期监听通知开启指令！
//        > CONFIG SET notify-keyspace-events Ex
//                OK
        return container;
    }

}