package site.campingon.campingon.common.jwt;

import java.util.Collection;
import java.util.Collections;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import site.campingon.campingon.user.entity.Role;

@Getter
public class CustomUserDetails implements UserDetails {
    private final Long id;
    private final String email;
    private final String nickname;
    private final Role role;
    private final String password;

    public CustomUserDetails(Long id, String email, String nickname, Role role, String password) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.role = role;
        this.password = password;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.name())); // 문자열 기반 권한
    }


    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}