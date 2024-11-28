package site.campingon.campingon.common.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import site.campingon.campingon.common.exception.ErrorCode;
import site.campingon.campingon.common.exception.GlobalException;
import site.campingon.campingon.reservation.entity.ReservationStatus;

import java.util.Arrays;

@Converter(autoApply = true)
public class ReservationStatusConverter implements AttributeConverter<ReservationStatus, String> {

    // 한글명으로 저장 시 (ENUM → String)
    @Override
    public String convertToDatabaseColumn(ReservationStatus reservationStatus) {

        return reservationStatus.getStatus();
    }

    // DB 에서 조회 시 (String -> ENUM)
    @Override
    public ReservationStatus convertToEntityAttribute(String dbData) {

        return Arrays.stream(ReservationStatus.values())
                .filter(i -> i.getStatus().equals(dbData))
                .findFirst()
                .orElseThrow(() -> new GlobalException(ErrorCode.RESERVATION_STATUS_NOT_FOUND));
    }
}