package site.campingon.campingon.reservation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import site.campingon.campingon.reservation.dto.ReservationCancelRequestDto;
import site.campingon.campingon.reservation.dto.ReservationCreateRequestDto;
import site.campingon.campingon.reservation.dto.ReservationResponseDto;
import site.campingon.campingon.reservation.dto.ReviewResponseDto;
import site.campingon.campingon.reservation.entity.Reservation;
import site.campingon.campingon.review.entity.Review;

// import site.campingon.campingon.review.entity.ReviewImage;  // 추가
import java.util.ArrayList;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
    imports = {
        Collectors.class,
        ArrayList.class
    })
public interface ReservationMapper {
    @Mapping(source = "checkin", target = "checkinDate")
    @Mapping(source = "checkout", target = "checkoutDate")
    Reservation toEntity(ReservationCreateRequestDto reservationRequest);

    Reservation toEntity(ReservationCancelRequestDto reservationRequest);

    @Mapping(source = "campSite", target = "campSiteResponseDto")
    @Mapping(source = "camp.campAddr", target = "campAddrResponseDto")
    @Mapping(source = "camp", target = "campResponseDto")
    @Mapping(source = "review", target = "reviewDto", qualifiedByName = "reviewToDto")
    ReservationResponseDto toResponse(Reservation reservation);

    @Named("reviewToDto")
    @Mapping(target = "images", expression = "java(review.getReviewImages() != null ? review.getReviewImages().stream().map(image -> image.getImageUrl()).collect(Collectors.toList()) : new ArrayList<>())")
    ReviewResponseDto toResponseDto(Review review);
}