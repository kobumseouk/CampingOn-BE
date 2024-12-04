package site.campingon.campingon.camp.controller.mongodb;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.campingon.campingon.camp.entity.mongodb.SearchInfo;
import site.campingon.campingon.camp.service.mongodb.SearchInfoService;


@RestController
@RequestMapping("/api/mongo/camps")
@RequiredArgsConstructor
public class SearchInfoController {
  private final SearchInfoService searchInfoService;

  @Value("${app.pagination.searchInfo.page}")
  private int page;

  @Value("${app.pagination.searchInfo.size}")
  private int size;

  @GetMapping("/search")
  public ResponseEntity<Page<SearchInfo>> searchCamps(
      @RequestParam(name = "city", required = false) String city,
      @RequestParam(name = "name", required = false) String name
  ) {
    Page<SearchInfo> results = searchInfoService.searchExactMatchByLocationAndName(
        city,
        name,
        PageRequest.of(page, size)
    );

    return ResponseEntity.ok(results);
//    return results.isEmpty()
//        ? ResponseEntity.noContent().build()
//        : ResponseEntity.ok(results);
  }
}
