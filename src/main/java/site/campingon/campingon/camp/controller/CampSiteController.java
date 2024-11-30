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
}
