package site.campingon.campingon.bookmark.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import site.campingon.campingon.bookmark.service.BookmarkService;
import site.campingon.campingon.user.entity.User;

@Slf4j
@RestController
@RequestMapping("/api/camps")
@RequiredArgsConstructor
public class BookMarkController {
  private final BookmarkService bookmarkService;

  // 찜하기
  @PostMapping("/{campId}/bookmarks")
  public ResponseEntity<Void> bookmarkCamp(
      @PathVariable Long campId,
      @AuthenticationPrincipal UserDetails userDetails
  ) {
    User user = (User) userDetails;
    bookmarkService.bookmarkCamp(campId, user.getId());
    return ResponseEntity.ok().build();
  }

  // 찜 해제
  @DeleteMapping("/{campId}/bookmarks")
  public ResponseEntity<Void> unbookmarkCamp(
      @PathVariable Long campId,
      @AuthenticationPrincipal UserDetails userDetails
  ) {
    User user = (User) userDetails;
    bookmarkService.unbookmarkCamp(campId, user.getId());
    return ResponseEntity.ok().build();
  }
}
