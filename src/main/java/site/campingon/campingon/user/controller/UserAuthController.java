package site.campingon.campingon.user.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.util.WebUtils;
import site.campingon.campingon.common.exception.ErrorCode;
import site.campingon.campingon.common.exception.GlobalException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.campingon.campingon.common.jwt.JwtToken;
import site.campingon.campingon.common.util.CookieUtil;
import site.campingon.campingon.user.dto.UserSignInRequestDto;
import site.campingon.campingon.user.service.UserAuthService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserAuthController {

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
    public ResponseEntity<JwtToken> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Cookie refreshTokenCookie = WebUtils.getCookie(request, "refreshToken");

        if (refreshTokenCookie == null) {
            throw new GlobalException(ErrorCode.NO_TOKEN);
        }

        JwtToken jwtToken = userAuthService.refresh(refreshTokenCookie.getValue());
        CookieUtil.setCookie(response, "refreshToken", jwtToken.getRefreshToken(), refreshTokenExpired);

        // JWT 토큰 정보 반환
        return ResponseEntity.ok(jwtToken);
    }

    // 로그아웃 API
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        // 헤더에서 Access Token 추출
        String accessToken = request.getHeader("Authorization");
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7); // "Bearer " 제거
        } else {
            throw new GlobalException(ErrorCode.NO_TOKEN);
        }

        // 서버에서 리프레시 토큰 삭제
        userAuthService.logout(accessToken);

        // Refresh Token 쿠키 삭제
        CookieUtil.deleteCookie(response, "refreshToken");

        return ResponseEntity.noContent().build(); // 204 No Content 응답
    }

}
