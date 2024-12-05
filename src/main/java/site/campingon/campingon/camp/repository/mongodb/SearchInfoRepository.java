package site.campingon.campingon.camp.repository.mongodb;

import org.bson.Document;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import site.campingon.campingon.camp.entity.mongodb.SearchInfo;

import java.util.List;


public interface SearchInfoRepository extends MongoRepository<SearchInfo, String> {
    @Aggregation(pipeline = {
        "{$search: {" +
            "index: 'searchIndex'," +
            "compound: {" +
                "should: [" +
                    "{text: { query: ?0, path: 'name', score: { boost: { value: 5 } }, fuzzy: { maxEdits: 1 } }}," +
                    "{text: { query: ?0, path: 'hashtags', score: { boost: { value: 3 } }, fuzzy: { maxEdits: 1 } }}," +
                    "{text: { query: ?0, path: 'address.state', score: { boost: { value: 2 } }, fuzzy: { maxEdits: 1 } }}," +
                    "{text: { query: ?1, path: 'hashtags', score: { boost: { value: 1 } } }}" +
                "]," +
                "must: [" +
                    "{text: { query: ?2, path: 'address.city' }}" +
                "]" +
            "}" +
        "}}",
        "{$project: {" +
            "camp_id: 1," +
            "name: 1," +
            "intro: 1," +
            "image_url: 1," +
            "streetAddr: '$address.street_addr'," +
            "hashtags: 1," +
            "score: {$meta: 'searchScore'}" +
        "}}"
    })
    List<SearchInfo> searchWithUserPreferences(String name, List<String> userKeywords, String city);


    // 전체 문서 수를 계산하기 위한 메서드
    @Aggregation(pipeline = {
        "{$search: {" +
            "index: 'searchIndex'," +
            "compound: {" +
                "should: [" +
                    "{text: { query: ?0, path: ['name', 'hashtags', 'address.state', 'address.city', 'intro'], fuzzy: { maxEdits: 1 } }}," +
                    "{text: { query: ?1, path: 'hashtags' }}" +
                "]," +
                "must: [" +
                    "{text: { query: ?2, path: 'address.city' }}" +
                "]" +
            "}" +
        "}}",
        "{$count: 'total'}"
    })
    List<Document> countSearchResults(String searchTerm, List<String> userKeywords, String city);
}
