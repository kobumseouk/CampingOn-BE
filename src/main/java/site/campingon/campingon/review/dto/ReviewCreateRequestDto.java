package site.campingon.campingon.review.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreateRequestDto {
    private Long campId; // 캠핑장 ID
    private Long reservationId; // 예약 ID
    private Long userId; // 작성자 ID
    private String content; // 리뷰 내용
    private boolean isRecommend; // 추천 여부
    private List<MultipartFile> s3Images;
}