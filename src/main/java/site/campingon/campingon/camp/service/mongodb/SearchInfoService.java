package site.campingon.campingon.camp.service.mongodb;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.bson.Document;
import site.campingon.campingon.bookmark.repository.BookmarkRepository;
import site.campingon.campingon.camp.dto.CampListResponseDto;
import site.campingon.campingon.camp.entity.mongodb.SearchInfo;
import site.campingon.campingon.camp.mapper.mongodb.SearchInfoMapper;
import site.campingon.campingon.camp.repository.mongodb.SearchInfoRepository;
import site.campingon.campingon.user.repository.UserRepository;
import site.campingon.campingon.user.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class SearchInfoService {
  private final UserService userService;
  private final SearchInfoRepository searchInfoRepository;
  private final UserRepository userRepository;
  private final BookmarkRepository bookmarkRepository;
  private final SearchInfoMapper searchInfoMapper;

  public Page<CampListResponseDto> searchExactMatchBySearchTermAndUserKeyword(
      String city,
      String searchTerm,
      Long userId,
      Pageable pageable) {
    List<String> userKeywords = new ArrayList<>();

    if (userId != 0L) {  // 인증된 사용자인 경우
      userKeywords = userService.getKeywordsByUserId(userId);
    }

    // 검색 결과 가져오기
    List<SearchInfo> results = searchInfoRepository.searchWithUserPreferences(
        searchTerm,
        userKeywords,
        city
    );

    // 전체 결과 수 계산
    List<Document> countResult = searchInfoRepository.countSearchResults(
        searchTerm,
        userKeywords,
        city
    );
    long total = countResult.isEmpty() ? 0 : ((Document) countResult.get(0)).getLong("total");

    // 페이징 처리
    int start = (int) pageable.getOffset();
    int end = Math.min((start + pageable.getPageSize()), results.size());
    List<SearchInfo> pageContent = results.subList(start, end);

    // DTO 변환 및 Page 객체 생성
    List<CampListResponseDto> dtoList = pageContent.stream()
        .map(searchInfo -> {
          CampListResponseDto dto = searchInfoMapper.toDto(searchInfo);

          // 인증된 사용자인 경우에만 북마크와 유저명 설정
          if (userId != 0L) {
            // 북마크 상태 설정
            dto.setMarked(bookmarkRepository.existsByCampIdAndUserId(searchInfo.getCampId(), userId));

            // 유저명 설정
            userRepository.findById(userId)
                .ifPresent(user -> dto.setUsername(user.getNickname()));
          }

          return dto;
        })
        .collect(Collectors.toList());

    return new PageImpl<>(dtoList, pageable, total);
  }
}
