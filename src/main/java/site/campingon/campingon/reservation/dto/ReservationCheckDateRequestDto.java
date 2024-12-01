package site.campingon.campingon.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCheckDateRequestDto {

    private Long campId;

    private LocalDateTime checkIn;

    private LocalDateTime checkOut;

}
