package site.campingon.campingon.reservation.dto;

import lombok.*;

import java.util.List;

@Getter
@ToString
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ReservedCampSiteIdListResponseDto {

    private List<Long> campSiteId;

}
