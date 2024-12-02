package site.campingon.campingon.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCreateRequestDto {

    private Long campId;

    private Long campSiteId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkIn;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkOut;

    private int guestCnt;

    private int totalPrice;

}
