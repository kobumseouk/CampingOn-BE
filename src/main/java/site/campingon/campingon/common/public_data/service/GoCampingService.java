package site.campingon.campingon.common.public_data.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import site.campingon.campingon.camp.entity.*;
import site.campingon.campingon.camp.repository.*;
import site.campingon.campingon.common.public_data.GoCampingPath;
import site.campingon.campingon.common.public_data.dto.GoCampingDataDto;
import site.campingon.campingon.common.public_data.dto.GoCampingParsedResponseDto;
import site.campingon.campingon.common.public_data.mapper.GoCampingMapper;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static site.campingon.campingon.common.public_data.PublicDataConstants.*;
import static site.campingon.campingon.common.public_data.PublicDataConstants.MOBILE_APP;

@Service
@RequiredArgsConstructor
public class GoCampingService {

    private final GoCampingMapper goCampingMapper;
    private final CampAddrRepository campAddrRepository;
    private final CampImageRepository campImageRepository;
    private final CampInfoRepository campInfoRepository;
    private final CampKeywordRepository campKeywordRepository;
    private final CampRepository campRepository;
    private final CampSiteRepository campSiteRepository;
    private final CampIndutyRepository campIndutyRepository;

    @Value("${public-data.go-camping}")
    private String serviceKey;

    //todo 생성일, 수정일 엔티티 주입하기
    public List<GoCampingParsedResponseDto> publicDataFilters(GoCampingDataDto request) {
        List<GoCampingDataDto.Item> items = request.getResponse().getBody().getItems().getItem();
        List<GoCampingParsedResponseDto> goCampingParsedResponseDtoList = goCampingMapper.toGoCampingResponseDtoList(items);

        for (GoCampingParsedResponseDto data : goCampingParsedResponseDtoList) {
            Integer normalSiteCnt = data.getGnrlSiteCo();//주요시설 일반야영장
            Integer carSiteCnt = data.getAutoSiteCo();//주요시설 자동차야영장
            Integer glampSiteCnt = data.getGlampSiteCo();//주요시설 글램핑
            Integer caravSiteCnt = data.getCaravSiteCo();//주요시설 카라반
            Integer personalCaravanSiteCnt = data.getIndvdlCaravSiteCo();//주요시설 개인 카라반
            String glampInnerFacility = data.getGlampInnerFclty();//글램핑 - 내부시설
            String caravInnerFacility = data.getCaravInnerFclty();//카라반 - 내부시설

            Camp camp = Camp.builder()
//                    .id(data.getContentId())  //엔티티 autoIncrement 전략
                    .campName(data.getFacltNm())
                    .lineIntro(data.getLineIntro())
                    .intro(data.getIntro())
                    .tel(data.getTel())
                    .homepage(data.getHomepage())
                    .outdoorFacility(data.getSbrsCl())
                    .thumbImage(data.getFirstImageUrl())
                    .build();

            campRepository.save(camp);

            createCampInduty(camp,normalSiteCnt, carSiteCnt, glampSiteCnt, caravSiteCnt, personalCaravanSiteCnt);

            CampAddr campAddr = CampAddr.builder()
                    .camp(camp)
                    .city(data.getDoNm())
                    .state(data.getSigunguNm())
                    .zipcode(data.getZipcode())
                    .streetAddr(data.getAddr1())
                    .detailedAddr(data.getAddr2())
                    .longitude(data.getMapX())  //x좌표
                    .latitude(data.getMapY())   //y좌표
                    .build();
            campAddrRepository.save(campAddr);

            //캠핑지 DB 저장
            createCampSite(camp, normalSiteCnt, Induty.NORMAL_SITE, null, 6, 25000);
            createCampSite(camp, carSiteCnt, Induty.CAR_SITE, null, 6, 35000);
            createCampSite(camp, glampSiteCnt, Induty.GLAMP_SITE, glampInnerFacility, 4, 70000);
            createCampSite(camp, caravSiteCnt, Induty.CARAV_SITE, caravInnerFacility, 4, 80000);
            createCampSite(camp, personalCaravanSiteCnt, Induty.PERSONAL_CARAV_SITE, null, 6, 35000);

        }
        return goCampingParsedResponseDtoList;
    }

    private void createCampInduty(Camp camp, Integer normalSiteCnt, Integer carSiteCnt, Integer glampSiteCnt, Integer caravSiteCnt, Integer personalCaravanSiteCnt) {

        if (normalSiteCnt != 0) {
            CampInduty campInduty = CampInduty.builder()
                    .induty(Induty.NORMAL_SITE).camp(camp).build();
            campIndutyRepository.save(campInduty);
        }

        if (carSiteCnt != 0) {
            CampInduty campInduty = CampInduty.builder()
                    .induty(Induty.CAR_SITE).camp(camp).build();
            campIndutyRepository.save(campInduty);
        }

        if (glampSiteCnt != 0) {
            CampInduty campInduty = CampInduty.builder()
                    .induty(Induty.GLAMP_SITE).camp(camp).build();
            campIndutyRepository.save(campInduty);
        }

        if (caravSiteCnt != 0) {
            CampInduty campInduty = CampInduty.builder()
                    .induty(Induty.CARAV_SITE).camp(camp).build();
            campIndutyRepository.save(campInduty);
        }

        if (personalCaravanSiteCnt != 0) {
            CampInduty campInduty = CampInduty.builder()
                    .induty(Induty.PERSONAL_CARAV_SITE).camp(camp).build();
            campIndutyRepository.save(campInduty);
        }
    }

    //CampSite 생성 및 DB 저장
    private void createCampSite(Camp camp, Integer normalSiteCnt,
                                Induty induty, String innerFacility,
                                int maximum_people, int price) {
        for (int i = 0; i < normalSiteCnt; ++i) {
            CampSite campSite = CampSite.builder()
                    .camp(camp)
                    .maximumPeople(maximum_people)
                    .price(price)
                    .type(induty)
                    .indoorFacility(innerFacility)
                    .build();

            campSiteRepository.save(campSite);
        }
    }

    public GoCampingDataDto goCampingDataDtoByGoCampingUrl(
            GoCampingPath goCampingPath, String... params
    ) throws URISyntaxException {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(
                        GO_CAMPING_END_POINT + goCampingPath.getPath())
                .queryParam("_type", CONTENT_TYPE)
                .queryParam("MobileOS", MOBILE_OS)
                .queryParam("MobileApp", MOBILE_APP)
                .queryParam("serviceKey", serviceKey);

        for (int i = 0; i < params.length; i += 2) {
            uriBuilder.queryParam(params[i], params[i + 1]);
        }

        RestTemplate restTemplate = new RestTemplate();
        URI uri = new URI(uriBuilder.build().toUriString());

        return restTemplate.getForObject(uri, GoCampingDataDto.class);
    }
}
