package site.campingon.campingon.review.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import site.campingon.campingon.camp.entity.Camp;
import site.campingon.campingon.camp.entity.CampSite;
import site.campingon.campingon.camp.repository.CampRepository;
import site.campingon.campingon.camp.repository.CampSiteRepository;
import site.campingon.campingon.reservation.entity.Reservation;
import site.campingon.campingon.reservation.repository.ReservationRepository;
import site.campingon.campingon.review.dto.ReviewCreateRequestDto;
import site.campingon.campingon.review.dto.ReviewResponseDto;
import site.campingon.campingon.review.dto.ReviewUpdateRequestDto;
import site.campingon.campingon.review.entity.Review;
import site.campingon.campingon.review.entity.ReviewImage;
import site.campingon.campingon.review.mapper.ReviewImageMapper;
import site.campingon.campingon.review.mapper.ReviewMapper;
import site.campingon.campingon.review.repository.ReviewImageRepository;
import site.campingon.campingon.review.repository.ReviewRepository;
import site.campingon.campingon.s3bucket.service.S3BucketService;
import site.campingon.campingon.user.repository.UserRepository;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ReservationRepository reservationRepository;
    private final CampRepository campRepository;
    private final CampSiteRepository campSiteRepository;
    private final ReviewMapper reviewMapper;
    private final ReviewImageMapper reviewImageMapper;
    private final S3BucketService s3BucketService;
    private final UserRepository userRepository;


    // 리뷰 생성
    @Transactional
    public ReviewResponseDto createReview(
            Long campId,
            Long reservationId,
            ReviewCreateRequestDto requestDto
    ) throws IOException {
        Camp camp = campRepository.findById(campId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 캠프장 ID입니다."));
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 예약 ID입니다."));

        // 3. ReviewCreateRequestDto를 Review 엔티티로 변환하고 저장
        Review review = reviewMapper.toEntity(requestDto, camp, reservation);
        Review savedReview = reviewRepository.save(review);

        List<String> uploadedUrls = s3BucketService.upload(requestDto.getS3Images(), "reviews/" + savedReview.getId());

        // 5. 업로드된 URL 리스트와 저장된 리뷰 데이터를 사용하여 ReviewImage 엔티티 리스트 생성
        List<ReviewImage> reviewImages = reviewImageMapper.toEntityList(uploadedUrls, savedReview);
        reviewImageRepository.saveAll(reviewImages);

        // 7. 저장된 Review 엔티티를 ReviewResponseDto로 변환하여 반환
        return reviewMapper.toResponseDto(savedReview);
    }
      // 추후 단건 예약에 대해 중복 리뷰 작성 불가 로직 추가 예정
//    // 예약이 완료된 상태를 기준으로 리뷰 작성
//    @Transactional
//    public ReviewResponseDto createReview(Long reservationId, ReviewCreateRequestDto requestDto) throws IOException {
//        // 1. 예약 정보 가져오기
//        Reservation reservation = reservationRepository.findById(reservationId)
//                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 예약 ID입니다."));
//
//        // 2. 예약 상태 확인
//        if (reservation.getStatus() != ReservationStatus.COMPLETED) {
//            throw new IllegalArgumentException("완료된 예약에 대해서만 리뷰를 작성할 수 있습니다.");
//        }
//
//        // 3. 기존 리뷰 여부 확인
//        boolean hasReview = reviewRepository.existsByReservationId(reservationId);
//        if (hasReview) {
//            throw new IllegalArgumentException("이미 이 예약에 대해 리뷰를 작성하셨습니다.");
//        }
//
//        // 4. 캠핑지 정보 가져오기
//        CampSite campSite = reservation.getCampSite();
//
//        // 5. 리뷰 생성
//        Review review = reviewMapper.toEntity(requestDto, camp, reservation);
//        Review savedReview = reviewRepository.save(review);
//
//        // 6. 이미지 업로드
//        List<String> uploadedUrls = s3BucketService.upload(requestDto.getS3Images(), "reviews/" + savedReview.getId());
//        List<ReviewImage> reviewImages = reviewImageMapper.toEntityList(uploadedUrls, savedReview);
//        reviewImageRepository.saveAll(reviewImages);
//
//        // 7. 응답 DTO 반환
//        return reviewMapper.toResponseDto(savedReview);
//    }


    // 리뷰 수정
    @Transactional
    public ReviewResponseDto updateReview(Long campId, Long reviewId, ReviewUpdateRequestDto requestDto) throws IOException {
        // 1. 기존 리뷰 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 리뷰 ID입니다."));

        // 2. 캠프 ID 확인
        if (!review.getCamp().getId().equals(campId)) {
            throw new IllegalArgumentException("리뷰가 해당 캠프에 속하지 않습니다.");
        }

        Review updatedReview = reviewMapper.updateFromRequest(review, requestDto);

        // 4. 이미지 업데이트
        List<MultipartFile> newImages = requestDto.getS3Images();
        if (newImages != null && !newImages.isEmpty()) {
            updateReviewImages(review, newImages);
        }

        // 5. 리뷰 저장
        reviewRepository.save(review);

        // 6. 응답 DTO 반환
        return reviewMapper.toResponseDto(review);
    }

    private void updateReviewImages(Review review, List<MultipartFile> newImages) throws IOException {
        // 1. 기존 이미지 삭제
        List<ReviewImage> existingImages = reviewImageRepository.findByReview(review);
        for (ReviewImage existingImage : existingImages) {
            s3BucketService.remove(existingImage.getImageUrl());
        }
        reviewImageRepository.deleteAll(existingImages);

        // 2. 새로운 이미지 업로드
        List<String> uploadedUrls = s3BucketService.upload(newImages, "reviews/" + review.getId());

        // 3. 새로운 이미지 엔티티 생성 및 저장
        List<ReviewImage> newImageEntities = reviewImageMapper.toEntities(review, uploadedUrls);
        reviewImageRepository.saveAll(newImageEntities);
    }

    // 리뷰 삭제
    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found with id: " + reviewId));

        List<ReviewImage> reviewImages = reviewImageRepository.findByReview(review);
        if (!reviewImages.isEmpty()) {
            reviewImageRepository.deleteAll(reviewImages);
            reviewImages.forEach(image -> s3BucketService.remove(image.getImageUrl()));
        }

        reviewRepository.delete(review);
    }

    // 캠핑장 id로 리뷰 조회
    public List<ReviewResponseDto> getReviewsByCampId(Long campId) {
        Camp camp = campRepository.findById(campId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 캠프장 ID입니다."));

        List<Review> reviews = reviewRepository.findByCampId(camp.getId());
        return reviewMapper.toResponseDtoList(reviews);
    }

    // 캠핑지 id로 리뷰 조회
    public List<ReviewResponseDto> getReviewsByCampSiteId(Long campSiteId) {
        CampSite campSite = campSiteRepository.findById(campSiteId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 캠핑지 ID입니다."));

        List<Review> reviews = reviewRepository.findByCampSiteId(campSite.getId());
        return reviewMapper.toResponseDtoList(reviews);
    }

    // 리뷰 추천 토글
    public boolean toggleRecommend(Long reviewId, Long userId) {
        // 리뷰 가져오기
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found with ID: " + reviewId));

        // 작성자 확인
        if (!review.getReservation().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("User is not authorized to toggle recommend for this review.");
        }
        Review updatedReview = reviewMapper.toUpdatedReview(review);
        reviewRepository.save(updatedReview);
        return updatedReview.isRecommend();
    }
}
