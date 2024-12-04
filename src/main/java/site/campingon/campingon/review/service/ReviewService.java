package site.campingon.campingon.review.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static site.campingon.campingon.common.exception.ErrorCode.*;
import static site.campingon.campingon.common.exception.ErrorCode.REVIEW_ALREADY_SUBMITTED;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ReservationRepository reservationRepository;
    private final CampRepository campRepository;
    private final ReviewMapper reviewMapper;
    private final ReviewImageMapper reviewImageMapper;
    private final S3BucketService s3BucketService;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final int MAX_FILES_COUNT = 5; // 최대 파일 개수 제한
    private static final Set<String> ALLOWED_TYPES = Set.of(
        "image/jpeg",
        "image/png",
        "image/gif"
    );

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

        // 리뷰 엔티티 생성 및 저장
        Review review = reviewMapper.toEntity(requestDto, camp, reservation);
        Review savedReview = reviewRepository.save(review);

        // 이미지가 있는 경우에만 처리
        if (requestDto.getS3Images() != null && !requestDto.getS3Images().isEmpty()) {
            // 이미지 파일의 타입, 크기, 개수 검증
            validateImages(requestDto.getS3Images());
            try {
                List<String> uploadedUrls = s3BucketService.upload(requestDto.getS3Images(), "reviews/" + savedReview.getId());
                List<ReviewImage> reviewImages = reviewImageMapper.toEntityList(uploadedUrls, savedReview);
                reviewImageRepository.saveAll(reviewImages);
            } catch (IOException e) {
                throw new GlobalException(FILE_UPLOAD_FAILED);
            }
        }

        // 저장된 Review 엔티티를 ReviewResponseDto로 반환
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

    private void updateReviewImages(Review review, List<MultipartFile> newImages) {
        // 이미지가 없는 경우 기존 이미지만 삭제하고 종료
        if (newImages == null || newImages.isEmpty()) {
            List<ReviewImage> existingImages = reviewImageRepository.findByReview(review);
            if (!existingImages.isEmpty()) {
                existingImages.forEach(image -> {
                    try {
                        s3BucketService.remove(image.getImageUrl());
                    } catch (Exception e) {
                        log.error("기존 이미지 삭제 실패: {}", image.getImageUrl(), e);
                    }
                });
                reviewImageRepository.deleteAll(existingImages);
            }
            return;
        }

        // 이미지 파일의 타입, 크기, 개수 검증
        validateImages(newImages);

        List<String> uploadedUrls = new ArrayList<>();
        List<ReviewImage> existingImages = reviewImageRepository.findByReview(review);

        try {
            // 새 이미지 업로드 시도
            uploadedUrls = s3BucketService.upload(newImages, "reviews/" + review.getId());

            // 새 이미지 업로드 성공 시 기존 이미지 삭제
            for (ReviewImage existingImage : existingImages) {
                try {
                    s3BucketService.remove(existingImage.getImageUrl());
                } catch (Exception e) {
                    log.error("Failed to remove existing image: {}", existingImage.getImageUrl(), e);
                }
            }

            reviewImageRepository.deleteAll(existingImages);

            // 새 이미지 엔티티 저장
            List<ReviewImage> newImageEntities = reviewImageMapper.toEntities(review, uploadedUrls);
            reviewImageRepository.saveAll(newImageEntities);
        } catch (IOException e) {
            // 실패 시 새로 업로드된 이미지들 롤백
            uploadedUrls.forEach(url -> {
                try {
                    s3BucketService.remove(url);
                } catch (Exception ex) {
                    log.error("Failed to remove uploaded file during rollback: {}", url, ex);
                }
            });
            throw new GlobalException(FILE_UPLOAD_FAILED);
        }
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

    // 허용된 이미지 파일 타입 및 파일 개수 검증
    private void validateImages(List<MultipartFile> images) {
        // 이미지 파일 개수 제한
        if (images.size() > MAX_FILES_COUNT) {
            throw new GlobalException(FILE_COUNT_EXCEEDED);
        }

        for (MultipartFile image : images) {
            // 파일 크기 검증
            if (image.getSize() > MAX_FILE_SIZE) {
                throw new GlobalException(FILE_SIZE_EXCEEDED);
            }

            // 파일 타입 검증
            String contentType = image.getContentType();
            if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
                throw new GlobalException(INVALID_FILE_TYPE);
            }
        }
    }
}