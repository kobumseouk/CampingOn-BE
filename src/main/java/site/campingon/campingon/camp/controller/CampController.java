package site.campingon.campingon.camp.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import site.campingon.campingon.camp.dto.CampDetailResponseDto;
import site.campingon.campingon.camp.dto.CampListResponseDto;
import site.campingon.campingon.camp.dto.CampSiteListResponseDto;
import site.campingon.campingon.camp.service.CampService;
import site.campingon.campingon.common.jwt.CustomUserDetails;
import site.campingon.campingon.user.entity.User;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import site.campingon.campingon.user.repository.UserKeywordRepository;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/camps")
@RequiredArgsConstructor
public class CampController {

  private final CampService campService;
  private final UserKeywordRepository userKeywordRepository;

  private static final int DEFAULT_PAGE_SIZE = 12;
  private static final int KEYWORD_MATCHED_PAGE_SIZE = 9;

  // 사용자 키워드 맞춤 캠핑장 목록 조회 (페이지네이션 - 횡스크롤)
  @GetMapping("/matched")
  public ResponseEntity<Page<CampListResponseDto>> getMatchedCamps(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "3") int size,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.ok(campService.getMatchedCampsByKeywords(
        userDetails.getId(), PageRequest.of(page, size))
    );
  }

  // 캠핑장 인기 목록 조회 (페이지네이션)
  @GetMapping("/popular")
  public ResponseEntity<Page<CampListResponseDto>> getPopularCamps(
      @RequestParam(defaultValue = "0") int page,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    boolean hasKeywords = userKeywordRepository.existsByUserId(userDetails.getId());
    int pageSize = hasKeywords ? KEYWORD_MATCHED_PAGE_SIZE : DEFAULT_PAGE_SIZE;

    return ResponseEntity.ok(
        campService.getPopularCamps(userDetails.getId(), PageRequest.of(page, pageSize))
    );
  }

  // TODO: NoSQL을 이용한 검색 기능 구현 & 엘라스틱 서치 | 인덱싱 작업
  // 검색한 캠핑장 목록 (페이지네이션)
  @GetMapping("/search")
  public ResponseEntity<Page<CampListResponseDto>> searchCamps(
      @RequestParam String keyword,
      @RequestParam(required = false) String city,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size,
      //TODO: 사용자별 검색 기록 사용 시 필요(Redis - 서버사이드 캐시)
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    // searchHistoryService.saveSearchKeyword(userDetails.getId(), keyword, city);
    return ResponseEntity.ok(campService.searchCamps(
        userDetails.getId(), keyword, city, PageRequest.of(page, size)
    ));
  }

  // 캠핑장 상세 조회  -  찜 버튼 활성화 시 유저 확인 추가
  @GetMapping("/{campId}")
  public ResponseEntity<CampDetailResponseDto> getCampDetail(
      @PathVariable("campId") Long campId
  ) {
    return ResponseEntity.ok(campService.getCampDetail(campId));
  }

  // 캠핑지 목록 조회
  @GetMapping("/{campId}/sites")
  public ResponseEntity<List<CampSiteListResponseDto>> getCampSites(
      @PathVariable("campId") Long campId
  ) {
    return ResponseEntity.ok(campService.getCampSites(campId));
  }


  // 사용자 찜 목록 조회
  @GetMapping("/bookmarked")
  public ResponseEntity<Page<CampListResponseDto>> getBookmarkedCamps(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "3") int size,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.ok(campService.getBookmarkedCamps(
        userDetails.getId(), PageRequest.of(page, size))
    );
  }

}
