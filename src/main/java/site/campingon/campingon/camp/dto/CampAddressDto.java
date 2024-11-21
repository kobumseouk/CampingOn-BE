package site.campingon.campingon.camp.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampAddressDto {
  private String city;   // 경기도
  private String state;   // 하남시
  private String streetAddr;   // 미사대로
  private String detailedAddr;   // 750
  private String zipcode;

  // 도로명 주소 조합 (예: 경기도 하남시 미사대로 750)
  public String getFullAddress() {
    return String.format("%s %s %s %s", city, state, streetAddr, detailedAddr);
  }
}
