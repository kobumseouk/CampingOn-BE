package site.campingon.campingon.reservation.mapper;

import org.mapstruct.Mapper;
import site.campingon.campingon.reservation.dto.ReservationCancelRequestDto;
import site.campingon.campingon.reservation.dto.ReservationCreateRequestDto;
import site.campingon.campingon.reservation.dto.ReservationResponseDto;
import site.campingon.campingon.reservation.entity.Reservation;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    Reservation toEntity(ReservationCreateRequestDto reservationRequest);

    Reservation toEntity(ReservationCancelRequestDto reservationRequest);

    ReservationResponseDto toResponse(Reservation reservation);

}