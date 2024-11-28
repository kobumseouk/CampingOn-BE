package site.campingon.campingon.camp.dto.admin;

import jakarta.validation.constraints.*;
import lombok.*;
import site.campingon.campingon.camp.entity.Induty;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampSiteUpdateRequestDto {

//    @NotNull(message = "최대 수용 인원은 필수입니다.")
//    @Min(value = 1, message = "최대 수용 인원은 최소 1명 이상이어야 합니다.")
//    @Max(value = 10, message = "최대 수용 인원은 10명을 초과할 수 없습니다.")
//    private Integer maximumPeople;
//
//    @NotNull(message = "가격은 필수입니다.")
//    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
//    private Integer price;

    @NotNull(message = "캠핑 유형은 필수입니다.")
    private Induty siteType;

    @Size(max = 255, message = "내부 시설 정보는 최대 255자까지 가능합니다.")
    private String indoorFacility;

    private boolean isAvailable;
}