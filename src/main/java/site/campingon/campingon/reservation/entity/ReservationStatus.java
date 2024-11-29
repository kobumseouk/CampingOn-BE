package site.campingon.campingon.reservation.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ReservationStatus {

    RESERVED("예약완료"),
    CANCELED("예약취소"),
    COMPLETED("체크인완료"); // 체크인 이후로 (자동) 변경

    private final String status;

    // JSON 변환 시 한글값 사용
    @JsonValue
    public String getStatus() {
        return this.status;
    }

}