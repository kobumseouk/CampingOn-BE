package site.campingon.campingon.camp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampInfoDto {
    private Long id;             // 캠프 정보 ID
    private Integer recommendCnt; // 추천 수
    private Integer bookmarkCnt;  // 찜 수
}