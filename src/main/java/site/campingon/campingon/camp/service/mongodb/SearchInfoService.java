package site.campingon.campingon.camp.service.mongodb;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import site.campingon.campingon.bookmark.repository.BookmarkRepository;
import site.campingon.campingon.camp.dto.CampListResponseDto;
import site.campingon.campingon.camp.dto.mongodb.SearchCriteriaDto;
import site.campingon.campingon.camp.dto.mongodb.SearchResultDto;
import site.campingon.campingon.camp.entity.mongodb.SearchInfo;
import site.campingon.campingon.camp.mapper.mongodb.SearchInfoMapper;
import site.campingon.campingon.camp.repository.mongodb.MongoSearchClient;
import site.campingon.campingon.user.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class SearchInfoService {
    private final UserService userService;
    private final MongoSearchClient mongoSearchClient;
    private final BookmarkRepository bookmarkRepository;
    private final SearchInfoMapper searchInfoMapper;

/*
    public Page<CampListResponseDto> searchExactMatchBySearchTermAndUserKeyword(
        String city, String searchTerm, Long userId, Pageable pageable) {

        // city와 searchTerm이 모두 비어있으면 빈 페이지 반환
        if (!StringUtils.hasText(city) && !StringUtils.hasText(searchTerm)) {
            return Page.empty(pageable);
        }

        // 사용자 키워드 조회 (검색 조건이 있을 때만 사용됨)
        List<String> userKeywords = userId != 0L ?
            userService.getKeywordsByUserId(userId).getKeywords() :
            new ArrayList<>();

        // 검색 수행 (결과와 전체 개수를 한 번에 조회)
        SearchResultDto searchResult = mongoSearchClient.searchWithUserPreferences(searchTerm, userKeywords, city, pageable);

        // 검색 결과가 없는 경우 빈 페이지 반환
        if (searchResult.getResults().isEmpty()) {
            return Page.empty(pageable);
        }

        // DTO 변환 및 Page 객체 생성
        List<CampListResponseDto> dtoList = searchResult.getResults().stream()
            .map(searchInfo -> {
                CampListResponseDto dto = searchInfoMapper.toDto(searchInfo);

                // 인증된 사용자인 경우에만 북마크 상태 설정
                if (userId != 0L) {
                    dto.setMarked(bookmarkRepository.existsByCampIdAndUserId(searchInfo.getCampId(), userId));
                }

                return dto;
            })
            .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, searchResult.getTotal());
    }
*/

    // 지역명과 키워드 검색 서비스
    public Page<CampListResponseDto> searchCamps(SearchCriteriaDto criteria, Long userId) {
        if (!criteria.hasSearchCriteria()) {
            return Page.empty(criteria.getPageable());
        }

        List<String> userKeywords = getUserKeywords(userId);
        SearchCriteriaDto enrichedCriteria = criteria.withUserKeywords(userKeywords);  // userKeyword 추가
        SearchResultDto searchResult = mongoSearchClient.search(enrichedCriteria);

        return convertToPageResponse(searchResult, userId);
    }

    private List<String> getUserKeywords(Long userId) {
        return userId != 0L ?
            userService.getKeywordsByUserId(userId).getKeywords() :
            new ArrayList<>();
    }

    private Page<CampListResponseDto> convertToPageResponse(SearchResultDto searchResult, Long userId) {
        List<CampListResponseDto> dtoList = searchResult.getResults().stream()
            .map(searchInfo -> convertToResponseDto(searchInfo, userId))
            .collect(Collectors.toList());

        return new PageImpl<>(
            dtoList,
            PageRequest.of(searchResult.getCurrentPage(), searchResult.getResults().size()),
            searchResult.getTotal()
        );
    }

    private CampListResponseDto convertToResponseDto(SearchInfo searchInfo, Long userId) {
        CampListResponseDto dto = searchInfoMapper.toDto(searchInfo);
        if (userId != 0L) {
            dto.setMarked(bookmarkRepository.existsByCampIdAndUserId(searchInfo.getCampId(), userId));
        }
        return dto;
    }

    // 검색어 자동완성
    @Cacheable(value = "autocomplete", key = "#word", unless = "#result.isEmpty()")
    public List<String> getAutocompleteResults(String word) {
        if (!StringUtils.hasText(word) || word.length() < 2) {
            return new ArrayList<>();
        }
        return mongoSearchClient.getAutocompleteResults(word);
    }


    /*// 사용자 캠핑장 추천 서비스
    public Page<CampListResponseDto> getMatchedCampsByKeywords(
        String username, Long userId, Pageable pageable) {
        // 사용자 키워드 조회
        List<String> userKeywords = userService.getKeywordsByUserId(userId).getKeywords();

        if (userKeywords.isEmpty()) {
            return Page.empty(pageable);
        }

        // 매칭된 캠핑장 조회
        SearchResultDto searchResult = mongoRecommendClient.getMatchedCamps(userKeywords, pageable);

        if (searchResult.getResults().isEmpty()) {
            return Page.empty(pageable);
        }

        // DTO 변환 및 Page 객체 생성
        List<CampListResponseDto> dtoList = searchResult.getResults().stream()
            .map(searchInfo -> {
                CampListResponseDto dto = searchInfoMapper.toDto(searchInfo);
                dto.setUsername(username);

                dto.setMarked(bookmarkRepository.existsByCampIdAndUserId(
                    searchInfo.getCampId(), userId));
                return dto;
            })
            .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, searchResult.getTotal());
    }*/
}
