package site.campingon.campingon.reservation.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.campingon.campingon.camp.entity.CampSite;
import site.campingon.campingon.reservation.entity.Reservation;
import site.campingon.campingon.reservation.entity.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 유저의 모든 예약 조회
    Page<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 특정 캠핑장의 특정 날짜에 예약된 캠프사이트 ID 목록 조회 (일자만 조회)
    @Query("SELECT r.campSite.id FROM Reservation r " +
            "WHERE r.camp.id = :campId " +
            "AND r.status = 'RESERVED' " +
            "AND r.checkInDate < :checkOut " +
            "AND r.checkOutDate > :checkIn")
    List<Long> findReservedCampSiteIds(@Param("campId") Long campId,
                                       @Param("checkIn") LocalDate checkIn,
                                       @Param("checkOut") LocalDate checkOut);

    // 특정 캠핑장의 특정 날짜에 예약 가능한 캠프사이트를 타입별로 1개 반환되게끔 조회
    @Query("SELECT cs FROM CampSite cs " +
            "LEFT JOIN Reservation r ON cs.id = r.campSite.id " +
            "WHERE cs.isAvailable = true " +
            "AND cs.camp.id = :campId " +
            "AND (r.id IS NULL OR r.status = 'CANCELED') " + // 예약테이블에 없거나 취소상태여야 함
            "AND (r.checkInDate < :checkOut AND r.checkOutDate > :checkIn) " +
            "ORDER BY cs.id ASC")
    List<CampSite> findAvailableCampSites(@Param("campId") Long campId,
                                                           @Param("checkIn") LocalDate checkIn,
                                                           @Param("checkOut") LocalDate checkOut);

    // 특정 예약의 상세 정보 조회 (연관된 캠프, 주소 정보 포함)
    @EntityGraph(attributePaths = {"camp", "campSite"})
    Optional<Reservation> findById(Long id);

}
