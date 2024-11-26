package site.campingon.campingon.user.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import site.campingon.campingon.common.exception.ErrorCode;
import site.campingon.campingon.common.exception.GlobalException;
import site.campingon.campingon.common.util.CookieUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.WebUtils;
import site.campingon.campingon.common.jwt.JwtToken;
import site.campingon.campingon.user.dto.UserSignInRequestDto;
import site.campingon.campingon.user.service.UserAuthService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserAuthController {

    @Value("${jwt.refresh-expired}")
    private Long refreshTokenExpired;

    @Value("${jwt.access-expired}")
    private Long accessExpired;

    private final UserAuthService userAuthService;

    // 로그인 처리
    @PostMapping("/login")
    public ResponseEntity<JwtToken> login(@RequestBody UserSignInRequestDto userSignInRequestDto,
        HttpServletResponse response) throws IOException {

        JwtToken jwtToken = userAuthService.login(userSignInRequestDto);

        // access token 쿠키 설정
        CookieUtil.setCookie(response, "accessToken", jwtToken.getAccessToken(), accessExpired);
        // refresh token 쿠키 설정
        CookieUtil.setCookie(response, "refreshToken", jwtToken.getRefreshToken(), refreshTokenExpired);

        return ResponseEntity.ok(jwtToken); // 응답 본문에도 포함
    }


    // 토큰 재발급
    @GetMapping("/token/refresh")
    public ResponseEntity<Object> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Cookie refreshTokenCookie = WebUtils.getCookie(request, "refreshToken");

        if (refreshTokenCookie == null) {
            throw new GlobalException(ErrorCode.NO_TOKEN);
        }

        JwtToken jwtToken = userAuthService.refresh(refreshTokenCookie.getValue());

        CookieUtil.setCookie(response, "accessToken", jwtToken.getAccessToken(), accessExpired);
        CookieUtil.setCookie(response, "refreshToken", jwtToken.getRefreshToken(), refreshTokenExpired);

        // JWT 토큰 정보 반환
        return ResponseEntity.ok(jwtToken);
    }

    // 로그아웃 API
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키 삭제
        CookieUtil.deleteCookie(response, "accessToken");
        CookieUtil.deleteCookie(response, "refreshToken");

        // refresh token을 db에서 삭제
        String accessToken = CookieUtil.getCookieValue(request, "accessToken")
            .orElseThrow(() -> new GlobalException(ErrorCode.NO_TOKEN));
        userAuthService.logout(accessToken);
        return ResponseEntity.noContent().build(); // 204 No Content 응답
    }

}
