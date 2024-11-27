package site.campingon.campingon.common.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import site.campingon.campingon.common.jwt.JwtToken;
import site.campingon.campingon.common.jwt.JwtTokenProvider;
import site.campingon.campingon.common.util.CookieUtil;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomOAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.refresh-expired}")
    private Long refreshTokenExpired;

    @Value("${jwt.access-expired}")
    private Long accessTokenExpired;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // JWT 토큰 생성
        JwtToken jwtToken = jwtTokenProvider.generateOAuth2Token(authentication);

        // Access Token을 쿠키에 추가
        CookieUtil.setCookie(response, "accessToken", jwtToken.getAccessToken(), accessTokenExpired);

        // Refresh Token을 쿠키에 추가
        CookieUtil.setCookie(response, "refreshToken", jwtToken.getRefreshToken(), refreshTokenExpired);

        response.sendRedirect("/oauth/success");
    }
}
