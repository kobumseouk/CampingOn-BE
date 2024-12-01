package site.campingon.campingon.reservation.dto;

import lombok.*;
import site.campingon.campingon.reservation.entity.ReservationStatus;
import java.time.LocalDateTime;

@Getter
@ToString
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponseDto {

    private Long id;

    private Long userId;

    private Long campSiteId;

    private LocalDateTime checkIn;

    private LocalDateTime checkOut;

    private int guestCnt;

    private ReservationStatus status;

    private int totalPrice;

    private CampResponseDto campResponseDto;

    private CampAddrResponseDto campAddrResponseDto;

}
