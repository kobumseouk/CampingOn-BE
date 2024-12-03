package site.campingon.campingon.camp.controller.mongodb;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.campingon.campingon.camp.entity.mongodb.SearchInfo;
import site.campingon.campingon.camp.service.mongodb.SearchInfoService;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchInfoController {
  private final SearchInfoService searchInfoService;

  @GetMapping("/exact")
  public ResponseEntity<List<SearchInfo>> searchExactMatch(
      @RequestParam(name = "city") String city,
      @RequestParam(name = "name", required = true) String name
  ) {
    List<SearchInfo> results = searchInfoService.searchExactMatchByLocationAndName(city, name);
    return results.isEmpty()
        ? ResponseEntity.noContent().build()
        : ResponseEntity.ok(results);
  }
}
