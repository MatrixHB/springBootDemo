package config;

import entities.BusData;
import org.springframework.cache.CacheManager;
import org.springframework.cache.config.CacheManagementConfigUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.repository.query.Param;

import java.net.UnknownHostException;

@Configuration
public class MyRedisConfig {

    //key-value的自定义序列化机制
    @Bean
    public RedisTemplate<String, BusData> busRedisTemplate(RedisConnectionFactory redisConnectionFactory) throws UnknownHostException {
        RedisTemplate<String, BusData> template = new RedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        //key仍按照字符串序列化机制
        template.setKeySerializer(RedisSerializer.string());

        //value按照对象json序列化机制
        Jackson2JsonRedisSerializer<BusData> serializer = new Jackson2JsonRedisSerializer<BusData>(BusData.class);
        template.setValueSerializer(serializer);
        return template;
    }

    //会覆盖默认配置的RedisCacheManager
    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory){
        Jackson2JsonRedisSerializer<BusData> serializer = new Jackson2JsonRedisSerializer<BusData>(BusData.class);

        //配置key-value序列化机制
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));
        //用builer.build的方式创建RedisCacheManager
        RedisCacheManager cacheManager = RedisCacheManager.builder(redisConnectionFactory).cacheDefaults(config).build();
        return cacheManager;
    }
}
