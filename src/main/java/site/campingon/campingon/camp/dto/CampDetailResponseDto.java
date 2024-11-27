package site.campingon.campingon.camp.dto;

import lombok.*;
import site.campingon.campingon.camp.entity.CampAddr;
import site.campingon.campingon.camp.entity.CampInfo;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampDetailResponseDto {
  private Long id;
  private String name;
  private String tel;
  private String lineIntro;
  private String homepage;   // 홈페이지 사용 고려
  private String outdoorFacility;   // 부대시설
  private String indutys;  // 업종

  private CampAddr campAddr;  // 경도, 위도, 도로명 주소

  private List<String> images;   // 캠핑장 이미지(썸네일 포함) <- Camp_image 엔티티

  private CampInfo campInfo;  // 추천 수, 찜 수
}
