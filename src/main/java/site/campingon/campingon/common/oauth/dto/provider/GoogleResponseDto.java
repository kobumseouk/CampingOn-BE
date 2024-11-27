package site.campingon.campingon.common.oauth.dto.provider;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class GoogleResponseDto implements OAuth2ResponseDto {
    private final String provider = "google";
    private final String providerId;
    private final String email;
    private final String name;
    private final String accessToken;
    private final Map<String, Object> attributes;

    public GoogleResponseDto(Map<String, Object> attributes, String accessToken) {
        
        // attributes를 새로 만들어서 토큰 추가
        Map<String, Object> newAttributes = new HashMap<>(attributes);
        newAttributes.put("accessToken", accessToken);
        this.attributes = newAttributes;
        
        this.providerId = String.valueOf(attributes.get("sub"));
        this.email = String.valueOf(attributes.get("email"));
        this.name = String.valueOf(attributes.get("name"));
        this.accessToken = accessToken;
    }
}