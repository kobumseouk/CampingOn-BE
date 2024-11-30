package site.campingon.campingon.common.public_data.service;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.Optional;

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
    private final CampRepository campRepository;
    private final CampSiteRepository campSiteRepository;
    private final CampIndutyRepository campIndutyRepository;
    private final RestTemplate restTemplate;
    private static final String IMAGE_PAGE_NO = "1";    //이미지 몇번부터 값 꺼내올지

    @Value("${public-data.go-camping}")
    private String serviceKey;

    //Camp 관련 엔티티 생성 및 DB 저장 메서드
    @Transactional
    public List<GoCampingParsedResponseDto> createCampByGoCampingData(GoCampingDataDto goCampingDataDto) throws ParseException {
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
            if (normalSiteCnt + carSiteCnt
                    + glampSiteCnt + caravSiteCnt
                    + personalCaravanSiteCnt == 0) {
                continue;
            }

            Camp camp = Camp.builder()
                    .id(data.getContentId())
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

            createOrUpdateCampInduty(camp, normalSiteCnt, carSiteCnt, glampSiteCnt, caravSiteCnt, personalCaravanSiteCnt);

            String pointWKT = String.format("POINT(%f %f)", data.getMapY(), data.getMapX());

            if (campAddrRepository.findByCampId(data.getContentId()).isPresent()) {
                //update
                campAddrRepository.updateWithPoint(
                        camp.getId(),
                        data.getDoNm(),
                        data.getSigunguNm(),
                        data.getZipcode(),
                        data.getAddr1(),
                        data.getAddr2(),
                        pointWKT
                );

                updateCampSite(camp, normalSiteCnt, NORMAL_SITE, null,
                        NORMAL_SITE.getMaximum_people(), NORMAL_SITE.getPrice());

                updateCampSite(camp, carSiteCnt, CAR_SITE, null,
                        CAR_SITE.getMaximum_people(), CAR_SITE.getPrice());

                updateCampSite(camp, glampSiteCnt, GLAMP_SITE, glampInnerFacility,
                        GLAMP_SITE.getMaximum_people(), GLAMP_SITE.getPrice());

                updateCampSite(camp, caravSiteCnt, CARAV_SITE, caravInnerFacility,
                        CAR_SITE.getMaximum_people(), CAR_SITE.getPrice());

                updateCampSite(camp, personalCaravanSiteCnt, PERSONAL_CARAV_SITE,
                        null, PERSONAL_CARAV_SITE.getMaximum_people(), PERSONAL_CARAV_SITE.getPrice());
            } else {
                //create
                campAddrRepository.saveWithPoint(
                        camp.getId(),
                        data.getDoNm(),
                        data.getSigunguNm(),
                        data.getZipcode(),
                        data.getAddr1(),
                        data.getAddr2(),
                        pointWKT
                );

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


        }
        return goCampingParsedResponseDtoList;
    }

    @Transactional
    public void updateCampSite(Camp camp, Integer siteCnt,
                               Induty induty, String innerFacility,
                               int maximumPeople, int price) {
        // 특정 Camp와 Induty에 대한 모든 CampSite 조회
        List<CampSite> existingCampSites = campSiteRepository.findAllByCampAndSiteType(camp, induty);

        int existingCount = existingCampSites.size();

        if (existingCount < siteCnt) {
            //기존 보다 campSite 수가 많다면 부족한 campSite 추가
            for (int i = 0; i < siteCnt - existingCount; i++) {
                CampSite newCampSite = CampSite.builder()
                        .camp(camp)
                        .maximumPeople(maximumPeople)
                        .price(price)
                        .siteType(induty)
                        .indoorFacility(innerFacility)
                        .build();
                campSiteRepository.save(newCampSite);
            }
        } else if (existingCount > siteCnt) {
            // 초과됐다면 CampSite 삭제
            List<CampSite> toRemove = existingCampSites.subList(0, existingCount - siteCnt); //초과된 개수만큼 삭제
            campSiteRepository.deleteAll(toRemove);
        }

        // 나머지 CampSite 정보 업데이트
        for (CampSite campSite : existingCampSites) {
            campSite.updateCampSite(camp, maximumPeople, price, induty, innerFacility);
        }
    }

    //공공데이터 전체 API 조회하고 dto 변환
    public GoCampingDataDto getAndConvertToGoCampingDataDto(
            String... params
    ) throws URISyntaxException {
        URI uri = createUri(GoCampingPath.BASED_LIST, params);

        return restTemplate.getForObject(uri, GoCampingDataDto.class);  //API 호출
    }

    //공공데이터 이미지 API 조회하고 dto 변환
    public List<GoCampingImageDto> getAndConvertToGoCampingImageDataDto(
            Long imageCnt)
            throws URISyntaxException {
        List<GoCampingImageDto> goCampingDataDtoList = new ArrayList<>();

        List<Long> campIdList = campRepository.findAll().stream()
                .map(Camp::getId)
                .toList();

        for (Long campId : campIdList) {
            URI uri = createUri(GoCampingPath.IMAGE_LIST,
                    "numOfRows", imageCnt.toString(),
                    "pageNo", IMAGE_PAGE_NO,  //몇번부터 시작할지
                    "contentId", campId.toString());

            goCampingDataDtoList.add(
                    restTemplate.getForObject(uri, GoCampingImageDto.class)); //API 호출
        }
        return goCampingDataDtoList;
    }

    //CampImage 를 생성 및 DB 저장 메서드
    @Transactional
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
    @Transactional
    public void createCampSite(Camp camp, Integer siteCnt,
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
    //todo 업데이트 처리하기
    @Transactional
    public void createOrUpdateCampInduty(Camp camp, Integer normalSiteCnt,
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
    public URI createUri(GoCampingPath goCampingPath, String... params)
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
