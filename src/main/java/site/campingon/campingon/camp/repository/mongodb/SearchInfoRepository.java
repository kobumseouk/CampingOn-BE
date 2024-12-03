package site.campingon.campingon.camp.repository.mongodb;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import site.campingon.campingon.camp.entity.mongodb.SearchInfo;


public interface SearchInfoRepository extends MongoRepository<SearchInfo, String> {
  Page<SearchInfo> findByName(String name, Pageable pageable);

  Page<SearchInfo> findByAddress_City(String city, Pageable pageable);
}
