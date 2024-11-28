package site.campingon.campingon.reservation.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReservationStatus {

    RESERVED("예약완료"),
    CANCELED("예약취소"),
    PAID("결제완료");

    private final String status;
}