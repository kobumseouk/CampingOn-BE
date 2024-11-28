package site.campingon.campingon.common.public_data.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import site.campingon.campingon.camp.entity.*;
import site.campingon.campingon.camp.repository.*;
import site.campingon.campingon.common.exception.ErrorCode;
import site.campingon.campingon.common.exception.GlobalException;
import site.campingon.campingon.common.public_data.GoCampingPath;
import site.campingon.campingon.common.public_data.dto.GoCampingDataDto;
import site.campingon.campingon.common.public_data.dto.GoCampingImageDto;
import site.campingon.campingon.common.public_data.dto.GoCampingImageParsedResponseDto;
import site.campingon.campingon.common.public_data.dto.GoCampingParsedResponseDto;
import site.campingon.campingon.common.public_data.mapper.GoCampingMapper;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static site.campingon.campingon.camp.entity.Induty.*;
import static site.campingon.campingon.common.public_data.PublicDataConstants.*;
import static site.campingon.campingon.common.public_data.PublicDataConstants.MOBILE_APP;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoCampingService {

    private final GoCampingMapper goCampingMapper;
    private final CampAddrRepository campAddrRepository;
    private final CampImageRepository campImageRepository;
    private final CampInfoRepository campInfoRepository;
    private final CampKeywordRepository campKeywordRepository;
    private final CampRepository campRepository;
    private final CampSiteRepository campSiteRepository;
    private final CampIndutyRepository campIndutyRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String IMAGE_PAGE_NO = "1";    //이미지 몇번부터 값 꺼내올지

    @Value("${public-data.go-camping}")
    private String serviceKey;

    //Camp 관련 엔티티 생성 및 DB 저장 메서드
    public List<GoCampingParsedResponseDto> createCampByGoCampingData(GoCampingDataDto goCampingDataDto) {
        List<GoCampingDataDto.Item> items = goCampingDataDto.getResponse().getBody().getItems().getItem();
        List<GoCampingParsedResponseDto> goCampingParsedResponseDtoList = goCampingMapper.toGoCampingParsedResponseDtoList(items);

        for (GoCampingParsedResponseDto data : goCampingParsedResponseDtoList) {
            Integer normalSiteCnt = data.getGnrlSiteCo();//주요시설 일반야영장
            Integer carSiteCnt = data.getAutoSiteCo();//주요시설 자동차야영장
            Integer glampSiteCnt = data.getGlampSiteCo();//주요시설 글램핑
            Integer caravSiteCnt = data.getCaravSiteCo();//주요시설 카라반
            Integer personalCaravanSiteCnt = data.getIndvdlCaravSiteCo();//주요시설 개인 카라반
            String glampInnerFacility = data.getGlampInnerFclty();//글램핑 - 내부시설
            String caravInnerFacility = data.getCaravInnerFclty();//카라반 - 내부시설

            //데이터에 주요시설이 단, 한개도 없는 경우 DB 생성하지않는다.
            if (normalSiteCnt + caravSiteCnt
                    + glampSiteCnt + caravSiteCnt
                    + personalCaravanSiteCnt == 0) {
                continue;
            }

            Camp camp = Camp.builder()
                    .id(data.getContentId())  //엔티티 autoIncrement 전략
                    .campName(data.getFacltNm())
                    .lineIntro(data.getLineIntro())
                    .intro(data.getIntro())
                    .tel(data.getTel())
                    .homepage(data.getHomepage())
                    .outdoorFacility(data.getSbrsCl())
                    .thumbImage(data.getFirstImageUrl())
                    .createdAt(
                            LocalDateTime.parse(
                                    data.getCreatedtime()
                                    , DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    )
                    .modifiedAt(LocalDateTime.parse(
                            data.getModifiedtime()
                            , DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    )
                    .build();

            campRepository.save(camp);

            createCampInduty(camp, normalSiteCnt, carSiteCnt, glampSiteCnt, caravSiteCnt, personalCaravanSiteCnt);

            CampAddr campAddr = CampAddr.builder()
                    .camp(camp)
                    .city(data.getDoNm())
                    .state(data.getSigunguNm())
                    .zipcode(data.getZipcode())
                    .streetAddr(data.getAddr1())
                    .detailedAddr(data.getAddr2())
                    .location(new Point(data.getMapX(), data.getMapY()))
                    .build();
            campAddrRepository.save(campAddr);

            //캠핑지 DB 저장
            createCampSite(camp, normalSiteCnt, NORMAL_SITE, null,
                    NORMAL_SITE.getMaximum_people(), NORMAL_SITE.getPrice());

            createCampSite(camp, carSiteCnt, CAR_SITE, null,
                    CAR_SITE.getMaximum_people(), CAR_SITE.getPrice());

            createCampSite(camp, glampSiteCnt, GLAMP_SITE, glampInnerFacility,
                    GLAMP_SITE.getMaximum_people(), GLAMP_SITE.getPrice());

            createCampSite(camp, caravSiteCnt, CARAV_SITE, caravInnerFacility,
                    CAR_SITE.getMaximum_people(), CAR_SITE.getPrice());

            createCampSite(camp, personalCaravanSiteCnt, PERSONAL_CARAV_SITE,
                    null, PERSONAL_CARAV_SITE.getMaximum_people(), PERSONAL_CARAV_SITE.getPrice());

        }
        return goCampingParsedResponseDtoList;
    }

    //공공데이터 전체 API 조회하고 dto 변환
    public GoCampingDataDto getAndConvertToGoCampingDataDto(
            String... params
    ) throws URISyntaxException {
        URI uri = publicDataFilters(GoCampingPath.BASED_LIST, params);

        return restTemplate.getForObject(uri, GoCampingDataDto.class);  //API 호출
    }

    //공공데이터 이미지 API 조회하고 dto 변환
    public List<GoCampingImageDto> getAndConvertToGoCampingDataDto(
            Long imageCnt)
            throws URISyntaxException {
        List<GoCampingImageDto> goCampingDataDtoList = new ArrayList<>();

        List<Long> campIdList = campRepository.findAll().stream()
                .map(Camp::getId)
                .toList();

        for (Long campId : campIdList) {
            URI uri = publicDataFilters(GoCampingPath.IMAGE_LIST,
                    "numOfRows", imageCnt.toString(),
                    "pageNo", IMAGE_PAGE_NO,  //몇번부터 시작할지
                    "contentId", campId.toString());

            goCampingDataDtoList.add(
                    restTemplate.getForObject(uri, GoCampingImageDto.class)); //API 호출
        }
        return goCampingDataDtoList;
    }

    //CampImage 를 생성 및 DB 저장 메서드
    public List<List<GoCampingImageParsedResponseDto>> createCampImageByGoCampingImageData(
            List<GoCampingImageDto> goCampingImageDto) {
        List<List<GoCampingImageParsedResponseDto>> goCampingImageParsedResponseDtoList = new ArrayList<>();

        for (GoCampingImageDto goCampingDataDto : goCampingImageDto) {
            List<GoCampingImageDto.Item> item
                    = goCampingDataDto.getResponse().getBody().getItems().getItem();

            List<GoCampingImageParsedResponseDto> goCampingImageParsedResponseDto =
                    goCampingMapper.toGoCampingImageParsedResponseDtoList(item);

            Camp camp = campRepository.findById(
                            goCampingImageParsedResponseDto.getFirst().getContentId()
                    )
                    .orElseThrow(() -> new GlobalException(ErrorCode.CAMP_NOT_FOUND_BY_ID));
            for (GoCampingImageParsedResponseDto data : goCampingImageParsedResponseDto) {
                CampImage campImage = CampImage.builder()
                        .id(data.getSerialnum())
                        .camp(camp)
                        .imageUrl(data.getImageUrl())
                        .build();

                campImageRepository.save(campImage);
            }
            goCampingImageParsedResponseDtoList.add(goCampingImageParsedResponseDto);
        }
        return goCampingImageParsedResponseDtoList;
    }

    //CampSite 데이터 삽입
    private void createCampSite(Camp camp, Integer siteCnt,
                                Induty induty, String innerFacility,
                                int maximum_people, int price) {
        for (int i = 0; i < siteCnt; ++i) {
            CampSite campSite = CampSite.builder()
                    .camp(camp)
                    .maximumPeople(maximum_people)
                    .price(price)
                    .siteType(induty)
                    .indoorFacility(innerFacility)
                    .build();

            campSiteRepository.save(campSite);
        }
    }

    //CampInduty 데이터 삽입
    private void createCampInduty(Camp camp, Integer normalSiteCnt,
                                  Integer carSiteCnt, Integer glampSiteCnt,
                                  Integer caravSiteCnt, Integer personalCaravanSiteCnt) {

        Map<Induty, Integer> siteCounts = Map.of(
                NORMAL_SITE, normalSiteCnt,
                CAR_SITE, carSiteCnt,
                GLAMP_SITE, glampSiteCnt,
                CARAV_SITE, caravSiteCnt,
                PERSONAL_CARAV_SITE, personalCaravanSiteCnt
        );

        siteCounts.forEach((induty, count) -> {
            if (count != 0) {
                CampInduty campInduty = CampInduty.builder()
                        .induty(induty)
                        .camp(camp)
                        .build();
                campIndutyRepository.save(campInduty);
            }
        });
    }

    //공공데이터 URI 작업 메서드
    private URI publicDataFilters(GoCampingPath goCampingPath, String... params)
            throws URISyntaxException {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(
                        GO_CAMPING_END_POINT + goCampingPath.getPath())
                .queryParam("_type", CONTENT_TYPE)
                .queryParam("MobileOS", MOBILE_OS)
                .queryParam("MobileApp", MOBILE_APP)
                .queryParam("serviceKey", serviceKey);

        for (int i = 0; i < params.length; i += 2) {
            uriBuilder.queryParam(params[i], params[i + 1]);
        }

        return new URI(uriBuilder.build().toUriString());
    }
}
