package site.campingon.campingon.common.config.oauth.dto.provider;

import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class GoogleResponseDto implements OAuth2ResponseDto {

    private final Map<String, Object> attribute;

    @Override
    public String getProvider() {

        return "google";
    }

    @Override
    public String getProviderId() {

        return attribute.get("sub").toString();
    }

    @Override
    public String getEmail() {

        return attribute.get("email").toString();
    }

    @Override
    public String getName() {

        return attribute.get("name").toString();
    }
}
