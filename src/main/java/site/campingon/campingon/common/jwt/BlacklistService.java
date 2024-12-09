package site.campingon.campingon.common.jwt;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlacklistService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ACCESS_TOKEN_BLACKLIST_PREFIX="blacklist:access:";

    public void addToBlacklist(String jti, long expirationTime) {
        String redisKey = ACCESS_TOKEN_BLACKLIST_PREFIX + jti;
        redisTemplate.opsForValue().set(redisKey, "blacklisted", expirationTime, TimeUnit.SECONDS);
        log.debug("Access Token 블랙리스트 추가 - JTI: {}, 만료 시간: {}초 후", jti, expirationTime);
    }


    public boolean isTokenBlacklisted(String jti) {
        String redisKey = ACCESS_TOKEN_BLACKLIST_PREFIX + jti;
        try {
            Boolean exists = redisTemplate.hasKey(redisKey);
            if (Boolean.TRUE.equals(exists)) {
                log.warn("블랙리스트에 있는 Access Token으로 접근 시도 중 - JTI: {}", jti);
            }
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Redis 연결 중 오류 발생 - JTI: {}, 오류: {}", jti, e.getMessage());
            return false; // Redis가 죽었을 때 기본값을 false로
        }
    }


    public void removeFromBlacklist(String jti) {
        String redisKey = "blacklist:access:" + jti;
        redisTemplate.delete(redisKey);
        log.debug("Access Token 블랙리스트에서 삭제 - JTI: {}", jti);
    }


}
