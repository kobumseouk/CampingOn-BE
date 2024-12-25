package site.campingon.campingon.camp.controller.mongodb;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.campingon.campingon.camp.dto.CampListResponseDto;
import site.campingon.campingon.camp.service.mongodb.CampMatchedService;
import site.campingon.campingon.common.jwt.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/mongo/camps/matched")
@RequiredArgsConstructor
public class CampMatchedController {
    private final CampMatchedService campMatchedService;

    // 사용자 키워드 맞춤 캠핑장 목록 조회 (페이지네이션 - 횡스크롤)
    @GetMapping
    public ResponseEntity<Page<CampListResponseDto>> getMatchedCamps(
        @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
        @RequestParam(name = "size", defaultValue = "3") @Positive @Max(30) int size,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(campMatchedService.getMatchedCampsByKeywords(
            userDetails.getName(), userDetails.getId(), PageRequest.of(page, size))
        );
    }
}
