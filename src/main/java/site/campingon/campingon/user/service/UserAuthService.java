package site.campingon.campingon.user.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.campingon.campingon.common.exception.ErrorCode;
import site.campingon.campingon.common.exception.GlobalException;
import site.campingon.campingon.common.jwt.BlacklistService;
import site.campingon.campingon.common.jwt.CustomUserDetails;
import site.campingon.campingon.common.jwt.JwtToken;
import site.campingon.campingon.common.jwt.JwtTokenProvider;
import site.campingon.campingon.common.jwt.RefreshToken;
import site.campingon.campingon.common.jwt.RefreshTokenService;
import site.campingon.campingon.common.util.CookieUtil;
import site.campingon.campingon.user.dto.UserSignInRequestDto;
import site.campingon.campingon.user.entity.User;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAuthService {



    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;
    private final BlacklistService blacklistService;

    @Value("${jwt.refresh-expired}")
    private Long refreshTokenExpired;

    // 로그인
    @Transactional
    public JwtToken login(UserSignInRequestDto signInRequestDto) {
        User user = validateUser(signInRequestDto);
        Authentication authentication = authenticateUser(user.getEmail(), signInRequestDto.getPassword());
        return jwtTokenProvider.generateToken(authentication);
    }

    // 토큰 재발급
    @Transactional
    public JwtToken refresh(String refreshToken) {

        RefreshToken tokenDetails = refreshTokenService.getRefreshToken(refreshToken)
            .orElseThrow(() -> new GlobalException(ErrorCode.INVALID_TOKEN));

        String email = tokenDetails.getEmail();

        User user = userService.findUserByEmail(email);
        Authentication authentication = createAuthentication(user);

        // 기존에 있던 리프레시 토큰은 DB에서 제거
        refreshTokenService.deleteRefreshToken(refreshToken);

        return jwtTokenProvider.generateToken(authentication);

    }

    // 로그 아웃
    @Transactional
    public void logout(String accessToken, String refreshToken, HttpServletResponse response) {
        blacklistService.addToBlacklist(accessToken);
        refreshTokenService.deleteRefreshToken(refreshToken);
        CookieUtil.deleteCookie(response, "refreshToken");
    }

    // email, password를 사용해서 유저 확인
    private User validateUser(UserSignInRequestDto signInRequestDto) {
        User user = userService.findUserByEmail(signInRequestDto.getEmail());

        if (!passwordEncoder.matches(signInRequestDto.getPassword(), user.getPassword())) {
            throw new GlobalException(ErrorCode.INVALID_PASSWORD);
        }
        return user;
    }

    // 이메일, authentication 생성
    private Authentication authenticateUser(String email, String password) {
        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(email, password);

        return authenticationManagerBuilder.getObject().authenticate(authenticationToken);
    }


    // User 객체를 사용해 authentication 생성
    private Authentication createAuthentication(User user) {
        CustomUserDetails customUserDetails = new CustomUserDetails(
            user.getId(), user.getEmail(), user.getNickname(), user.getRole(), user.getPassword(),user.getName());

        return new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
    }


}