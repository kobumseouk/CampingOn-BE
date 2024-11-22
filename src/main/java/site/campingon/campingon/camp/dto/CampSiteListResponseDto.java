package site.campingon.campingon.camp.dto;


import lombok.*;

import java.time.LocalTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampSiteListResponseDto {
  private Long siteId;
  private String roomName;
  private Integer maxPeople;
  private Integer price;
  private String imageUrl;
  private String indoor_facility;

  private LocalTime checkInTime;   // 15:00 고정
  private LocalTime checkOutTime;   // 11:00 고정
}
