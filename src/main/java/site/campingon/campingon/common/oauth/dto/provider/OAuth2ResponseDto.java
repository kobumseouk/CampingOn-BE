package site.campingon.campingon.common.oauth.dto.provider;

// 제공자마다 반환 형태가 달라서 interface 생성. 제공자별 구현체 필요
public interface OAuth2ResponseDto {

    // 제공자 (Ex. google, 네이버)
    String getProvider();

    String getProviderId();

    String getEmail();

    String getName();
}
