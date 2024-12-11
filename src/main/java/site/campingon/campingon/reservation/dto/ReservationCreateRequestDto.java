package site.campingon.campingon.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCreateRequestDto {

    private Long campId;

    private Long campSiteId;

    private LocalDate checkin;

    private LocalDate checkout;

    private int guestCnt;

    private int totalPrice;

}
