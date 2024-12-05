package site.campingon.campingon.camp.repository.mongodb;

import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface SearchInfoRepository {
    SearchInfoRepositoryImpl.SearchResult searchWithUserPreferences(String searchTerm, List<String> userKeywords, String city, Pageable pageable);
}
