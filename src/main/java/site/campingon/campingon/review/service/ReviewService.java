package site.campingon.campingon.review.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    ) {
        Camp camp = campRepository.findById(campId)
            .orElseThrow(() -> new GlobalException(CAMP_NOT_FOUND_BY_ID));
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new GlobalException(RESERVATION_NOT_FOUND_BY_ID));
        if (reservation.getStatus() != ReservationStatus.COMPLETED) {
            throw new GlobalException(RESERVATION_NOT_COMPLETED_FOR_REVIEW);
        }

        boolean hasReview = reviewRepository.existsByReservationId(reservationId);
        if (hasReview) {
            throw new GlobalException(REVIEW_ALREADY_SUBMITTED);
        }

        // 리뷰 엔티티 생성 및 저장
        Review review = reviewMapper.toEntity(requestDto, camp, reservation);
        Review savedReview = reviewRepository.save(review);

        // 이미지가 있는 경우에만 처리
        List<ReviewImage> reviewImages = processReviewImages(requestDto.getS3Images(), savedReview);
        if (!reviewImages.isEmpty()) {
            reviewImageRepository.saveAll(reviewImages);
        }

        // 저장된 Review 엔티티를 ReviewResponseDto로 반환
        return reviewMapper.toResponseDto(savedReview);
    }

    // 이미지 처리를 위한 private 메서드
    private List<ReviewImage> processReviewImages(List<MultipartFile> images, Review review) {
        if (images == null || images.isEmpty()) {
            return new ArrayList<>();
        }

        // 이미지 유효성 검증
        validateImages(images);

        // 이미지 업로드 및 엔티티 변환
        try {
            List<String> uploadedUrls = s3BucketService.upload(images, "reviews/" + review.getId());
            return reviewImageMapper.toEntityList(uploadedUrls, review);
        } catch (Exception e) {
            throw new GlobalException(FILE_UPLOAD_FAILED);
        }
    }

    /*// 리뷰 수정
    @Transactional
    public ReviewResponseDto updateReview(Long campId, Long reviewId, ReviewUpdateRequestDto requestDto) {
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
        reviewRepository.save(updatedReview);

        // 6. 응답 DTO 반환
        return reviewMapper.toResponseDto(updatedReview);
    }

    private void updateReviewImages(Review review, List<MultipartFile> newImages) {
        // 기존 이미지 처리
        List<ReviewImage> existingImages = reviewImageRepository.findByReview(review);
        if (existingImages != null && !existingImages.isEmpty()) {
            List<String> existingUrls = existingImages.stream()
                .map(ReviewImage::getImageUrl)
                .collect(Collectors.toList());
            s3BucketService.remove(existingUrls);
            reviewImageRepository.deleteAll(existingImages);
        }

        // 새 이미지 처리 - processReviewImages
        List<ReviewImage> newReviewImages = processReviewImages(newImages, review);
        if (!newReviewImages.isEmpty()) {
            reviewImageRepository.saveAll(newReviewImages);
        }
    }

    // 리뷰 삭제
    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new GlobalException(REVIEW_NOT_FOUND_BY_ID));

        try {
            moveReviewImagesToDeletedFolder(review);
        } catch (Exception e) {
            log.error("Failed to move review images: reviewId={}", reviewId, e);
            throw new GlobalException(FILE_MOVE_FAILED);
        }

        // 삭제일 등록
        review.softDelete();

        reviewRepository.save(review);
    }

    // 이미지는 보존(6개월 후 삭제), deleted 폴더로 이동
    private void moveReviewImagesToDeletedFolder(Review review) {
        List<ReviewImage> reviewImages = reviewImageRepository.findByReview(review);
        for (ReviewImage image : reviewImages) {
            String newUrl = "deleted/reviews/" + review.getId() + "/" +
                image.getImageUrl().substring(image.getImageUrl().lastIndexOf('/') + 1);
            try {
                s3BucketService.moveObject(image.getImageUrl(), newUrl);
                image.updateImageUrl(newUrl);  // ReviewImage에 새로운 메서드 필요
            } catch (Exception e) {
                log.error("Failed to move image to deleted folder: {}", image.getImageUrl(), e);
            }
        }
    }*/

    // 캠핑장 id로 리뷰 목록 조회
    public Page<ReviewResponseDto> getReviewsByCampId(Long campId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByCampId(campId, pageable);
        return reviews.map(reviewMapper::toDto);
    }

    // 리뷰 상세 조회
    public ReviewResponseDto getReviewById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new GlobalException(REVIEW_NOT_FOUND_BY_ID));
        return reviewMapper.toResponseDto(review);
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