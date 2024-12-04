package site.campingon.campingon.camp.controller.mongodb;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.campingon.campingon.camp.dto.CampListResponseDto;
import site.campingon.campingon.camp.service.mongodb.SearchInfoService;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/mongo/camps")
@RequiredArgsConstructor
public class SearchInfoController {
  private final SearchInfoService searchInfoService;

  /*@Value("${app.pagination.searchInfo.page}")
  private int page;

  @Value("${app.pagination.searchInfo.size}")
  private int size;*/

  @GetMapping("/search")
  public ResponseEntity<Page<CampListResponseDto>> searchCamps(
      @RequestParam(name = "city", required = false, defaultValue = "") String city,
      @RequestParam(name = "name", required = false, defaultValue = "") String name,
      @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
      @RequestParam(name = "size", defaultValue = "12") @Positive @Max(100)int size
  ) {
    return ResponseEntity.ok(
        searchInfoService.searchExactMatchByLocationAndName(
            city,
            name,
            PageRequest.of(page, size)
        )
    );
  }
}
