package site.campingon.campingon.camp.dto;


import lombok.*;
import site.campingon.campingon.camp.entity.Induty;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampSiteListResponseDto {
  private Long siteId;
//  private Integer maximumPeople;
//  private Integer price;
  private Induty siteType;
  private String indoor_facility;

  @Builder.Default  // 기본값 자동 설정
  private LocalTime checkInTime = LocalTime.of(15, 0);  // 15:00

  @Builder.Default  // 기본값 자동 설정
  private LocalTime checkOutTime = LocalTime.of(11, 0);  // 11:00

}