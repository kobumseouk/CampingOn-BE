package site.campingon.campingon.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import site.campingon.campingon.camp.entity.mongodb.CacheType;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        Arrays.stream(CacheType.values())
            .forEach(cacheType -> {
                cacheManager.registerCustomCache(cacheType.getCacheName(),
                    Caffeine.newBuilder()
                        .recordStats()   // 캐시에 대한 Statics 적용
                        .expireAfterWrite(Duration.ofHours(cacheType.getExpiredAfterWrite()))  // 항목을 수정 후 만료 시간 이후 삭제
                        .maximumSize(cacheType.getMaximumSize())  // 크기 지정
                        .build()
                );
            });

        return cacheManager;
    }
}
