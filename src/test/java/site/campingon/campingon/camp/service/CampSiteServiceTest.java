package site.campingon.campingon.camp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.campingon.campingon.camp.dto.CampSiteListResponseDto;
import site.campingon.campingon.camp.dto.CampSiteResponseDto;
import site.campingon.campingon.camp.dto.admin.CampSiteCreateRequestDto;
import site.campingon.campingon.camp.dto.admin.CampSiteUpdateRequestDto;
import site.campingon.campingon.camp.entity.Camp;
import site.campingon.campingon.camp.entity.CampSite;
import site.campingon.campingon.camp.entity.Induty;
import site.campingon.campingon.camp.mapper.CampSiteMapper;
import site.campingon.campingon.camp.repository.CampRepository;
import site.campingon.campingon.camp.repository.CampSiteRepository;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampSiteServiceTest {

  @Mock
  private CampSiteRepository campSiteRepository;

  @Mock
  private CampRepository campRepository;

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
        .maximumPeople(4)
        .price(50000)
        .indoor_facility("화장실, 취사장")
        .siteType(Induty.NORMAL_SITE)
        .checkInTime(LocalTime.of(15, 0))
        .checkOutTime(LocalTime.of(11, 0))
        .build();
  }

  @Test
  @DisplayName("TEST - 예약 가능한 캠프사이트 조회 성공 확인 테스트")
  void getAvailableCampSites_success() {
    // given
    Long campId = 1L;
    List<Long> reservedSiteIds = Arrays.asList(2L, 3L);
    List<CampSite> allCampSites = Arrays.asList(
        mockCampSite,
        CampSite.builder().id(2L).camp(mockCamp).siteType(Induty.NORMAL_SITE).build(),
        CampSite.builder().id(3L).camp(mockCamp).siteType(Induty.CAR_SITE).build()
    );

    when(campSiteRepository.findByCampId(campId)).thenReturn(allCampSites);
    when(campSiteMapper.toCampSiteListDto(any(CampSite.class))).thenReturn(mockCampSiteListDto);

    // when
    List<CampSiteListResponseDto> result = campSiteService.getAvailableCampSites(campId, reservedSiteIds);

    // then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(mockCampSiteListDto, result.get(0));
    verify(campSiteRepository).findByCampId(campId);
    verify(campSiteMapper).toCampSiteListDto(any(CampSite.class));
  }

  @Test
  @DisplayName("TEST - 모든 캠프사이트가 예약 불가능할 때 빈 리스트 반환 확인 테스트")
  void getAvailableCampSites_allReserved_returnsEmptyList() {
    // given
    Long campId = 1L;
    List<Long> reservedSiteIds = Arrays.asList(1L);
    List<CampSite> allCampSites = Arrays.asList(mockCampSite);

    when(campSiteRepository.findByCampId(campId)).thenReturn(allCampSites);

    // when
    List<CampSiteListResponseDto> result = campSiteService.getAvailableCampSites(campId, reservedSiteIds);

    // then
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(campSiteRepository).findByCampId(campId);
    verify(campSiteMapper, never()).toCampSiteListDto(any(CampSite.class));
  }


  // 캠핑지 관리자 테스트 CRUD
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

  @Test
  @DisplayName("TEST - 캠프사이트 예약 가능 여부 토글")
  public void testToggleAvailability() {
    // Given
    Long campSiteId = 1L;
    CampSite existingCampSite = CampSite.builder()
            .id(campSiteId)
            .maximumPeople(10)
            .price(10000)
            .siteType(Induty.CAR_SITE)
            .indoorFacility("Indoor Facility")
            .isAvailable(false) // 초기 상태 false
            .build();

    CampSite updatedCampSite = existingCampSite.toBuilder()
            .isAvailable(true) // 상태를 true로 변경
            .build();

    when(campSiteRepository.findById(campSiteId)).thenReturn(Optional.of(existingCampSite));
    when(campSiteRepository.save(any(CampSite.class))).thenReturn(updatedCampSite);

    // When
    boolean updatedStatus = campSiteService.toggleAvailability(campSiteId);

    // Then
    assertTrue(updatedStatus);
    verify(campSiteRepository, times(1)).findById(campSiteId);
    verify(campSiteRepository, times(1)).save(any(CampSite.class));
  }

  @Test
  @DisplayName("TEST - 캠프사이트 예약 가능 여부 조회")
  public void testGetAvailability() {
    // Given
    Long campSiteId = 1L;
    CampSite campSite = CampSite.builder()
            .id(campSiteId)
            .maximumPeople(10)
            .price(10000)
            .siteType(Induty.CAR_SITE)
            .indoorFacility("Indoor Facility")
            .isAvailable(true) // 초기 상태 true
            .build();

    when(campSiteRepository.findById(campSiteId)).thenReturn(Optional.of(campSite));

    // When
    boolean isAvailable = campSiteService.getAvailability(campSiteId);

    // Then
    assertTrue(isAvailable);
    verify(campSiteRepository, times(1)).findById(campSiteId);
  }
}