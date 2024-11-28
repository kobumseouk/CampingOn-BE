package site.campingon.campingon.reservation.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReservationStatus {

    RESERVED("예약완료"),
    CANCELED("예약취소"),
    COMPLETED("체크아웃완료"); // 체크아웃시간 이후로 (자동) 변경

    private final String status;
}