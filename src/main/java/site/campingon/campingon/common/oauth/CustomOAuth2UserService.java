package site.campingon.campingon.common.oauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import site.campingon.campingon.common.oauth.dto.provider.GoogleResponseDto;
import site.campingon.campingon.common.oauth.dto.provider.OAuth2ResponseDto;
import site.campingon.campingon.common.oauth.dto.OAuth2UserDto;
import site.campingon.campingon.user.entity.Role;
import site.campingon.campingon.user.entity.User;
import site.campingon.campingon.user.repository.UserRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        log.debug("[oAuth2User 확인]: {}", oAuth2User);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2ResponseDto oAuth2ResponseDto = null;

        // 리팩토링 시 다른 소셜 연동을 위한 분기점
        if (registrationId.equals("google")) {

            oAuth2ResponseDto = new GoogleResponseDto(oAuth2User.getAttributes());
        }
        else {

            return null;
        }

        // 리소스 서버에서 발급 받은 정보로 유저의 특정 아이디값 생성
        String oauthName = oAuth2ResponseDto.getProvider() + " " + oAuth2ResponseDto.getProviderId();

        // DB에 존재하는 oauth 로그인 계정인지 확인
        User existUser = userRepository.findByOauthName(oauthName);

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

            return new CustomOAuth2User(OAuthUserDto);
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

            return new CustomOAuth2User(OAuthUserDto);
        }
    }
}