package site.campingon.campingon.common.config.oauth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import site.campingon.campingon.common.config.oauth.dto.OAuth2UserDto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    private final OAuth2UserDto OAuthUserDto;

    @Override
    public Map<String, Object> getAttributes() {

        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return OAuthUserDto.getRole();
            }
        });

        return collection;
    }

    @Override
    public String getName() {

        return OAuthUserDto.getNickname();
    }

    public String getEmail() {

        return OAuthUserDto.getEmail();
    }

    public String getOauthName(){

        return OAuthUserDto.getOauthName();
    }
}
