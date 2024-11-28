package site.campingon.campingon.camp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.campingon.campingon.camp.dto.CampSiteListResponseDto;
import site.campingon.campingon.camp.entity.Camp;
import site.campingon.campingon.camp.entity.CampSite;
import site.campingon.campingon.camp.entity.Induty;
import site.campingon.campingon.camp.mapper.CampSiteMapper;
import site.campingon.campingon.camp.repository.CampSiteRepository;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampSiteServiceTest {

  @Mock
  private CampSiteRepository campSiteRepository;

  @Mock
  private CampSiteMapper campSiteMapper;

  @InjectMocks
  private CampSiteService campSiteService;

  private Camp mockCamp;
  private CampSite mockCampSite;
  private CampSiteListResponseDto mockCampSiteListDto;

  @BeforeEach
  void setUp() {
    mockCamp = Camp.builder()
        .id(1L)
        .campName("Test Camp")
        .build();

    mockCampSite = CampSite.builder()
        .id(1L)
        .camp(mockCamp)
        .maximumPeople(4)
        .price(50000)
        .siteType(Induty.NORMAL_SITE)
        .indoorFacility("화장실, 취사장")
        .isAvailable(true)
        .build();

    mockCampSiteListDto = CampSiteListResponseDto.builder()
        .siteId(1L)
        .maxPeople(4)
        .price(50000)
        .indoor_facility("화장실, 취사장")
        .type("일반야영장")
        .checkInTime(LocalTime.of(15, 0))
        .checkOutTime(LocalTime.of(11, 0))
        .build();
  }


  @Test
  @DisplayName("캠핑장의 캠핑지 전체 목록 조회 성공 확인 테스트")
  void getCampInSites_success() {
    // given
    Long campId = 1L;
    List<CampSite> campSites = Arrays.asList(mockCampSite);

    when(campSiteRepository.findByCampId(campId)).thenReturn(campSites);
    when(campSiteMapper.toCampSiteListDto(any(CampSite.class))).thenReturn(mockCampSiteListDto);

    // when
    List<CampSiteListResponseDto> result = campSiteService.getCampInSites(campId);

    // then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(mockCampSiteListDto, result.get(0));
    assertEquals(mockCampSiteListDto.getSiteId(), result.get(0).getSiteId());
    assertEquals(mockCampSiteListDto.getType(), result.get(0).getType());
    assertEquals(mockCampSiteListDto.getMaxPeople(), result.get(0).getMaxPeople());

    verify(campSiteRepository).findByCampId(campId);
    verify(campSiteMapper).toCampSiteListDto(any(CampSite.class));
  }

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