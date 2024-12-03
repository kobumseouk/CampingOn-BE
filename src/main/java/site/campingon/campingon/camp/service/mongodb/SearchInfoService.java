package site.campingon.campingon.camp.service.mongodb;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.campingon.campingon.camp.entity.mongodb.SearchInfo;
import site.campingon.campingon.camp.repository.mongodb.SearchInfoRepository;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchInfoService {
  private final SearchInfoRepository searchInfoRepository;

  public List<SearchInfo> searchExactMatchByLocationAndName(String city, String name) {
    if (city == null || name == null) {
      return Collections.emptyList();
    }
    return searchInfoRepository.findByAddress_CityAndName(city, name);
  }
}
