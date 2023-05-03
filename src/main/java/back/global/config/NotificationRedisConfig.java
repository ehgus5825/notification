package back.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@EnableCaching
public class NotificationRedisConfig {
    @Value("${spring.redis.notification.host}")
    private String host;

    @Value("${spring.redis.notification.port}")
    private int port;

    @Bean
    public RedisTemplate<String, Boolean> notificationRedisTemplate(){
        RedisTemplate<String, Boolean> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(notificationConnectionFactory());
        return redisTemplate;
    }

    @Bean
    public RedisConnectionFactory notificationConnectionFactory(){
        return new LettuceConnectionFactory(host, port);
    }

}
