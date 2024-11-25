package site.campingon.campingon.common.config.oauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import site.campingon.campingon.common.config.oauth.dto.provider.GoogleResponseDto;
import site.campingon.campingon.common.config.oauth.dto.provider.OAuth2ResponseDto;
import site.campingon.campingon.common.config.oauth.dto.OAuth2UserDto;
import site.campingon.campingon.user.entity.Role;
import site.campingon.campingon.user.entity.User;
import site.campingon.campingon.user.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        log.info("\n\n\n[oAuth2User확인]\n" + oAuth2User + "\n\n\n");

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
            User newUser = User.builder()
                    .email(oAuth2ResponseDto.getEmail())
                    .oauthName(oauthName)
                    .nickname(oAuth2ResponseDto.getName())
                    .role(Role.USER)
                    .build();

            userRepository.save(newUser);

            OAuth2UserDto OAuthUserDto = OAuth2UserDto.builder()
                    .oauthName(oauthName)
                    .email(oAuth2ResponseDto.getEmail())
                    .nickname(oAuth2ResponseDto.getName())
                    .role(String.valueOf(Role.USER))
                    .build();

            return new CustomOAuth2User(OAuthUserDto);
        }

        // 기존에 로그인 한 적 o -> 업데이트 할 부분이 있는지 체크
        else {


            existUser.toBuilder()
                    .email(oAuth2ResponseDto.getEmail())
                    .nickname(oAuth2ResponseDto.getName())
                    .build();

            userRepository.save(existUser);

            OAuth2UserDto OAuthUserDto = OAuth2UserDto.builder()
                    .oauthName(existUser.getOauthName())
                    .email(existUser.getEmail())
                    .nickname(existUser.getNickname())
                    .role(String.valueOf(existUser.getRole()))
                    .build();

            return new CustomOAuth2User(OAuthUserDto);
        }
    }
}