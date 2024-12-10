package site.campingon.campingon.review.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import site.campingon.campingon.camp.entity.Camp;
import site.campingon.campingon.reservation.entity.Reservation;
import site.campingon.campingon.review.dto.ReviewCreateRequestDto;
import site.campingon.campingon.review.dto.ReviewResponseDto;
import site.campingon.campingon.review.dto.ReviewUpdateRequestDto;
import site.campingon.campingon.review.entity.Review;

// import site.campingon.campingon.review.entity.ReviewImage;
import java.util.ArrayList;
import java.util.stream.Collectors;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    imports = {
        Collectors.class,
        ArrayList.class
    })
public interface ReviewMapper {

    // 리뷰 생성 매퍼
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "reservation.user")
    @Mapping(target = "campSite", source = "reservation.campSite")
    @Mapping(target = "reservation", source = "reservation")
    @Mapping(target = "camp", source = "camp")
    @Mapping(target = "isRecommend", source = "requestDto.recommended")
    Review toEntity(ReviewCreateRequestDto requestDto, Camp camp, Reservation reservation);

    @Mapping(target = "reviewId", source = "id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "recommended", source = "recommend")
    @Mapping(target = "images", expression = "java(review.getReviewImages() != null ? review.getReviewImages().stream().map(image -> image.getImageUrl()).collect(Collectors.toList()) : new ArrayList<>())")
    ReviewResponseDto toResponseDto(Review review);

    /*// 리뷰 수정 매퍼
    @Mapping(target = "title", source = "requestDto.title", defaultValue = "review.title")
    @Mapping(target = "content", source = "requestDto.content", defaultValue = "review.content")
    Review updateFromRequest(Review review, ReviewUpdateRequestDto requestDto);*/

//    List<ReviewResponseDto> toResponseDtoList(List<Review> reviews);

    @Mapping(target = "isRecommend", expression = "java(!review.isRecommend())")
    Review toUpdatedReview(Review review);

    @Mapping(target = "reviewId", source = "id")
    @Mapping(target = "campId", source = "camp.id")
    @Mapping(target = "reservationId", source = "reservation.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "recommended", source = "recommend")
    @Mapping(target = "images", expression = "java(review.getReviewImages() != null ? review.getReviewImages().stream().map(image -> image.getImageUrl()).toList() : java.util.List.of())")
    ReviewResponseDto toDto(Review review);
}
