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
  private Integer maximumPeople;
  private Integer price;
  private Induty siteType;
  private String indoorFacility;

  private LocalTime checkInTime;

  private LocalTime checkOutTime;

}