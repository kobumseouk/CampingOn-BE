package site.campingon.campingon.reservation.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import site.campingon.campingon.reservation.dto.*;

public interface ReservationService {

    // 유저의 모든 예약리스트를 조회
    Page<ReservationResponseDto> getReservations(Long userId, Pageable pageable);

    // 예약완료 직후 확인을 위해 예약 정보 조회
    ReservationResponseDto getReservation(Long reservationId);

    // 캠프사이트 선택 후 예약 요청
    void createReservation(Long userId, ReservationCreateRequestDto requestDto);

    // 예약완료 이후 예약취소 요청
    void cancelReservation(Long reservationId, ReservationCancelRequestDto requestDto);

    // 예약가능한 캠프사이트 조회를 위해 특정 날짜에 예약이 됐는지 조회
    ReservedCampSiteIdListResponseDto getReservedCampSiteIds(ReservationCheckDateRequestDto requestDto);

}
