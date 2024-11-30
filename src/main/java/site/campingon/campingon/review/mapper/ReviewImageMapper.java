package site.campingon.campingon.review.mapper;

import org.mapstruct.*;
import site.campingon.campingon.review.entity.Review;
import site.campingon.campingon.review.entity.ReviewImage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReviewImageMapper {

    // 리뷰 생성 매퍼
    default ReviewImage toReviewImage(String url, Review review) {
        if (url == null || review == null) {
            throw new IllegalArgumentException("url 또는 review가 null일 수 없습니다.");
        }

        // 빌더 패턴 사용
        return ReviewImage.builder()
                .imageUrl(url)  // URL 설정
                .review(review) // Review 설정
                .build();
    }

    default List<ReviewImage> toEntityList(List<String> uploadedUrls, Review review) {
        if (uploadedUrls == null || uploadedUrls.isEmpty()) {
            return Collections.emptyList(); // URL이 비어 있으면 빈 리스트 반환
        }
        return uploadedUrls.stream()
                .map(url -> toReviewImage(url, review)) // 각 URL을 ReviewImage로 변환
                .collect(Collectors.toList()); // 리스트로 수집
    }

    // 리뷰 이미지 수정 매퍼
    // 커스텀 메서드로 리스트 매핑
    @Mapping(target = "review", source = "review")
    @Mapping(target = "imageUrl", source = "url")
    ReviewImage toEntity(Review review, String url);

    // List 매핑
    @Named("toEntities")
    default List<ReviewImage> toEntities(Review review, List<String> uploadedUrls) {
        return uploadedUrls.stream()
                .map(url -> toEntity(review, url)) // 개별 매핑 메서드 호출
                .collect(Collectors.toList());
    }
}
