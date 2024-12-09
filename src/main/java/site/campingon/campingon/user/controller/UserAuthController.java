package site.campingon.campingon.user.controller;


import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import site.campingon.campingon.common.exception.ErrorCode;
import site.campingon.campingon.common.exception.GlobalException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.campingon.campingon.common.jwt.JwtToken;
import site.campingon.campingon.common.jwt.JwtTokenProvider;
import site.campingon.campingon.common.util.CookieUtil;
import site.campingon.campingon.user.dto.UserSignInRequestDto;
import site.campingon.campingon.user.service.UserAuthService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserAuthController {

    private final JwtTokenProvider jwtTokenProvider;
    @Value("${jwt.refresh-expired}")
    private Long refreshTokenExpired;

    private final UserAuthService userAuthService;

    // 로그인 처리
    @PostMapping("/login")
    public ResponseEntity<JwtToken> login(@RequestBody UserSignInRequestDto userSignInRequestDto,
        HttpServletResponse response) throws IOException {

        JwtToken jwtToken = userAuthService.login(userSignInRequestDto);
        CookieUtil.setCookie(response, "refreshToken", jwtToken.getRefreshToken(),refreshTokenExpired);

        return ResponseEntity.ok(jwtToken);
    }


    // 토큰 재발급
    @GetMapping("/token/refresh")
    public ResponseEntity<JwtToken> refreshAccessToken(
        @CookieValue(name = "refreshToken", required = false) String refreshToken,
        HttpServletResponse response
    ) throws IOException {

        if (refreshToken == null) {
            throw new GlobalException(ErrorCode.NO_TOKEN);
        }

        JwtToken jwtToken = userAuthService.refresh(refreshToken);
        CookieUtil.setCookie(response, "refreshToken", jwtToken.getRefreshToken(), refreshTokenExpired);

        // JWT 토큰 정보 반환
        return ResponseEntity.ok(jwtToken);
    }

    // 로그아웃 API
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
        @RequestHeader("Authorization") String authHeader,
        @CookieValue(name = "refreshToken", required = false) String refreshToken,
        HttpServletResponse response
    ) {
        // 헤더에서 Access Token 추출
        String accessToken = authHeader.replace("Bearer ", "");

        // 로그아웃 로직 - AccessToken: Blacklist 등록, RefreshToken: redis에서 삭제 및 쿠키 제거
        userAuthService.logout(accessToken, refreshToken, response);

        return ResponseEntity.noContent().build(); // 204 No Content 응답
    }

}
