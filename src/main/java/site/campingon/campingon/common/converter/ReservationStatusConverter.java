package site.campingon.campingon.common.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import site.campingon.campingon.common.exception.ErrorCode;
import site.campingon.campingon.common.exception.GlobalException;
import site.campingon.campingon.reservation.entity.ReservationStatus;

import java.util.Arrays;

@Converter(autoApply = true)
public class ReservationStatusConverter implements AttributeConverter<ReservationStatus, String> {

    // DB에 저장할 때 (Enum -> String) 영문으로 저장
    @Override
    public String convertToDatabaseColumn(ReservationStatus reservationStatus) {

        return reservationStatus.name();
    }

    // DB에 꺼내서 조회할 때 (String -> Enum)
    @Override
    public ReservationStatus convertToEntityAttribute(String dbData) {

        return Arrays.stream(ReservationStatus.values())
                .filter(i -> i.getStatus().equals(dbData))
                .findFirst()
                .orElseThrow(() -> new GlobalException(ErrorCode.RESERVATION_STATUS_NOT_FOUND));
    }
}