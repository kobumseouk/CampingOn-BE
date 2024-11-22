package site.campingon.campingon.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.campingon.campingon.user.entity.UserKeyword;

import java.util.List;

@Repository
public interface UserKeywordRepository extends JpaRepository<UserKeyword, Long> {
  List<String> findKeywordsByUserId(Long userId);
  boolean existsByUserId(Long userId);  // 키워드 존재 여부 확인
}
