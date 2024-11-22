package site.campingon.campingon.camp.dto;


import lombok.*;

import java.util.List;

@Getter
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

  private CampAddrDto address;   // 도로명 주소

  private List<String> images;   // 캠핑장 이미지(썸네일 포함) <- Camp_image 엔티티

  private List<String> keywords;   // 캠핑장 키워드 <- Camp_Keyword 엔티티

  private int recommendCnt;  // 추천 수 <- Camp_Info 엔티티
  private int likeCnt;  // 찜 수 <- Camp_Info 엔티티
}
