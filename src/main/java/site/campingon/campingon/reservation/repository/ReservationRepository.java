package site.campingon.campingon.reservation.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.campingon.campingon.reservation.entity.Reservation;

import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 유저의 모든 예약 조회
    Page<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 특정 예약의 상세 정보 조회 (연관된 캠프, 주소 정보 포함)
    @EntityGraph(attributePaths = {"camp", "campSite"})
    Optional<Reservation> findById(Long id);

}
