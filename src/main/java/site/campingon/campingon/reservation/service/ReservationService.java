package site.campingon.campingon.reservation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.campingon.campingon.reservation.repository.ReservationRepository;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
}
