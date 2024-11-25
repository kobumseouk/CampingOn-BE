package site.campingon.campingon.bookmark.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.campingon.campingon.bookmark.entity.Bookmark;
import site.campingon.campingon.bookmark.repository.BookmarkRepository;
import site.campingon.campingon.camp.entity.Camp;
import site.campingon.campingon.camp.repository.CampRepository;
import site.campingon.campingon.user.entity.User;
import site.campingon.campingon.user.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class BookmarkService {
  private final BookmarkRepository bookmarkRepository;
  private final CampRepository campRepository;
  private final UserRepository userRepository;

  // 찜하기
  public void bookmarkCamp(Long campId, Long userId) {
    // 이미 찜 관계가 있는지 확인
    Optional<Bookmark> existingBookmark = bookmarkRepository.findByCampIdAndUserId(campId, userId);

    if (existingBookmark.isPresent()) {
      // 이미 찜 관계가 있고 isMarked가 false면 true로 업데이트
      if (!existingBookmark.get().isMarked()) {
        Bookmark bookmark = existingBookmark.get().toBuilder()
            .isMarked(true)
            .build();
        bookmarkRepository.save(bookmark);
      }
      // isMarked가 이미 true인 경우에는 아무것도 하지 않음
      return;
    }

    // 새로운 찜 관계 생성
    Camp camp = campRepository.findById(campId)
        .orElseThrow(() -> new RuntimeException("캠핑장을 찾을 수 없습니다."));

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

    Bookmark bookmark = Bookmark.builder()
        .camp(camp)
        .user(user)
        .build();  // isMarked는 기본값 true

    bookmarkRepository.save(bookmark);
  }

  // 찜 해제
  public void unbookmarkCamp(Long campId, Long userId) {
    Bookmark bookmark = bookmarkRepository.findByCampIdAndUserId(campId, userId)
        .orElseThrow(() -> new RuntimeException("찜 관계를 찾을 수 없습니다."));

    // soft delete: isMarked만 false로 변경
    Bookmark updatedBookmark = bookmark.toBuilder()
        .isMarked(false)
        .build();

    bookmarkRepository.save(updatedBookmark);
  }
}
