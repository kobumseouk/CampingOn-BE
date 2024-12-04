package site.campingon.campingon.camp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampAddrDto {
    private Long campAddrId;    // 주소 ID
    private String city;        // 도/광역시
    private String state;       // 시/군/구
    private String zipcode;     // 우편번호
    private String streetAddr;  // 도로명 주소
    private String detailedAddr;// 상세 주소
    private double latitude;    // 위도
    private double longitude;   // 경도
}
