package site.campingon.campingon.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.campingon.campingon.review.entity.Review;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // 특정 캠핑지의 리뷰 조회
    List<Review> findByCampSiteId(Long campSiteId);

    // 특정 캠핑장 하위 모든 리뷰 조회
    List<Review> findByCampId(Long campId);

    boolean existsByReservationId(Long reservationId);

    /*// 특정 시점 이전에 삭제된 리뷰들 조회
    @Query("SELECT r FROM Review r WHERE r.deletedAt IS NOT NULL AND r.deletedAt < :date")
    List<Review> findByDeletedAtBefore(@Param("date") LocalDateTime date);*/
}