package site.campingon.campingon.reservation.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.campingon.campingon.reservation.entity.Reservation;
import site.campingon.campingon.reservation.entity.ReservationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 유저의 모든 예약 조회
    Page<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 특정 캠프장의 특정 날짜에 예약된 캠프사이트 ID 목록 조회 (일자까지만 파싱해서 조회)
    @Query("SELECT r.campSite.id FROM Reservation r " +
            "WHERE r.camp.id = :campId " +
            "AND r.status = 'RESERVED' " +
            "AND CAST(r.checkIn AS date) < CAST(:checkOut AS date) " +
            "AND CAST(r.checkOut AS date) > CAST(:checkIn AS date)")
    List<Long> findReservedCampSiteIds(
            @Param("campId") Long campId,
            @Param("checkIn") LocalDateTime checkIn,
            @Param("checkOut") LocalDateTime checkOut
    );

    // 특정 예약의 상세 정보 조회 (연관된 캠프, 주소 정보 포함)
    @EntityGraph(attributePaths = {"camp", "campSite"})
    Optional<Reservation> findById(Long id);

}
