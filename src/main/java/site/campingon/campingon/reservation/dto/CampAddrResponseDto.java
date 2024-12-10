package site.campingon.campingon.reservation.dto;

import lombok.*;

@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampAddrResponseDto {

    public String streetAddr;

/*
    // 유지보수를 고려해 주석 처리
    private String city;
    private String state;
    private String zipcode;
    private String detailedAddr;
*/

}
