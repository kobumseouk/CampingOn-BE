package site.campingon.campingon.camp.controller.mongodb;

import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchInfoController {
  private final SearchInfoService searchInfoService;

  @GetMapping("/exact")
  public ResponseEntity<Page<SearchInfo>> searchExactMatch(
      @RequestParam(name = "city", required = false) String city,
      @RequestParam(name = "name", required = false) String name,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "12") int size
  ) {
    Page<SearchInfo> results = searchInfoService.searchExactMatchByLocationAndName(
        city,
        name,
        PageRequest.of(page, size)
    );

    return results.isEmpty()
        ? ResponseEntity.noContent().build()
        : ResponseEntity.ok(results);
  }
}
