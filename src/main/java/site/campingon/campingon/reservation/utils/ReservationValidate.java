package site.campingon.campingon.reservation.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import site.campingon.campingon.camp.entity.CampSite;
import site.campingon.campingon.camp.repository.CampSiteRepository;
import site.campingon.campingon.common.exception.ErrorCode;
import site.campingon.campingon.common.exception.GlobalException;
import site.campingon.campingon.reservation.entity.Reservation;
import site.campingon.campingon.reservation.entity.ReservationStatus;
import site.campingon.campingon.reservation.repository.ReservationRepository;
import site.campingon.campingon.user.entity.User;
import site.campingon.campingon.user.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class ReservationValidate {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final CampSiteRepository campSiteRepository;

    public Reservation validateReservationById(Long reservationId) {

        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new GlobalException(ErrorCode.RESERVATION_NOT_FOUND_BY_ID));

    }

    public User validateUserById(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND_BY_ID));
    }

    public CampSite validateCampSiteById(Long campSiteId) {

        return campSiteRepository.findById(campSiteId)
                .orElseThrow(() -> new GlobalException(ErrorCode.CAMP_NOT_FOUND_BY_ID));
    }

    // 예약취소가 가능한지 체크
    public void validateStatus(ReservationStatus status) {

        if (status == ReservationStatus.CANCELED || status == ReservationStatus.COMPLETED) {
            throw new GlobalException(ErrorCode.RESERVATION_NOT_CANCELED);
        }
    }
}
