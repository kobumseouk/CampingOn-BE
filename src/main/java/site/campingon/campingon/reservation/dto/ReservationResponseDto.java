package site.campingon.campingon.reservation.dto;

import lombok.*;
import site.campingon.campingon.reservation.entity.CheckTime;
import site.campingon.campingon.reservation.entity.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@ToString
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponseDto {

    private Long id;

    private Long userId;

    private Long campSiteId;

    private LocalDate checkinDate;

    private LocalDate checkoutDate;

    private CheckTime checkinTime;

    private CheckTime checkoutTime;

    private int guestCnt;

    private ReservationStatus status;

    private int totalPrice;

    private CampResponseDto campResponseDto;

    private CampAddrResponseDto campAddrResponseDto;

}
