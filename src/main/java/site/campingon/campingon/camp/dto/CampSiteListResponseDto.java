package site.campingon.campingon.camp.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import site.campingon.campingon.camp.entity.Induty;

import java.time.LocalDateTime;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampSiteListResponseDto {
  private Long siteId;
  private Integer maximumPeople;
  private Integer price;
  private Induty siteType;
  private String indoorFacility;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
  private LocalDateTime checkinTime;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
  private LocalDateTime checkoutTime;

}