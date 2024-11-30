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

    // 리뷰 상세 조회
    Optional<Review> findById(Long id);

    // 특정 캠핑지의 리뷰 조회
    List<Review> findByCampSiteId(Long campSiteId);

    // 특정 캠핑장 하위 모든 리뷰 조회
    @Query("""
            SELECT r FROM Review r
            WHERE r.camp.id = :campId
            """)
    List<Review> findByCampId(@Param("campId") Long campId);

    boolean existsByReservationId(Long reservationId);
}