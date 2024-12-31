package site.campingon.campingon.bookmark.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.campingon.campingon.bookmark.entity.Bookmark;
import site.campingon.campingon.bookmark.repository.BookmarkRepository;
import site.campingon.campingon.camp.entity.Camp;
import site.campingon.campingon.camp.entity.CampInfo;
import site.campingon.campingon.camp.repository.CampInfoRepository;
import site.campingon.campingon.camp.repository.CampRepository;
import site.campingon.campingon.common.exception.ErrorCode;
import site.campingon.campingon.common.exception.GlobalException;
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
  private final CampInfoRepository campInfoRepository;

  // 찜 기능 (토글활용)
  public void bookmarkCamp(Long campId, Long userId) {
    // 이미 찜 관계가 있는지 확인
    CampInfo campInfo = campInfoRepository.findByCampId(campId)
            .orElseThrow(() -> new GlobalException(ErrorCode.CAMP_NOT_FOUND_BY_ID));

    Camp camp = campRepository.findById(campId)
        .orElseThrow(() -> new GlobalException(ErrorCode.CAMP_NOT_FOUND_BY_ID));

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new GlobalException(ErrorCode.BOOKMARK_USER_NOT_FOUND));

    Optional<Bookmark> existingBookmark = bookmarkRepository.findByCampIdAndUserId(campId, userId);

    // 이미 찜 등록이 된경우
    if (existingBookmark.isPresent()) {
      // isMarked 상태를 반대로 토글 - true->false, false->true로 전환
      bookmarkRepository.delete(existingBookmark.get());
      campInfo.updateBookmarkCntDown();
      return;  // 변경 후 반환
    }



    Bookmark bookmark = Bookmark.builder()
        .camp(camp)
        .user(user)
        .build();  // isMarked는 기본값 true

    bookmarkRepository.save(bookmark);
    campInfo.updateBookmarkCntUp();
  }

}
