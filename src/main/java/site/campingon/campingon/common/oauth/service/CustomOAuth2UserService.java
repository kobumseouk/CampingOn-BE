package site.campingon.campingon.common.oauth.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import site.campingon.campingon.common.exception.ErrorCode;
import site.campingon.campingon.common.exception.OAuthException;
import site.campingon.campingon.common.oauth.CustomOAuth2User;
import site.campingon.campingon.common.oauth.dto.provider.GoogleResponseDto;
import site.campingon.campingon.common.oauth.dto.provider.OAuth2ResponseDto;
import site.campingon.campingon.common.oauth.dto.OAuth2UserDto;
import site.campingon.campingon.user.entity.Role;
import site.campingon.campingon.user.entity.User;
import site.campingon.campingon.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    // 로그인 시 제공자와 연동을 하고 데이터를 확인
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        String accessToken = userRequest.getAccessToken().getTokenValue();

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2ResponseDto oAuth2ResponseDto = null;

        // 리팩토링 시 다른 소셜 연동을 위한 분기점
        if (registrationId.equals("google")) {

            oAuth2ResponseDto = new GoogleResponseDto(oAuth2User.getAttributes(), accessToken);
        }
        else {

            return null;
        }

        // 리소스 서버에서 발급 받은 정보로 유저의 특정 아이디값 생성
        String oauthName = oAuth2ResponseDto.getProvider() + " " + oAuth2ResponseDto.getProviderId();

        // DB에 존재하는 oauth 로그인 계정인지 확인
        User existUser = userRepository.findByOauthName(oauthName);

        // attributes에 accessToken 추가 -> oauth 연동 해제를 위해 토큰값이 필요
        Map<String, Object> newAttributes = new HashMap<>(oAuth2User.getAttributes());
        newAttributes.put("accessToken", accessToken);

        // 한번도 로그인 한 적 x -> 새 데이터 삽입
        if(existUser == null) {

            // 임의의 9자리 UUID 생성
            String nickname = UUID.randomUUID().toString().replace("-", "").substring(0, 9);

            User newUser = User.builder()
                    .email(oAuth2ResponseDto.getEmail())
                    .oauthName(oauthName)
                    .nickname(nickname)
                    .name(oAuth2ResponseDto.getName())
                    .role(Role.ROLE_USER)
                    .build();

            userRepository.save(newUser);

            OAuth2UserDto OAuthUserDto = OAuth2UserDto.builder()
                    .oauthName(oauthName)
                    .email(oAuth2ResponseDto.getEmail())
                    .nickname(nickname)
                    .name(oAuth2ResponseDto.getName())
                    .role(String.valueOf(Role.ROLE_USER))
                    .build();

            return new CustomOAuth2User(OAuthUserDto, newAttributes);
        }

        // 기존에 로그인 한 적 o -> 바로 로그인 처리
        else {

            OAuth2UserDto OAuthUserDto = OAuth2UserDto.builder()
                    .oauthName(existUser.getOauthName())
                    .email(existUser.getEmail())
                    .nickname(existUser.getNickname())
                    .name(existUser.getName())
                    .role(String.valueOf(existUser.getRole()))
                    .build();

            return new CustomOAuth2User(OAuthUserDto, newAttributes);
        }
    }

    // 구글 로그인 유저 탈퇴 시
    @Transactional
    public void deleteGoogleAccount(CustomOAuth2User oauth2User) {
        String accessToken = (String) oauth2User.getAttributes().get("accessToken");

        if (accessToken == null) {
            throw new OAuthException(ErrorCode.INVALID_TOKEN);
        }

        // OAuth 연동 해제 - 처음에 발급 받은 액세스토큰 필요
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://accounts.google.com/o/oauth2/revoke?token=" + accessToken;

        try {
            restTemplate.exchange(url, HttpMethod.POST, null, String.class);
            log.debug("Google account successfully revoked.");

            //DB 업데이트 - soft-delete, oauthName 삭제
            String oauthName = ((CustomOAuth2User) oauth2User).getOauthName();
            User user = userRepository.findByOauthName(oauthName);

            User updatedUser = user.toBuilder()
                    .oauthName(null)
                    .deletedAt(LocalDateTime.now())
                    .build();
            
            userRepository.save(updatedUser);
            log.debug("User data successfully updated for deletion: {}", updatedUser);

        } catch (Exception e) {
            throw new OAuthException(ErrorCode.DELETE_USER_DENIED);
        }
    }

}