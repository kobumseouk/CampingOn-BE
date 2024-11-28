package site.campingon.campingon.common.oauth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import site.campingon.campingon.common.exception.ErrorCode;
import site.campingon.campingon.common.exception.OAuthException;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        // 예외 유형에 따른 적절한 ErrorCode 설정
        ErrorCode errorCode;

        if (exception instanceof BadCredentialsException) {
            // 잘못된 자격 증명 (비밀번호 오류)
            errorCode = ErrorCode.INVALID_CREDENTIALS;

        } else if (exception instanceof InsufficientAuthenticationException) {
            // 인증에 필요한 비밀 키가 유효하지 않음
            errorCode = ErrorCode.INVALID_SECRET_KEY;

        } else {
            // 기본적인 Access Denied 처리
            errorCode = ErrorCode.ACCESS_DENIED;
        }

        throw new OAuthException(errorCode);
    }
}
