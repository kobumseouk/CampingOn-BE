package site.campingon.campingon.review.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import site.campingon.campingon.camp.entity.Camp;
import site.campingon.campingon.camp.entity.CampSite;
import site.campingon.campingon.camp.repository.CampRepository;
import site.campingon.campingon.camp.repository.CampSiteRepository;
import site.campingon.campingon.common.exception.GlobalException;
import site.campingon.campingon.common.s3bucket.service.S3BucketService;
import site.campingon.campingon.reservation.entity.Reservation;
import site.campingon.campingon.reservation.entity.ReservationStatus;
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
import site.campingon.campingon.user.repository.UserRepository;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static site.campingon.campingon.common.exception.ErrorCode.*;
import static site.campingon.campingon.common.exception.ErrorCode.REVIEW_ALREADY_SUBMITTED;

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


    // 리뷰 작성
    @Transactional
    public ReviewResponseDto createReview(
            Long campId,
            Long reservationId,
            ReviewCreateRequestDto requestDto
    ) throws IOException {
        Camp camp = campRepository.findById(campId)
                .orElseThrow(() -> new GlobalException(CAMP_NOT_FOUND_BY_ID));
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new GlobalException(RESERVATION_NOT_FOUND_BY_ID));
        if (reservation.getStatus() != ReservationStatus.COMPLETED) {
            throw new GlobalException((RESERVATION_NOT_COMPLETED_FOR_REVIEW));
        }
        boolean hasReview = reviewRepository.existsByReservationId(reservationId);
        if (hasReview) {
            throw new GlobalException((REVIEW_ALREADY_SUBMITTED));
        }
        Review review = reviewMapper.toEntity(requestDto, camp, reservation);
        Review savedReview = reviewRepository.save(review);

        List<String> uploadedUrls = s3BucketService.upload(requestDto.getS3Images(), "reviews/" + savedReview.getId());

        // 5. 업로드된 URL 리스트와 저장된 리뷰 데이터를 사용하여 ReviewImage 엔티티 리스트 생성
        List<ReviewImage> reviewImages = reviewImageMapper.toEntityList(uploadedUrls, savedReview);
        reviewImageRepository.saveAll(reviewImages);

        // 7. 저장된 Review 엔티티를 ReviewResponseDto로 변환하여 반환
        return reviewMapper.toResponseDto(savedReview);
    }

    // 리뷰 수정
    @Transactional
    public ReviewResponseDto updateReview(Long campId, Long reviewId, ReviewUpdateRequestDto requestDto) throws IOException {
        // 1. 기존 리뷰 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new GlobalException(REVIEW_NOT_FOUND_BY_ID));

        // 2. 캠프 ID 확인
        if (!review.getCamp().getId().equals(campId)) {
            throw new GlobalException(REVIEW_NOT_IN_CAMP);
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
                .orElseThrow(() -> new GlobalException(REVIEW_NOT_FOUND_BY_ID));

        List<ReviewImage> reviewImages = reviewImageRepository.findByReview(review);
        if (!reviewImages.isEmpty()) {
            reviewImageRepository.deleteAll(reviewImages);
            reviewImages.forEach(image -> s3BucketService.remove(image.getImageUrl()));
        }

        reviewRepository.delete(review);
    }

    // 캠핑장 id로 리뷰 목록 조회
    public List<ReviewResponseDto> getReviewsByCampId(Long campId) {
        Camp camp = campRepository.findById(campId)
                .orElseThrow(() -> new GlobalException(CAMP_NOT_FOUND_BY_ID));

        List<Review> reviews = reviewRepository.findByCampId(camp.getId());
        return reviewMapper.toResponseDtoList(reviews);
    }

    // 리뷰 상세 조회
    public ReviewResponseDto getReviewById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new GlobalException(REVIEW_NOT_FOUND_BY_ID));
        return reviewMapper.toResponseDto(review);
    }

    // 리뷰 추천 토글
    @Transactional
    public boolean toggleRecommend(Long reviewId, Long userId) {
        // 리뷰 가져오기
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new GlobalException(REVIEW_NOT_FOUND_BY_ID));

        // 작성자 확인
        if (!review.getReservation().getUser().getId().equals(userId)) {
            throw new GlobalException(USER_NOT_FOUND_BY_ID);
        }
        Review updatedReview = reviewMapper.toUpdatedReview(review);
        reviewRepository.save(updatedReview);
        return updatedReview.isRecommend();
    }
}