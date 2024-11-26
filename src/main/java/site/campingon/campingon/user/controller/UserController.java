package site.campingon.campingon.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.campingon.campingon.common.jwt.CustomUserDetails;
import site.campingon.campingon.user.dto.UserResponseDto;
import site.campingon.campingon.user.dto.UserSignUpRequestDto;
import site.campingon.campingon.user.dto.UserSignUpResponseDto;
import site.campingon.campingon.user.service.UserService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    // 회원 가입 api
    @PostMapping("/signup")
    public ResponseEntity<UserSignUpResponseDto> registerUser(@RequestBody @Valid UserSignUpRequestDto userSignUpRequestDto) {
        UserSignUpResponseDto userSignUpResponseDto = userService.registerUser(userSignUpRequestDto);
        log.info("회원가입 성공: email={}", userSignUpRequestDto.getEmail());
        return ResponseEntity.ok(userSignUpResponseDto);
    }

    // 회원 정보 조회 api
    @GetMapping("/user/me")
    public ResponseEntity<UserResponseDto> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        UserResponseDto userInfo = userService.getMyInfo(userId);

        return ResponseEntity.ok(userInfo);
    }

}
