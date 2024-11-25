package site.campingon.campingon.common.public_data.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.campingon.campingon.camp.entity.Camp;
import site.campingon.campingon.camp.entity.CampAddr;
import site.campingon.campingon.camp.entity.CampSite;
import site.campingon.campingon.camp.repository.*;
import site.campingon.campingon.common.public_data.dto.GoCampingRequestDto;
import site.campingon.campingon.common.public_data.dto.GoCampingResponseDto;
import site.campingon.campingon.common.public_data.mapper.GoCampingMapper;

import java.util.List;

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

    //todo 생성일, 수정일 엔티티 주입하기

    public GoCampingResponseDto publicDataFilter(GoCampingRequestDto request) {
        GoCampingRequestDto.Item item = request.getResponse().getBody().getItems().getItem().getFirst();

        return goCampingMapper.toGoCampingResponseDto(item);
    }

    public List<GoCampingResponseDto> publicDataFilters(GoCampingRequestDto request) {
        List<GoCampingRequestDto.Item> items = request.getResponse().getBody().getItems().getItem();
        List<GoCampingResponseDto> goCampingResponseDtoList = goCampingMapper.toGoCampingResponseDtoList(items);

        for (GoCampingResponseDto data : goCampingResponseDtoList) {
            Camp camp = Camp.builder()
//                    .id(data.getContentId())
                    .campName(data.getFacltNm())
                    .lineIntro(data.getLineIntro())
                    .intro(data.getIntro())
                    .tel(data.getTel())
                    .homepage(data.getHomepage())
                    .outdoorFacility(data.getSbrsCl())
                    .thumbImage(data.getFirstImageUrl())
                    .build();

            campRepository.save(camp);

            CampAddr campAddr = CampAddr.builder()
                    .camp(camp)
                    .city(data.getDoNm())
                    .state(data.getSigunguNm())
                    .zipcode(data.getZipcode())
                    .streetAddr(data.getAddr1())
                    .detailedAddr(data.getAddr2())
                    .build();

            campAddrRepository.save(campAddr);

            Integer normalSiteCnt = data.getGnrlSiteCo();//주요시설 일반야영장
            Integer carSiteCnt = data.getAutoSiteCo();//주요시설 자동차야영장
            Integer glampSiteCnt = data.getGlampSiteCo();//주요시설 글램핑
            Integer caravSiteCnt = data.getCaravSiteCo();//주요시설 카라반
            Integer personalCaravanSiteCnt = data.getIndvdlCaravSiteCo();//주요시설 개인 카라반
            String glampInnerFacility = data.getGlampInnerFclty();//글램핑 - 내부시설
            String caravInnerFacility = data.getCaravInnerFclty();//카라반 - 내부시설

            createCampSite(camp, normalSiteCnt, "일반야영장", "일반야영", null);
            createCampSite(camp, carSiteCnt, "자동차야영장", "자동차야영", null);
            createCampSite(camp, glampSiteCnt, "글램핑장", "글램핑", glampInnerFacility);
            createCampSite(camp, caravSiteCnt, "카라반", "카라반", caravInnerFacility);
            createCampSite(camp, personalCaravanSiteCnt, "개인카라반", "카라반", null);

        }
        return goCampingResponseDtoList;
    }

    //CampSite 생성 및 DB 저장
    private void createCampSite(Camp camp, Integer normalSiteCnt, String roomName, String induty, String innerFacility) {
        for (int i = 0; i < normalSiteCnt; ++i) {
            CampSite campSite = CampSite.builder()
                    .camp(camp)
                    .maximumPeople(4)   //todo 임시설정
                    .price(1000)    //todo 임시설정
                    .type(induty)
                    .indoorFacility(innerFacility)
                    .build();

            campSiteRepository.save(campSite);
        }
    }
}
