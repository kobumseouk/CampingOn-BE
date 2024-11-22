package site.campingon.campingon.camp.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import site.campingon.campingon.camp.dto.CampDetailResponseDto;
import site.campingon.campingon.camp.dto.CampListResponseDto;
import site.campingon.campingon.camp.dto.CampSiteListResponseDto;
import site.campingon.campingon.camp.service.CampService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;


import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/camps")
@RequiredArgsConstructor
public class CampController {

  private final CampService campService;

  private static final int RECOMMENDED_CAMP_SIZE = 3;
  private static final int DEFAULT_PAGE_SIZE = 12;
  private static final int KEYWORD_MATCHED_PAGE_SIZE = 9;

  // 사용자 키워드 맞춤 캠핑장 목록 조회
  @GetMapping("/recommended")
  @PreAuthorize("isAuthenticated()")  // 로그인 확인
  public ResponseEntity<List<CampListResponseDto>> getRecommendedCamps(
      @AuthenticationPrincipal UserDetails userDetails   // 추후 UserDto로 커스텀 
  ) {
    User user = (User) userDetails;
    List<CampListResponseDto> recommendedCamps =
        campService.getRecommendedCampsByKeywords(   // 사용자 키워드에 가장 일치하는 내림차순 정렬
            user.getId(),
            RECOMMENDED_CAMP_SIZE
        );
    return ResponseEntity.ok(recommendedCamps);
  }

  // 캠핑장 인기 목록 조회
  @GetMapping("/popular")
  @PreAuthorize("isAuthenticated()")  // 로그인 확인
  public ResponseEntity<Page<CampListResponseDto>> getPopularCamps(
      @RequestParam(defaultValue = "0") int page,
      @AuthenticationPrincipal UserDetails userDetails
  ) {
    User user = (User) userDetails;
    int pageSize = user.hasKeywords() ? KEYWORD_MATCHED_PAGE_SIZE : DEFAULT_PAGE_SIZE;

    PageRequest pageRequest = PageRequest.of(page, pageSize);
    Page<CampListResponseDto> popularCamps =
        campService.getPopularCamps(user.getId(), pageRequest);  // 추천 수 내림차순 정렬

    return ResponseEntity.ok(popularCamps);
  }

/*
  // 검색한 캠핑장 목록 (페이지네이션)
  @GetMapping("/search")
  @PreAuthorize("isAuthenticated()")  // 로그인 확인
  public ResponseEntity<Page<CampListResponseDto>> searchCamps(
      @RequestParam String keyword,
      @RequestParam(required = false) String location,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size,
      UserDto currentUser
  ) {
    PageRequest pageRequest = PageRequest.of(page, size);
    Long userId = currentUser.getUserId();
    Page<CampListResponseDto> camps = campService.searchCamps(keyword, location, pageRequest, userId);
    return ResponseEntity.ok(camps);
  }
*/

  // 캠핑장 상세
  @GetMapping("/{campId}")
  @PreAuthorize("isAuthenticated()")  // 로그인 확인 
  public ResponseEntity<CampDetailResponseDto> getCampDetail(
      @PathVariable Long campId
  ) {
    CampDetailResponseDto camp = campService.getCampDetail(campId);
    return ResponseEntity.ok(camp);
  }

  // 캠핑지 목록
  @GetMapping("/{campId}/sites")
  @PreAuthorize("isAuthenticated()")  // 로그인 확인
  public ResponseEntity<List<CampSiteListResponseDto>> getCampSites(
      @PathVariable Long campId
  ) {
    List<CampSiteListResponseDto> sites = campService.getCampSites(campId);
    return ResponseEntity.ok(sites);
  }


/*  // 찜하기
  @PostMapping("/{campId}/likes")
  @PreAuthorize("isAuthenticated()")  // 로그인 확인
  public ResponseEntity<Void> likeCamp(
      @PathVariable Long campId
  ) {

  }

  // 찜 해제
  @DeleteMapping("/{campId}/likes")
  @PreAuthorize("isAuthenticated()")  // 로그인 확인
  public ResponseEntity<Void> unlikeCamp(
      @PathVariable Long campId
  ) {

  }

  // 사용자 찜 목록 조회
  @GetMapping("/likes")
  @PreAuthorize("isAuthenticated()")  // 로그인 확인
  public ResponseEntity<List<CampListResponseDto>> getLikedCamps(
      UserDto currentUser
  ) {
    return ResponseEntity.ok(
        campService.getLikedCamps(currentUser.getUserId())
    );
  }*/

}
