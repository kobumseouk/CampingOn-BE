package site.campingon.campingon.camp.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
  @GetMapping("/{campId}/sites")
  public ResponseEntity<List<CampSiteListResponseDto>> getCampInSites(
      @PathVariable("campId") Long campId
  ) {
    return ResponseEntity.ok(campSiteService.getCampSites(campId));
  }
}
