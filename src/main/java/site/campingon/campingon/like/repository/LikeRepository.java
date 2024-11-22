package site.campingon.campingon.like.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.campingon.campingon.like.entity.Like;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
  boolean existsByCampIdAndUserId(Long campId, Long userId);
}
