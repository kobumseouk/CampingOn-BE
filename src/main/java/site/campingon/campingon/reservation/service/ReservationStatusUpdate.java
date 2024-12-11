package site.campingon.campingon.reservation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import site.campingon.campingon.reservation.entity.Reservation;
import site.campingon.campingon.reservation.entity.ReservationStatus;
import site.campingon.campingon.reservation.repository.ReservationRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationStatusUpdate {

    private final ReservationRepository reservationRepository;

    // 매일 자정에 예약불가 실행
    @Scheduled(cron = "0 59 23 * * ?")
    public void updateStatusToNotCancelable() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        LocalDate targetDate = LocalDate.from(now.plusDays(1));

        LocalDateTime targetTime = targetDate.atTime(15, 0);

        List<Reservation> reservations = reservationRepository.findByCheckin(targetTime);

        reservations.stream()
                .filter(reservation -> reservation.getStatus() != ReservationStatus.NOTCANCELABLE)
                .forEach(reservation -> reservation.changeStatus(ReservationStatus.NOTCANCELABLE));

        reservationRepository.saveAll(reservations);

        log.debug("예약불가 업데이트 스케줄러가 실행되었습니다.");
    }

    // 매일 3시에 체크인완료 실행
    @Scheduled(cron = "0 0 15 * * ?")
    public void updateStatusToCompleted() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        LocalDateTime targetDate = now.toLocalDate().atTime(15, 0);

        List<Reservation> reservations = reservationRepository.findByCheckin(targetDate);
        for (Reservation reservation : reservations) {
            if (reservation.getStatus() != ReservationStatus.COMPLETED) {
                reservation.changeStatus(ReservationStatus.COMPLETED);
            }
        }
        reservationRepository.saveAll(reservations);

        log.debug("체크인완료 업데이트 스케줄러가 실행되었습니다.");
    }
}