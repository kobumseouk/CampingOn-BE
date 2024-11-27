package site.campingon.campingon.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import site.campingon.campingon.common.oauth.CustomOAuth2User;
import site.campingon.campingon.user.entity.Role;

@Slf4j
@Component
public class JwtTokenProvider {

    private final Key secretKey;

    @Value("${jwt.access-expired}")
    public Long accessTokenExpired;

    @Value("${jwt.refresh-expired}")
    public Long refreshTokenExpired;

    public JwtTokenProvider(@Value("${jwt.secret-key}") String secretKey) {
        byte[] keyBytes = secretKey.getBytes();
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // 토큰 생성 - 유저 정보 이용
    public JwtToken generateToken(Authentication authentication) {

        long now = (new Date()).getTime();
        Date accessTokenExpiration = new Date(now + accessTokenExpired * 1000);
        Date refreshTokenExpiration = new Date(now + refreshTokenExpired * 1000);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 사용자 권한 정보를 문자열로 변환
        String authorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));

        // Access Token 생성
        String accessToken = Jwts.builder()
            .setSubject(userDetails.getEmail()) // 이메일을 Subject로 설정
            .claim("id", userDetails.getId()) // 사용자 ID
            .claim("nickname", userDetails.getNickname()) // 닉네임
            .claim("role", userDetails.getRole().name()) // 사용자 역할(Role)
            .setExpiration(accessTokenExpiration) // 만료 시간
            .signWith(secretKey, SignatureAlgorithm.HS256) // 서명
            .compact();

        // Refresh Token 생성 (주로 만료 시간만 포함)
        String refreshToken = Jwts.builder()
            .setSubject(userDetails.getEmail()) // 사용자 식별용 정보
            .setExpiration(refreshTokenExpiration) // 만료 시간
            .signWith(secretKey, SignatureAlgorithm.HS256) // 서명
            .compact();


        // JWT Token 객체 반환
        return JwtToken.builder()
            .grantType("Bearer")
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();

    }

    // 리프레시 토큰 생성
    public String createRefreshToken(String email) {
        long now = (new Date()).getTime();
        Date refreshTokenExpiration = new Date(now + refreshTokenExpired * 1000);

        return Jwts.builder()
            .setSubject(email) // 토큰의 Subject에 사용자 이메일 저장
            .setExpiration(refreshTokenExpiration) // 만료 시간 설정
            .signWith(secretKey, SignatureAlgorithm.HS256) // 서명 알고리즘과 키 설정
            .compact();
    }

    // OAuth2 사용자 토큰 생성 메서드
    public JwtToken generateOAuth2Token(Authentication authentication) {

        long now = (new Date()).getTime();
        Date accessTokenExpiration = new Date(now + accessTokenExpired * 1000);
        Date refreshTokenExpiration = new Date(now + refreshTokenExpired * 1000);

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        // OAuth2 사용자 권한 정보를 문자열로 변환
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // Access Token 생성
        String accessToken = Jwts.builder()
                .setSubject(oAuth2User.getEmail())
                .claim("nickname", oAuth2User.getNickname())
                .claim("role", Role.ROLE_USER)
                .claim("oauthName", oAuth2User.getOauthName())
                .setExpiration(accessTokenExpiration)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        // Refresh Token 생성 (주로 만료 시간만 포함)
        String refreshToken = Jwts.builder()
                .setSubject(oAuth2User.getEmail())
                .setExpiration(refreshTokenExpiration)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        // JWT Token 객체 반환
        return JwtToken.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }


    // 토큰에서 유저 정보 추출
    public Authentication getAuthentication(String accessToken) {
        // 토큰에서 Claims 추출
        Claims claims = parseClaims(accessToken);

        // 권한 정보 확인
        if (claims.get("role") == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        // 사용자 정보 추출
        Long id = Long.parseLong(claims.get("id").toString()); // 토큰에서 사용자 ID 추출
        String email = claims.getSubject(); // 토큰 subject에서 email 추출
        String nickname = claims.get("nickname").toString(); // nickname 추출
        String password = claims.get("password", String.class);

        String roleName = claims.get("role", String.class); // 문자열로 읽기

        // 문자열에서 Role 객체로 변환
        Role role = Role.valueOf(roleName); // Enum이라면 가능

        // CustomUserDetails 생성
        CustomUserDetails userDetails = new CustomUserDetails(id, email, nickname, role, password);

        // 문자열을 GrantedAuthority로 변환
        Collection<? extends GrantedAuthority> authorities =
            Collections.singletonList(() -> roleName);


        // Authentication 객체 반환
        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }

    // 토큰 정보 검증
    public boolean validateToken(String token) {
        log.info("validateToken start");
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token", e);
        } catch (ExpiredJwtException e) {
            // refresh token 활용해서 재발급
            log.info("Expired JWT Token", e);
            throw e;
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
        }
        return false;
    }

    public Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(accessToken)
                .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    // 토큰에서 이메일 정보 추출
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .getBody();
        return claims.getSubject();
    }
}