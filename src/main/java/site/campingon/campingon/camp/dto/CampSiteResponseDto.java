package site.campingon.campingon.camp.dto;

import lombok.*;
import site.campingon.campingon.camp.entity.Induty;

import java.time.LocalTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CampSiteResponseDto {
    private Long siteId;
    private Integer maximumPeople;
    private Integer price;
    private String indoorFacility;
    private Induty siteType;
    private boolean isAvailable;

    private LocalTime checkinTime;
    private LocalTime checkoutTime;
}