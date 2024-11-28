package site.campingon.campingon.common.jwt;

import java.util.Map;

public interface CustomUserPrincipal {
    String getEmail();
    String getNickname();
    String getRole();
    Map<String, Object> getAttributes();

}
