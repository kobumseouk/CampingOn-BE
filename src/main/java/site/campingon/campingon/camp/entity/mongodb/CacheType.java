package site.campingon.campingon.camp.entity.mongodb;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CacheType {
    AUTOCOMPLETE("autocomplete", 2, 1200);

    private final String cacheName;
    private final int expiredAfterWrite;
    private final int maximumSize;
}
