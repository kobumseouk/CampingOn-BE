package site.campingon.campingon.camp.dto.mongodb;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;

@Getter
@Builder
public class SearchCriteriaDto {
    private final String searchTerm;
    private final List<String> userKeywords;
    private final String city;
    private final Pageable pageable;

    public SearchCriteriaDto withUserKeywords(List<String> userKeywords) {
        return SearchCriteriaDto.builder()
            .searchTerm(this.searchTerm)
            .userKeywords(userKeywords)
            .city(this.city)
            .pageable(this.pageable)
            .build();
    }

    // 유효성 검증 메서드
    public boolean hasSearchCriteria() {
        return StringUtils.hasText(searchTerm) ||
            (userKeywords != null && !userKeywords.isEmpty()) ||
            StringUtils.hasText(city);
    }
}
