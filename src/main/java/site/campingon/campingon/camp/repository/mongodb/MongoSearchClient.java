package site.campingon.campingon.camp.repository.mongodb;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import site.campingon.campingon.camp.dto.mongodb.SearchCriteriaDto;
import site.campingon.campingon.camp.dto.mongodb.SearchResultDto;
import site.campingon.campingon.camp.entity.mongodb.SearchInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MongoSearchClient {
    private final MongoTemplate mongoTemplate;
    private static final String SEARCH_INDEX = "searchIndex";
    private static final String AUTOCOMPLETE_INDEX = "autocompleteIndex";
    private static final String COLLECTION_NAME = "search_info";

    private static final String PROJECT_STAGE = """
        {
            $project: {
                camp_id: 1,
                name: 1,
                intro: 1,
                image_url: 1,
                address: 1,
                hashtags: 1,
                score: {$meta: 'searchScore'}
            }
        }""";

    public SearchResultDto search(SearchCriteriaDto criteria) {
        List<AggregationOperation> operations = new ArrayList<>();

        // Search Operation
        operations.add(context -> Document.parse(buildSearchQuery(criteria)));

        // Project Operation
        operations.add(context -> Document.parse(PROJECT_STAGE));

        // Facet Operation
        operations.add(context -> Document.parse(buildFacetStage(criteria.getPageable())));

        AggregationResults<Document> results = mongoTemplate.aggregate(
            Aggregation.newAggregation(operations),
            COLLECTION_NAME,
            Document.class
        );

        return processResults(results, criteria.getPageable());

        /*String mustClause = "";
        if (StringUtils.hasText(city)) {
            List<String> cityVariants = getCityVariants(city);
            List<String> phrases = new ArrayList<>();

            // city 검색을 위한 phrases
            phrases.addAll(cityVariants.stream()
                .map("""
                    {phrase: {
                        query: '%s',
                        path: 'address.city'
                    }}"""::formatted)
                .toList());

            //  state에서 제주시 추가 검색
            if (city.equals("제주특별자치도")) {
                phrases.add("""
                    {phrase: {
                        query: '제주시',
                        path: 'address.state'
                    }}""");
            }

            mustClause = """
                , must: [{
                    compound: {
                        should: [%s]
                    }
                }]""".formatted(String.join(",", phrases));
        }

        String searchQuery = """
            {
                $search: {
                    index: '%s',
                    compound: {
                        should: %s
                        %s
                    }
                }
            }""".formatted(SEARCH_INDEX, createShouldClauses(searchTerm, userKeywords), mustClause);

        String facetStage = """
            {
                $facet: {
                    results: [{$skip: %d}, {$limit: %d}],
                    total: [{$count: 'count'}]
                }
            }""".formatted(pageable.getOffset(), pageable.getPageSize());

        AggregationOperation searchOperation = context -> Document.parse(searchQuery);
        AggregationOperation projectOperation = context -> Document.parse(PROJECT_STAGE);
        AggregationOperation facetOperation = context -> Document.parse(facetStage);

        AggregationResults<Document> results = mongoTemplate.aggregate(
            Aggregation.newAggregation(
                searchOperation,
                projectOperation,
                facetOperation
            ),
            COLLECTION_NAME,
            Document.class
        );

        return processResults(results);*/
    }

    private String buildSearchQuery(SearchCriteriaDto criteria) {
        String mustClause = buildMustClause(criteria.getCity());
        String shouldClauses = buildShouldClauses(criteria.getSearchTerm(), criteria.getUserKeywords());

        return """
            {
                $search: {
                    index: '%s',
                    compound: {
                        should: %s
                        %s
                    }
                }
            }""".formatted(SEARCH_INDEX, shouldClauses, mustClause);
    }

    private String buildMustClause(String city) {
        if (!StringUtils.hasText(city)) {
            return "";
        }

        List<String> cityVariants = getCityVariants(city);

        // city 검색을 위한 phrases
        List<String> phrases = new ArrayList<>(cityVariants.stream()
            .map("""
                {phrase: {
                    query: '%s',
                    path: 'address.city'
                }}"""::formatted)
            .toList());

        // 제주특별자치도 특수 케이스
        if (city.equals("제주특별자치도")) {
            phrases.add("""
                {phrase: {
                    query: '제주시',
                    path: 'address.state'
                }}""");
        }

        return """
            , must: [{
                compound: {
                    should: [%s]
                }
            }]""".formatted(String.join(",", phrases));
    }

    private String buildShouldClauses(String searchTerm, List<String> userKeywords) {
        List<String> clauses = new ArrayList<>();

        // 검색어 관련 조건
        if (StringUtils.hasText(searchTerm)) {
            addSearchTermClauses(clauses, searchTerm);
        }

        // 사용자 키워드 관련 조건
        if (userKeywords != null && !userKeywords.isEmpty()) {
            userKeywords.stream()
                .filter(StringUtils::hasText)
                .forEach(keyword -> clauses.add(createSearchClause("hashtags", keyword, 2.0f, 0)));
        }

        return clauses.isEmpty() ? "[]" : "[" + String.join(",", clauses) + "]";
    }

    private void addSearchTermClauses(List<String> clauses, String searchTerm) {
        clauses.add(createSearchClause("name", searchTerm, 5.0f, 1));
        clauses.add(createSearchClause("name", searchTerm, 4.5f, 2));
        clauses.add(createSearchClause("hashtags", searchTerm, 4.0f, 1));
        clauses.add(createSearchClause("address.state", searchTerm, 3.0f, 1));
        clauses.add(createSearchClause("address.city", searchTerm, 2.5f, 0));
        clauses.add(createSearchClause("intro", searchTerm, 2.5f, 0));

        // 정규식 전방 일치 검색
        clauses.add("""
            {regex: {
                query: '%s.*',
                path: 'name',
                allowAnalyzedField: true,
                score: {boost: {value: 4}}
            }}""".formatted(searchTerm));
    }

    private String createSearchClause(String path, String query, float boost, int maxEdits) {
        return """
            {text: {
                query: '%s',
                path: '%s',
                score: {boost: {value: %.1f}}%s
            }}""".formatted(
            query,
            path,
            boost,
            maxEdits > 0 ? ", fuzzy: {maxEdits: " + maxEdits + "}" : ""
        );
    }


    private String buildFacetStage(Pageable pageable) {
        return """
            {
                $facet: {
                    results: [{$skip: %d}, {$limit: %d}],
                    total: [{$count: 'count'}]
                }
            }""".formatted(pageable.getOffset(), pageable.getPageSize());
    }


    private SearchResultDto processResults(AggregationResults<Document> results, Pageable pageable) {
        Document result = results.getUniqueMappedResult();
        if (result == null) {
            return SearchResultDto.of(List.of(), 0L, pageable);
        }

        /*List<Document> resultDocs = (List<Document>) result.get("results");
        List<Document> totalDocs = (List<Document>) result.get("total");*/
        List<Document> resultDocs = result.get("results", List.class);
        List<Document> totalDocs = result.get("total", List.class);

        if (resultDocs == null) {
            return SearchResultDto.of(List.of(), 0L, pageable);
        }

        List<SearchInfo> searchResults = resultDocs.stream()
            .map(doc -> mongoTemplate.getConverter().read(SearchInfo.class, doc))
            .toList();

        long total = 0L;
        if (totalDocs != null && !totalDocs.isEmpty()) {
            Number count = totalDocs.getFirst().get("count", Number.class);
            total = count != null ? count.longValue() : 0L;
        }

        // return new SearchResultDto(searchResults, total);
        return SearchResultDto.of(searchResults, total, pageable);
    }

    private List<String> getCityVariants(String city) {
        List<String> variants = new ArrayList<>();
        variants.add(city);  // 원본 도시명은 항상 포함

        // 특별시 케이스 (서울)
        if (city.endsWith("특별시")) {
            String base = city.replace("특별시", "");
            variants.add(base);        // 서울
            variants.add(base + "시"); // 서울시
        }

        // 광역시 케이스 (부산, 대구, 인천, 광주, 대전, 울산)
        else if (city.endsWith("광역시")) {
            String base = city.replace("광역시", "");
            variants.add(base);        // 부산
            variants.add(base + "시"); // 부산시
        }

        // 특별자치시 케이스 (세종)
        else if (city.endsWith("특별자치시")) {
            String base = city.replace("특별자치시", "");
            variants.add(base);        // 세종
            variants.add(base + "시"); // 세종시
        }

        // 도 케이스 (경기도, 강원도 등)
        else if (city.endsWith("도")) {
            String base = city.replace("도", "");
            variants.add(base);        // 경기
        }

        // 특별자치도 케이스 (제주, 강원)
        else if (city.endsWith("특별자치도")) {
            String base = city.replace("특별자치도", "");
            variants.add(base);         // 제주
            variants.add(base + "도");  // 제주도
            /*variants.add(base + "시");  // 제주시
            variants.add(base + "특별시");  // 제주특별시
            variants.add(base + "특별자치시");   // 제주특별자치시*/
        }

        return variants;
    }


    // 검색어 자동완성
    public List<String> getAutocompleteResults(String word) {
        return mongoTemplate.aggregate(
                Aggregation.newAggregation(
                    context -> Document.parse("""
                    {
                        $search: {
                            index: '%s',
                            autocomplete: {
                                query: '%s',
                                path: 'name',
                                fuzzy: {maxEdits: 1}
                            }
                        }
                    }""".formatted(AUTOCOMPLETE_INDEX, word)),
                    Aggregation.project("name"),
                    Aggregation.limit(8)
                ),
                COLLECTION_NAME,
                Document.class
            )
            .getMappedResults()
            .stream()
            .map(doc -> doc.getString("name"))
            .distinct()
            .collect(Collectors.toList());
    }

}
