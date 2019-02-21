package config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class MyCacheConfig {

    //在RedisCacheManager存在的同时引入其他的CacheManager
    @Bean
    public ConcurrentMapCacheManager concurrentMapCacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        List<String> cacheNames = new ArrayList<>();
        cacheNames.add("bus");
        cacheManager.setCacheNames(cacheNames);

        return cacheManager;
    }

    @Bean
    public KeyGenerator myKeyGenerator(){
        return new KeyGenerator() {
            @Override
            public Object generate(Object o, Method method, Object... objects) {
                return  "All";
            }
        };
    }
}
