package site.campingon.campingon.camp.dto;

import lombok.*;
import site.campingon.campingon.camp.entity.Induty;

import java.time.LocalTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CampSiteResponseDto {
    private Long siteId;              // 캠핑지 ID
//    private Integer maximumPeople;        // 최대 수용 인원
//    private Integer price;            // 가격
    private String indoorFacility;    // 실내 시설 설명
    private Induty siteType;              // 업종 구분
    @Builder.Default
//    private boolean isAvailable = false;
    private boolean isAvailable = false;

    private LocalTime checkInTime;    // 체크인 시간
    private LocalTime checkOutTime;   // 체크아웃 시간
}