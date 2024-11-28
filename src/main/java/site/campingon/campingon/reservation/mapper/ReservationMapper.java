package site.campingon.campingon.reservation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import site.campingon.campingon.reservation.dto.ReservationCreatedRequestDto;
import site.campingon.campingon.reservation.dto.ReservationResponseDto;
import site.campingon.campingon.reservation.entity.Reservation;

@Mapper(componentModel = "spring")
public interface ReservationMapper {
    Reservation toEntity(ReservationCreatedRequestDto reservationRequest);
    ReservationResponseDto toResponse(Reservation reservation);


}