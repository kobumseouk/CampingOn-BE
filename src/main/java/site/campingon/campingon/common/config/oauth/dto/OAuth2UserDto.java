package site.campingon.campingon.common.config.oauth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuth2UserDto {

    private String oauthName;
    private String nickname;
    private String email;
    private String role;
}
