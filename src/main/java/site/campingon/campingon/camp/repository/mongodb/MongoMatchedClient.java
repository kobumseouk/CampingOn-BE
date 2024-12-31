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
import site.campingon.campingon.camp.dto.mongodb.SearchResultDto;
import site.campingon.campingon.camp.entity.mongodb.SearchInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class MongoMatchedClient {
    private final MongoTemplate mongoTemplate;
    private static final String INDEX_NAME = "searchIndex";
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
                matchCount: 1,   // 매칭된 키워드 수 추가
                score: {$meta: 'searchScore'}
            }
        }""";

    public SearchResultDto getMatchedCamps(List<String> userKeywords, Pageable pageable) {
        /*String searchQuery = """
            {
                $search: {
                    index: '%s',
                    compound: {
                        should: [%s]
                    }
                }
            )""".formatted(INDEX_NAME, createKeywordMatchClauses(userKeywords));

        // 매칭된 키워드 수를 계산하는 스테이지
        String matchCountStage = """
            {$addFields: {
                matchCount: {
                    $size: {
                        $setIntersection: ["$hashtags", [%s]]
                    }
                }
            }}""".formatted(userKeywords.stream()
                .map(keyword -> "\"" + keyword + "\"")
                .collect(Collectors.joining(", ")));

        // 매칭된 키워드가 1개 이상인 것만 필터링
        String filterStage = "{$match: {matchCount: {$gt: 0}}}";

        // 매칭 수와 검색 점수로 정렬
        String sortStage = "{$sort: {matchCount: -1, score: -1}}";

        String facetStage = """
            {$facet: {
                results: [{$skip: %d}, {$limit: %d}],
                total: [{$count: 'count'}]
            }}""".formatted(pageable.getOffset(), pageable.getPageSize());

        AggregationOperation searchOperation = context -> Document.parse(searchQuery);
        AggregationOperation matchCountOperation = context -> Document.parse(matchCountStage);
        AggregationOperation filterOperation = context -> Document.parse(filterStage);
        AggregationOperation sortOperation = context -> Document.parse(sortStage);
        AggregationOperation projectOperation = context -> Document.parse(PROJECT_STAGE);
        AggregationOperation facetOperation = context -> Document.parse(facetStage);

        AggregationResults<Document> results = mongoTemplate.aggregate(
            Aggregation.newAggregation(
                searchOperation,
                matchCountOperation,
                filterOperation,
                sortOperation,
                projectOperation,
                facetOperation
            ),
            COLLECTION_NAME,
            Document.class
        );

        return processResults(results);*/

        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(context -> Document.parse(buildSearchQuery(userKeywords)));
        operations.add(context -> Document.parse(buildMatchCountStage(userKeywords)));
        operations.add(context -> Document.parse(buildFilterStage()));
        operations.add(context -> Document.parse(buildSortStage()));
        operations.add(context -> Document.parse(PROJECT_STAGE));
        operations.add(context -> Document.parse(buildFacetStage(pageable)));

        AggregationResults<Document> results = mongoTemplate.aggregate(
            Aggregation.newAggregation(operations),
            COLLECTION_NAME,
            Document.class
        );

        return processResults(results, pageable);
    }

    private String buildSearchQuery(List<String> userKeywords) {
        return """
            {
                $search: {
                    index: '%s',
                    compound: {
                        should: [%s]
                    }
                }
            }""".formatted(INDEX_NAME, createKeywordMatchClauses(userKeywords));
    }

    private String buildMatchCountStage(List<String> userKeywords) {
        String keywordArray = userKeywords.stream()
            .map(keyword -> "\"" + keyword + "\"")
            .collect(Collectors.joining(", "));

        return """
            {
                $addFields: {
                    matchCount: {
                        $size: {
                            $setIntersection: ["$hashtags", [%s]]
                        }
                    }
                }
            }""".formatted(keywordArray);
    }

    // 매칭된 키워드가 1개 이상인 것만 필터링
    private String buildFilterStage() {
        return "{$match: {matchCount: {$gt: 0}}}";
    }
    // 매칭 수와 검색 점수로 정렬
    private String buildSortStage() {
        return "{$sort: {matchCount: -1, score: -1}}";
    }

    // 페이지 카운트
    private String buildFacetStage(Pageable pageable) {
        return """
            {
                $facet: {
                    results: [{$skip: %d}, {$limit: %d}],
                    total: [{$count: 'count'}]
                }
            }""".formatted(pageable.getOffset(), pageable.getPageSize());
    }


    private String createKeywordMatchClauses(List<String> userKeywords) {
        if (userKeywords == null || userKeywords.isEmpty()) {
            return "[]";
        }

        List<String> clauses = new ArrayList<>();

        // 첫 번째 키워드 - 가장 높은 가중치
        if (StringUtils.hasText(userKeywords.getFirst())) {
            clauses.add("""
                {text: {
                    query: '%s',
                    path: 'hashtags',
                    score: {boost: {value: 6.0}}
                }}""".formatted(userKeywords.getFirst()));
        }

        // 두 번째 키워드
        if (userKeywords.size() >= 2 && StringUtils.hasText(userKeywords.get(1))) {
            clauses.add("""
                {text: {
                    query: '%s',
                    path: 'hashtags',
                    score: {boost: {value: 5.0}}
                }}""".formatted(userKeywords.get(1)));
        }

        // 기본 가중치
        if (userKeywords.size() > 2) {
            List<String> remainingClauses = userKeywords.stream()
                .skip(2)  // 첫 두 개의 키워드는 건너뛰기
                .filter(StringUtils::hasText)
                .map("""
                    {text: {
                        query: '%s',
                        path: 'hashtags',
                        score: {boost: {value: 4.0}}
                    }}"""::formatted)
                .toList();

            clauses.addAll(remainingClauses);
        }

        return String.join(",", clauses);
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

}
