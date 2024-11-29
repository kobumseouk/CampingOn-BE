package site.campingon.campingon.reservation.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCreatedRequestDto {

    private Long userId;

    private Long campSiteId;

    private LocalDateTime checkIn;

    private LocalDateTime checkOut;

    private int guestCnt;

    private int totalPrice;

}
