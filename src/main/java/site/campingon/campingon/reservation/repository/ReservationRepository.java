package site.campingon.campingon.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.campingon.campingon.reservation.entity.Reservation;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

}
