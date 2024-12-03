package site.campingon.campingon.camp.service.mongodb;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import site.campingon.campingon.camp.entity.mongodb.SearchInfo;
import site.campingon.campingon.camp.repository.mongodb.SearchInfoRepository;

import java.util.List;


@Service
@RequiredArgsConstructor
public class SearchInfoService {
  private final SearchInfoRepository searchInfoRepository;

  public Page<SearchInfo> searchExactMatchByLocationAndName(String city, String name, Pageable pageable) {

    if (city == null) {
      if (name == null) {
        return Page.empty(pageable);
      }
      return searchInfoRepository.findByName(name, pageable);
    }

    // 지역 필터링
    Page<SearchInfo> locationFiltered = searchInfoRepository.findByAddress_City(city, pageable);

    if (name == null) {
      return locationFiltered;
    }

    // 필터링 후 이름 매칭
    List<SearchInfo> exactMatches = locationFiltered.getContent().stream()
        .filter(info -> info.getName().equals(name))
        .toList();

    return new PageImpl<>(exactMatches, pageable, exactMatches.size());
  }
}
