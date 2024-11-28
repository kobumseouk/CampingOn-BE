package site.campingon.campingon.camp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.geo.Point;
import site.campingon.campingon.camp.dto.CampSiteListResponseDto;
import site.campingon.campingon.camp.dto.CampSiteResponseDto;
import site.campingon.campingon.camp.dto.admin.CampSiteCreateRequestDto;
import site.campingon.campingon.camp.dto.admin.CampSiteUpdateRequestDto;
import site.campingon.campingon.camp.entity.*;
import site.campingon.campingon.camp.mapper.CampSiteMapper;
import site.campingon.campingon.camp.repository.CampRepository;
import site.campingon.campingon.camp.repository.CampSiteRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampSiteServiceTest {

    @InjectMocks
    private CampSiteService campSiteService;

    @Mock
    private CampRepository campRepository;

    @Mock
    private CampSiteMapper campSiteMapper;

    @Mock
    private CampSiteRepository campSiteRepository;

    private Camp camp;

    private Camp mockCamp;
    private CampAddr mockCampAddr;
    private CampInfo mockCampInfo;
    private List<CampInduty> mockCampInduties;
    private CampSite mockCampSite;
    private CampSiteListResponseDto mockCampSiteListDto;

    @BeforeEach
    void setUp() {
        mockCampAddr = CampAddr.builder()
                .id(1L)
                .city("TestCity")
                .state("TestState")
                .zipcode("12345")
                .location(new Point(127.0, 37.0))
                .streetAddr("Test Street")
                .build();

        mockCampInfo = CampInfo.builder()
                .id(1L)
                .recommendCnt(10)
                .bookmarkCnt(5)
                .build();

        mockCampInduties = Arrays.asList(
                CampInduty.builder()
                        .id(1L)
                        .induty(Induty.NORMAL_SITE)
                        .build(),
                CampInduty.builder()
                        .id(2L)
                        .induty(Induty.CAR_SITE)
                        .build()
        );

        mockCamp = Camp.builder()
                .id(1L)
                .campName("Test Camp")
                .tel("010-1234-5678")
                .lineIntro("Test Line Intro")
                .homepage("http://test.com")
                .outdoorFacility("Test Facility")
                .thumbImage("test.jpg")
                .induty(mockCampInduties)
                .campAddr(mockCampAddr)
                .campInfo(mockCampInfo)
                .build();

        mockCamp = Camp.builder()
                .id(1L)
                .campName("Test Camp")
                .tel("010-1234-5678")
                .lineIntro("Test Line Intro")
                .homepage("http://test.com")
                .outdoorFacility("Test Facility")
                .thumbImage("test.jpg")
                .induty(mockCampInduties)
                .campAddr(mockCampAddr)
                .campInfo(mockCampInfo)
                .build();
    }

    @Test
    @DisplayName("TEST - 캠핑지 생성: induty 필드를 단일 필드로 테스트")
    void testCreateCampSiteWithSingleInduty() {
        // Given
        Long campId = 1L;

        // 단일 Induty 설정 (캠핑 유형을 단일 값으로 설정)
        Induty induty = Induty.GLAMP_SITE;

        // CampSiteCreateRequestDto를 생성할 때 단일 Induty를 사용하여 생성
        CampSiteCreateRequestDto createDto = CampSiteCreateRequestDto.builder()
                .siteType(induty)
                .price(2000)
                .maximumPeople(4)
                .indoorFacility("전기, 수도, 와이파이")
                .isAvailable(true)
                .build();


        // CampSite 엔티티 생성 (단일 Induty 설정)
        CampSite campSite = CampSite.builder()
                .id(1L)
                .camp(mockCamp)
                .siteType(induty)
                .price(2000)
                .maximumPeople(4)
                .indoorFacility("전기, 수도, 와이파이")
                .isAvailable(true)
                .build();



        // Mock 설정
        when(campRepository.findById(1L)).thenReturn(Optional.of(mockCamp));
        when(campRepository.existsById(1L)).thenReturn(true);
        when(campSiteMapper.toCampSite(createDto, mockCamp)).thenReturn(campSite);
        when(campSiteRepository.save(any(CampSite.class))).thenReturn(campSite);
        when(campSiteMapper.toCampSiteResponseDto(campSite))
                .thenReturn(CampSiteResponseDto.builder()
                        .siteId(campSite.getId())
                        .siteType(campSite.getSiteType())
                        .price(campSite.getPrice())
                        .maximumPeople(campSite.getMaximumPeople())
                        .indoorFacility(campSite.getIndoorFacility())
                        .isAvailable(campSite.isAvailable())
                        .build());

        // When
        CampSiteResponseDto responseDto = campSiteService.createCampSite(campId, createDto);

        // Then
        assertNotNull(responseDto);
        assertEquals(induty, responseDto.getSiteType());
        verify(campRepository, times(1)).existsById(campId);
        verify(campSiteRepository, times(1)).save(any(CampSite.class));
    }

    @Test
    @DisplayName("TEST - 캠핑지 수정")
    void testUpdateCampSite() {
        // Given
        Long campId = 1L;
        Long siteId = 1L;

        CampSiteUpdateRequestDto updateRequestDto = CampSiteUpdateRequestDto.builder()
                .siteType(Induty.CAR_SITE)
                .price(3000)
                .maximumPeople(5)
                .indoorFacility("수도, 와이파이")
                .isAvailable(false)
                .build();

        CampSite existingCampSite = CampSite.builder()
                .id(siteId)
                .camp(mockCamp)
                .siteType(Induty.GLAMP_SITE)
                .price(2000)
                .maximumPeople(4)
                .indoorFacility("전기, 수도, 와이파이")
                .isAvailable(true)
                .build();

        CampSite updatedCampSite = CampSite.builder()
                .id(siteId)
                .camp(mockCamp)
                .siteType(updateRequestDto.getSiteType())
                .price(updateRequestDto.getPrice())
                .maximumPeople(updateRequestDto.getMaximumPeople())
                .indoorFacility(updateRequestDto.getIndoorFacility())
                .isAvailable(updateRequestDto.isAvailable())
                .build();

        when(campRepository.existsById(campId)).thenReturn(true);
        when(campSiteRepository.findByIdAndCampId(siteId, campId)).thenReturn(Optional.of(existingCampSite));
        when(campSiteRepository.save(any(CampSite.class))).thenReturn(updatedCampSite);
        when(campSiteMapper.toCampSiteResponseDto(updatedCampSite))
                .thenReturn(CampSiteResponseDto.builder()
                        .siteId(updatedCampSite.getId())
                        .siteType(updatedCampSite.getSiteType())
                        .price(updatedCampSite.getPrice())
                        .maximumPeople(updatedCampSite.getMaximumPeople())
                        .indoorFacility(updatedCampSite.getIndoorFacility())
                        .isAvailable(updatedCampSite.isAvailable())
                        .build());

        // When
        CampSiteResponseDto responseDto = campSiteService.updateCampSite(campId, siteId, updateRequestDto);

        // Then
        assertNotNull(responseDto);
        assertEquals(updateRequestDto.getSiteType(), responseDto.getSiteType());
        verify(campRepository, times(1)).existsById(campId);
        verify(campSiteRepository, times(1)).findByIdAndCampId(siteId, campId);
        verify(campSiteRepository, times(1)).save(any(CampSite.class));
    }

    @Test
    @DisplayName("TEST - 캠핑지 삭제")
    void testDeleteCampSite() {
        // Given
        Long campId = 1L;
        Long siteId = 1L;

        when(campSiteRepository.existsById(siteId)).thenReturn(true);

        // When
        campSiteService.deleteCampSite(campId, siteId);

        // Then
        verify(campSiteRepository, times(1)).existsById(siteId);
        verify(campSiteRepository, times(1)).deleteByIdAndCampId(siteId, campId);

    }

    @Test
    @DisplayName("TEST - 캠핑지 전체 조회")
    void testGetCampSites() {
        // Given
        Long campId = 1L;

        CampSite campSite1 = CampSite.builder()
                .id(1L)
                .camp(mockCamp)
                .siteType(Induty.NORMAL_SITE)
                .price(2000)
                .maximumPeople(4)
                .indoorFacility("전기, 수도")
                .isAvailable(true)
                .build();

        CampSite campSite2 = CampSite.builder()
                .id(2L)
                .camp(mockCamp)
                .siteType(Induty.CAR_SITE)
                .price(5000)
                .maximumPeople(6)
                .indoorFacility("수도, 와이파이")
                .isAvailable(false)
                .build();

        List<CampSite> campSites = Arrays.asList(campSite1, campSite2);

        when(campSiteRepository.findAllByCampId(campId)).thenReturn(campSites);
        when(campSiteMapper.toCampSiteListResponseDto(any(CampSite.class))).thenReturn(
                CampSiteListResponseDto.builder().build());

        // When
        List<CampSiteListResponseDto> responseDtos = campSiteService.getCampSites(campId);

        // Then
        assertNotNull(responseDtos);
        assertEquals(2, responseDtos.size());
        verify(campSiteRepository, times(1)).findAllByCampId(campId);
    }

    @Test
    @DisplayName("TEST - 캠핑장의 캠핑지 전체 목록 조회")
    void testGetCampInSites() {
        // Given
        Long campId = 1L;

        CampSite campSite1 = CampSite.builder()
                .id(1L)
                .camp(mockCamp)
                .siteType(Induty.NORMAL_SITE)
                .price(2000)
                .maximumPeople(4)
                .indoorFacility("전기, 수도")
                .isAvailable(true)
                .build();

        CampSite campSite2 = CampSite.builder()
                .id(2L)
                .camp(mockCamp)
                .siteType(Induty.CAR_SITE)
                .price(5000)
                .maximumPeople(6)
                .indoorFacility("수도, 와이파이")
                .isAvailable(false)
                .build();

        List<CampSite> campSites = Arrays.asList(campSite1, campSite2);

        when(campSiteRepository.findByCampId(campId)).thenReturn(campSites);
        when(campSiteMapper.toCampSiteListDto(any(CampSite.class))).thenReturn(
                CampSiteListResponseDto.builder().build());

        // When
        List<CampSiteListResponseDto> responseDtos = campSiteService.getCampInSites(campId);

        // Then
        assertNotNull(responseDtos);
        assertEquals(2, responseDtos.size());
        verify(campSiteRepository, times(1)).findByCampId(campId);
    }

    @Test
    @DisplayName("TEST - 특정 캠핑지 조회")
    void testGetCampSite() {
        // Given
        Long campId = 1L;
        Long siteId = 1L;

        CampSite campSite = CampSite.builder()
                .id(siteId)
                .camp(mockCamp)
                .siteType(Induty.NORMAL_SITE)
                .price(2000)
                .maximumPeople(4)
                .indoorFacility("전기, 수도")
                .isAvailable(true)
                .build();

        when(campSiteRepository.findByIdAndCampId(siteId, campId)).thenReturn(Optional.of(campSite));
        when(campSiteMapper.toCampSiteResponseDto(campSite))
                .thenReturn(CampSiteResponseDto.builder()
                        .siteId(campSite.getId())
                        .siteType(campSite.getSiteType())
                        .price(campSite.getPrice())
                        .maximumPeople(campSite.getMaximumPeople())
                        .indoorFacility(campSite.getIndoorFacility())
                        .build());

        // When
        CampSiteResponseDto responseDto = campSiteService.getCampSite(campId, siteId);

        // Then
        assertNotNull(responseDto);
        assertEquals(siteId, responseDto.getSiteId());
        verify(campSiteRepository, times(1)).findByIdAndCampId(siteId, campId);
    }

//    @Test
//    @DisplayName("캠핑장의 캠핑지 전체 목록 조회 성공 확인 테스트")
//    void getCampInSites_success() {
//        // given
//        Long campId = 1L;
//        List<CampSite> campSites = Arrays.asList(mockCampSite);
//
//        when(campSiteRepository.findByCampId(campId)).thenReturn(campSites);
//        when(campSiteMapper.toCampSiteListDto(any(CampSite.class))).thenReturn(mockCampSiteListDto);
//
//        // when
//        List<CampSiteListResponseDto> result = campSiteService.getCampInSites(campId);
//
//        // then
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        assertEquals(mockCampSiteListDto, result.get(0));
//        assertEquals(mockCampSiteListDto.getSiteId(), result.get(0).getSiteId());
//        assertEquals(mockCampSiteListDto.getSiteType(), result.get(0).getSiteType());
//        assertEquals(mockCampSiteListDto.getMaximumPeople(), result.get(0).getMaximumPeople());
//
//        verify(campSiteRepository).findByCampId(campId);
//        verify(campSiteMapper).toCampSiteListDto(any(CampSite.class));
//    }

    @Test
    @DisplayName("존재하지 않는 캠핑장의 캠핑지 목록 조회 시 빈 리스트 반환 확인 테스트")
    void getCampInSites_emptySites_returnsEmptyList() {
        // given
        Long nonExistentCampId = 999L;

        when(campSiteRepository.findByCampId(nonExistentCampId)).thenReturn(Collections.emptyList());

        // when
        List<CampSiteListResponseDto> result = campSiteService.getCampInSites(nonExistentCampId);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(campSiteRepository).findByCampId(nonExistentCampId);
        verify(campSiteMapper, never()).toCampSiteListDto(any(CampSite.class));
    }

}