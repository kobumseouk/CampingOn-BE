package site.campingon.campingon.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import site.campingon.campingon.common.exception.ErrorCode;
import site.campingon.campingon.common.exception.GlobalException;
import site.campingon.campingon.common.jwt.JwtTokenProvider;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.campingon.campingon.common.jwt.JwtToken;
import site.campingon.campingon.user.dto.UserSignInRequestDto;
import site.campingon.campingon.user.service.UserAuthService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserAuthController {


    private final UserAuthService userAuthService;
    private final JwtTokenProvider jwtTokenProvider;

    // 로그인 처리
    @PostMapping("/login")
    public ResponseEntity<JwtToken> login(@RequestBody UserSignInRequestDto userSignInRequestDto) {

        JwtToken jwtToken = userAuthService.login(userSignInRequestDto);
        return ResponseEntity.ok(jwtToken);
    }


    // 토큰 재발급
    @PostMapping("/token/refresh")
    public ResponseEntity<Object> refreshAccessToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        // 정상적인 refresh Token일 경우에
        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
            JwtToken jwtToken = userAuthService.refresh(refreshToken);

            return ResponseEntity.ok(jwtToken);
        }
        throw new GlobalException(ErrorCode.INVALID_TOKEN);
    }

    // 로그아웃 API
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        // 헤더에서 Access Token 추출
        String accessToken = request.getHeader("Authorization");
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7); // "Bearer " 제거
        } else {
            throw new GlobalException(ErrorCode.NO_TOKEN);
        }

        userAuthService.logout(accessToken); // 서버에서 Refresh Token 삭제 처리
        return ResponseEntity.noContent().build(); // 204 No Content 응답
    }

}
