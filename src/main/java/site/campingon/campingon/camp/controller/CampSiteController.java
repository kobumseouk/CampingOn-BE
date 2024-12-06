package site.campingon.campingon.camp.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.campingon.campingon.camp.dto.CampSiteListResponseDto;
import site.campingon.campingon.camp.dto.CampSiteResponseDto;
import site.campingon.campingon.camp.service.CampSiteReserveService;
import site.campingon.campingon.camp.service.CampSiteService;

import java.time.LocalDate;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/camps")
@RequiredArgsConstructor
public class CampSiteController {

    private final CampSiteReserveService campSiteReserveService;
    private final CampSiteService campSiteService;

    // 캠핑장의 예약가능한 캠핑지 목록 조회
    @GetMapping("/{campId}/available")
    public ResponseEntity<List<CampSiteListResponseDto>> getAvailableCampSites(@PathVariable("campId") Long campId,

                                                                               @RequestParam(value = "checkin")
                                                                               @DateTimeFormat(pattern = "yyyy-MM-dd")
                                                                               LocalDate checkin,

                                                                               @RequestParam(value = "checkout")
                                                                               @DateTimeFormat(pattern = "yyyy-MM-dd")
                                                                               LocalDate checkout) {

        return ResponseEntity.ok(campSiteReserveService.getAvailableCampSites(campId, checkin, checkout));
    }

    // 특정 캠핑지 조회
    @GetMapping("/{campId}/sites/{siteId}")
    public ResponseEntity<CampSiteResponseDto> getCampSite(@PathVariable("campId") Long campId,
                                                           @PathVariable("siteId") Long siteId) {

        return ResponseEntity.ok(campSiteService.getCampSite(campId, siteId));
    }

}
