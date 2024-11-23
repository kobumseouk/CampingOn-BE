package site.campingon.campingon.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    public ResponseEntity<UserSignUpResponseDto> registerUser(@RequestBody UserSignUpRequestDto userSignUpRequestDto) {
        UserSignUpResponseDto userSignUpResponseDto = userService.registerUser(userSignUpRequestDto);
        log.info("회원가입 성공: email={}", userSignUpRequestDto.getEmail());
        return ResponseEntity.ok(userSignUpResponseDto);
    }

}
