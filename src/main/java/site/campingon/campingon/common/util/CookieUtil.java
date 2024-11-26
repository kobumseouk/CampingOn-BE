package site.campingon.campingon.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.util.WebUtils;

public class CookieUtil {

    // 쿠키 설정
    public static void setCookie(HttpServletResponse response, String name, String value, Long maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .sameSite("None")
            .maxAge(maxAge)
            .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    // 쿠키 삭제
    public static void deleteCookie(HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, null)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .sameSite("None")
            .maxAge(0) // 즉시 만료
            .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    // 특정 쿠키 값 가져오기
    public static Optional<String> getCookieValue(HttpServletRequest request, String name) {
        Cookie cookie = WebUtils.getCookie(request, name);
        return cookie != null ? Optional.of(cookie.getValue()) : Optional.empty();
    }
}
