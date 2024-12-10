package site.campingon.campingon.camp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import site.campingon.campingon.camp.entity.Induty;

import java.time.LocalTime;

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

  @Builder.Default
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
  private LocalTime checkinTime =  LocalTime.of(15, 0);

  @Builder.Default
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
  private LocalTime checkoutTime = LocalTime.of(11, 0);
}