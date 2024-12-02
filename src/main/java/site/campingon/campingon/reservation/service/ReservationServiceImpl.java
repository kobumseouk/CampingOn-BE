package site.campingon.campingon.reservation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.campingon.campingon.camp.dto.CampSiteResponseDto;
import site.campingon.campingon.camp.entity.CampSite;
import site.campingon.campingon.camp.mapper.CampSiteMapper;
import site.campingon.campingon.reservation.dto.*;
import site.campingon.campingon.reservation.repository.ReservationRepository;
import site.campingon.campingon.reservation.mapper.ReservationMapper;
import site.campingon.campingon.reservation.entity.Reservation;
import site.campingon.campingon.reservation.utils.ReservationValidate;
import site.campingon.campingon.user.entity.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final CampSiteMapper campSiteMapper;
    private final ReservationMapper reservationMapper;
    private final ReservationValidate reservationValidate;
    private final ReservationRepository reservationRepository;

    // 유저의 모든 예약리스트를 조회
    @Transactional(readOnly = true)
    public Page<ReservationResponseDto> getReservations(Long userId, Pageable pageable) {

        Page<Reservation> reservations = reservationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return reservations.map(reservationMapper::toResponse);
    }

    // 예약완료 직후 확인을 위해 예약 정보 조회
    @Transactional(readOnly = true)
    public ReservationResponseDto getReservation(Long reservationId) {

        Reservation reservation = reservationValidate.validateReservationById(reservationId);

        return reservationMapper.toResponse(reservation);
    }


    // 캠프사이트 선택 후 예약 요청
    @Transactional
    public void createReservation(Long userId, ReservationCreateRequestDto requestDto) {

        User user = reservationValidate.validateUserById(userId);

        CampSite campSite = reservationValidate.validateCampSiteById(requestDto.getCampId());

        Reservation reservation = Reservation.builder()
                .user(user)
                .camp(campSite.getCamp())
                .campSite(campSite)
                .checkInDate(requestDto.getCheckIn())
                .checkOutDate(requestDto.getCheckOut())
                .guestCnt(requestDto.getGuestCnt())
                .totalPrice(requestDto.getTotalPrice())
                .build();

        reservationRepository.save(reservation);
    }

    // 예약완료 이후 예약취소 요청
    @Transactional
    public void cancelReservation(Long reservationId, ReservationCancelRequestDto requestDto) {
        Reservation reservation = reservationValidate.validateReservationById(requestDto.getId());

        reservationValidate.validateStatus(requestDto.getStatus());

        Reservation canceledReservation = reservation.toBuilder()
                .status(requestDto.getStatus())
                .cancelReason(requestDto.getCancelReason())
                .build();

        reservationRepository.save(canceledReservation);
    }


    // 예약가능한 캠프사이트 조회를 위해 특정 날짜에 예약이 됐는지 조회
    @Transactional(readOnly = true)
    public ReservedCampSiteIdListResponseDto getReservedCampSiteIds(ReservationCheckDateRequestDto requestDto) {

        List<Long> reservedIds = reservationRepository.findReservedCampSiteIds(
                requestDto.getCampId(),
                requestDto.getCheckIn(),
                requestDto.getCheckOut());

        return new ReservedCampSiteIdListResponseDto(reservedIds);
    }

    // 예약가능한 캠프사이트를 타입별로 하나씩 조회
    @Transactional(readOnly = true)
    public List<CampSiteResponseDto> getAvailableCampSites(ReservationCheckDateRequestDto requestDto) {

        List<CampSite> campSites = reservationRepository.findAvailableCampSites(
                requestDto.getCampId(),
                requestDto.getCheckIn(),
                requestDto.getCheckOut());

        // 타입별 첫 번째 행 데이터만 가져오기
        return campSites.stream()
                .collect(Collectors.groupingBy(CampSite::getSiteType))
                .values().stream()
                .map(group -> group.get(0))
                .map(campSiteMapper::toCampSiteResponseDto)
                .toList();
    }
}
