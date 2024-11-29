package site.campingon.campingon.reservation.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReservationStatus {

    RESERVED("예약완료"),
    CANCELED("예약취소"),
    COMPLETED("체크인완료"); // 체크인 이후로 (자동) 변경

    private final String status;

}