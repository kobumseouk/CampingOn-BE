package site.campingon.campingon.reservation.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@AllArgsConstructor
public enum CheckTime {
    CHECKIN(LocalTime.of(15, 0)),
    CHECKOUT(LocalTime.of(11, 0));

    private final LocalTime time;

}