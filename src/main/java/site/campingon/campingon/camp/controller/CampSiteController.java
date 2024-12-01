package site.campingon.campingon.camp.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.campingon.campingon.camp.dto.CampSiteListResponseDto;
import site.campingon.campingon.camp.service.CampSiteService;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/camps")
@RequiredArgsConstructor
public class CampSiteController {
  private final CampSiteService campSiteService;

  // 캠핑지 목록 조회
  @GetMapping("/{campId}/available-sites")
  public ResponseEntity<List<CampSiteListResponseDto>> getAvailableCampSites(
      @PathVariable Long campId,
      @RequestParam List<Long> reservedSiteIds
  ) {
    return ResponseEntity.ok(campSiteService.getAvailableCampSites(campId, reservedSiteIds));
  }

  // 특정 캠핑지의 isAvailable 상태를 토글
  @PutMapping("/{campSiteId}/toggle-availability")
  public ResponseEntity<Boolean> toggleAvailability(
          @PathVariable Long campSiteId
  ) {
    boolean newAvailability = campSiteService.toggleAvailability(campSiteId);
    return ResponseEntity.ok(newAvailability); // 변경된 isAvailable 상태 반환
  }

  // 특정 캠핑지의 isAvailable 상태 조회
  @GetMapping("/{campSiteId}/availability")
  public ResponseEntity<Boolean> getAvailability(
          @PathVariable Long campSiteId
  ) {
    boolean isAvailable = campSiteService.getAvailability(campSiteId);
    return ResponseEntity.ok(isAvailable); // 현재 isAvailable 상태 반환
  }
}
