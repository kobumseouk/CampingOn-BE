package site.campingon.campingon.camp.service.mongodb;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import site.campingon.campingon.bookmark.repository.BookmarkRepository;
import site.campingon.campingon.camp.dto.CampListResponseDto;
import site.campingon.campingon.camp.dto.mongodb.SearchResultDto;
import site.campingon.campingon.camp.entity.mongodb.SearchInfo;
import site.campingon.campingon.camp.mapper.mongodb.SearchInfoMapper;
import site.campingon.campingon.camp.repository.mongodb.MongoMatchedClient;
import site.campingon.campingon.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CampMatchedService {
    private final UserService userService;
    private final MongoMatchedClient mongoMatchedClient;
    private final BookmarkRepository bookmarkRepository;
    private final SearchInfoMapper searchInfoMapper;

    public Page<CampListResponseDto> getMatchedCampsByKeywords(String username, Long userId, Pageable pageable) {
        List<String> userKeywords = userService.getKeywordsByUserId(userId).getKeywords();
        if (userKeywords.isEmpty()) {
            return Page.empty(pageable);
        }

        SearchResultDto searchResult = mongoMatchedClient.getMatchedCamps(userKeywords, pageable);
        return convertToPageResponse(searchResult, userId, username);
    }

    private Page<CampListResponseDto> convertToPageResponse(SearchResultDto searchResult, Long userId, String username) {
        List<CampListResponseDto> dtoList = searchResult.getResults().stream()
            .map(searchInfo -> convertToResponseDto(searchInfo, userId, username))
            .collect(Collectors.toList());

        return new PageImpl<>(
            dtoList,
            PageRequest.of(searchResult.getCurrentPage(), searchResult.getResults().size()),
            searchResult.getTotal()
        );
    }

    private CampListResponseDto convertToResponseDto(SearchInfo searchInfo, Long userId, String username) {
        CampListResponseDto dto = searchInfoMapper.toDto(searchInfo);
        dto.setUsername(username);
        dto.setMarked(bookmarkRepository.existsByCampIdAndUserId(searchInfo.getCampId(), userId));
        return dto;
    }

}
