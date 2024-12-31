package site.campingon.campingon.camp.dto.mongodb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Pageable;
import site.campingon.campingon.camp.entity.mongodb.SearchInfo;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class SearchResultDto {
    private final List<SearchInfo> results;   // 결과
    private final long total;   // 데이터 총 개수
    private final int currentPage;
    private final int totalPages;
    private final boolean hasNext;  // 다음 페이지 여부

    public static SearchResultDto of(List<SearchInfo> results, long total, Pageable pageable) {
        int totalPages = (int) Math.ceil((double) total / pageable.getPageSize());
        return SearchResultDto.builder()
            .results(results)
            .total(total)
            .currentPage(pageable.getPageNumber())
            .totalPages(totalPages)
            .hasNext(pageable.getPageNumber() + 1 < totalPages)
            .build();
    }
}
