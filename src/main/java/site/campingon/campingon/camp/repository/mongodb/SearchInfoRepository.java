package site.campingon.campingon.camp.repository.mongodb;

import org.springframework.data.mongodb.repository.MongoRepository;
import site.campingon.campingon.camp.entity.mongodb.SearchInfo;

import java.util.List;

public interface SearchInfoRepository extends MongoRepository<SearchInfo, String> {
  // 지역과 이름이 정확히 일치하는 데이터 찾기
  List<SearchInfo> findByAddress_CityAndName(String city, String name);
}
