package site.campingon.campingon.user.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.campingon.campingon.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // user id로 검색
    Optional<User> findByIdAndIsDeletedFalse(Long id);

    // user email로 검색
    Optional<User> findByEmailAndIsDeletedFalse(String email);

    // user email로 중복유무 검색 - oauth 인증 절차
    User findByOauthName(String oauthName);

    // 이메일 중복 여부 확인
    boolean existsByEmailAndIsDeletedFalse(String email);

    // 닉네임 중복여부 확인
    boolean existsByNicknameAndIsDeletedFalse(String nickname);
}
