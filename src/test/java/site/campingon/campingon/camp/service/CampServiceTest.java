package site.campingon.campingon.camp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Point;
import site.campingon.campingon.bookmark.repository.BookmarkRepository;
import site.campingon.campingon.camp.dto.CampDetailResponseDto;
import site.campingon.campingon.camp.dto.CampListResponseDto;
import site.campingon.campingon.camp.dto.CampSiteListResponseDto;
import site.campingon.campingon.camp.entity.*;
import site.campingon.campingon.camp.mapper.CampMapper;
import site.campingon.campingon.camp.repository.CampRepository;
import site.campingon.campingon.camp.repository.CampSiteRepository;
import site.campingon.campingon.user.repository.UserKeywordRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampServiceTest {

  @Mock
  private CampRepository campRepository;

  @Mock
  private CampSiteRepository campSiteRepository;

  @Mock
  private UserKeywordRepository userKeywordRepository;

  @Mock
  private BookmarkRepository bookMarkRepository;

  @Mock
  private CampMapper campMapper;


  @InjectMocks
  private CampService campService;


  private Camp mockCamp;
  private CampListResponseDto mockCampListDto;
  private CampDetailResponseDto mockCampDetailDto;
  private CampSite mockCampSite;
  private CampSiteListResponseDto mockCampSiteDto;
  private List<String> mockKeywords;
  private CampAddr mockCampAddr;
  private CampInfo mockCampInfo;
  private List<CampInduty> mockCampInduties;


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

    mockCampListDto = CampListResponseDto.builder()
        .id(1L)
        .name("Test Camp")
        .lineIntro("Test Line Intro")
        .thumbImage("test.jpg")
        .address("Test Address")
        .keywords(Arrays.asList("keyword1", "keyword2"))
        .isMarked(false)
        .build();

    mockCampDetailDto = CampDetailResponseDto.builder()
        .id(1L)
        .name("Test Camp")
        .tel("010-1234-5678")
        .lineIntro("Test Line Intro")
        .homepage("http://test.com")
        .outdoorFacility("Test Facility")
        .indutys("일반야영장, 자동차야영장")
        .campAddr(mockCampAddr)
        .images(Arrays.asList("image1.jpg", "image2.jpg"))
        .campInfo(mockCampInfo)
        .build();

    mockKeywords = Arrays.asList("keyword1", "keyword2");

  }

  @Test
  @DisplayName("키워드 매칭된 캠핑장 목록 조회 확인 테스트")
  void getMatchedCampsByKeywords_success() {
    // given
    Long userId = 1L;
    Pageable pageable = PageRequest.of(0, 3);
    List<Camp> camps = Arrays.asList(mockCamp);
    Page<Camp> campPage = new PageImpl<>(camps, pageable, camps.size());

    when(userKeywordRepository.findKeywordsByUserId(userId)).thenReturn(mockKeywords);
    when(campRepository.findMatchedCampsByKeywords(mockKeywords, pageable)).thenReturn(campPage);
    when(campMapper.toCampListDto(any(Camp.class))).thenReturn(mockCampListDto);
    when(bookMarkRepository.existsByCampIdAndUserId(anyLong(), anyLong())).thenReturn(false);

    // when
    Page<CampListResponseDto> result = campService.getMatchedCampsByKeywords(userId, pageable);

    // then
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals(mockCampListDto, result.getContent().get(0));

    verify(userKeywordRepository).findKeywordsByUserId(userId);
    verify(campRepository).findMatchedCampsByKeywords(mockKeywords, pageable);
    verify(campMapper).toCampListDto(any(Camp.class));
    verify(bookMarkRepository).existsByCampIdAndUserId(anyLong(), anyLong());
  }

  @Test
  void getPopularCamps_success() {
    // given
    Long userId = 1L;
    Pageable pageable = PageRequest.of(0, 12);
    List<Camp> camps = Arrays.asList(mockCamp);
    Page<Camp> campPage = new PageImpl<>(camps, pageable, camps.size());

    when(campRepository.findPopularCamps(pageable)).thenReturn(campPage);
    when(campMapper.toCampListDto(any(Camp.class))).thenReturn(mockCampListDto);
    when(bookMarkRepository.existsByCampIdAndUserId(anyLong(), anyLong())).thenReturn(false);

    // when
    Page<CampListResponseDto> result = campService.getPopularCamps(userId, pageable);

    // then
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals(mockCampListDto, result.getContent().get(0));

    verify(campRepository).findPopularCamps(pageable);
    verify(campMapper).toCampListDto(any(Camp.class));
    verify(bookMarkRepository).existsByCampIdAndUserId(anyLong(), anyLong());
  }

  @Test
  @DisplayName("캠핑장 상세 조회 확인 테스트")
  void getCampDetail_success() {
    // given
    Long campId = 1L;

    when(campRepository.findById(campId)).thenReturn(Optional.of(mockCamp));
    when(campMapper.toCampDetailDto(mockCamp)).thenReturn(mockCampDetailDto);

    // when
    CampDetailResponseDto result = campService.getCampDetail(campId);

    // then
    assertNotNull(result);
    assertEquals(mockCampDetailDto.getId(), result.getId());
    assertEquals(mockCampDetailDto.getName(), result.getName());
    assertEquals("일반야영장, 자동차야영장", result.getIndutys());

    verify(campRepository).findById(campId);
    verify(campMapper).toCampDetailDto(mockCamp);
  }

  @Test
  @DisplayName("찜한 캠핑장 목록 조회 확인 테스트")
  void getBookmarkedCamps_success() {
    // given
    Long userId = 1L;
    Pageable pageable = PageRequest.of(0, 3);
    List<Camp> camps = Arrays.asList(mockCamp);
    Page<Camp> campPage = new PageImpl<>(camps, pageable, camps.size());

    when(campRepository.findByBookmarks_User_IdAndBookmarks_IsMarkedTrue(userId, pageable))
        .thenReturn(campPage);
    when(campMapper.toCampListDto(any(Camp.class))).thenReturn(mockCampListDto);

    // when
    Page<CampListResponseDto> result = campService.getBookmarkedCamps(userId, pageable);

    // then
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertTrue(result.getContent().get(0).isMarked());

    verify(campRepository).findByBookmarks_User_IdAndBookmarks_IsMarkedTrue(userId, pageable);
    verify(campMapper).toCampListDto(any(Camp.class));
  }

  @Test
  @DisplayName("지역(시/도)과 검색어를 통한 캠핑장 목록 조회 확인 테스트")
  void searchCamps_success() {
    // given
    Long userId = 1L;
    String keyword = "test";
    String city = "TestCity";
    PageRequest pageRequest = PageRequest.of(0, 12);

    List<Camp> camps = Arrays.asList(mockCamp);
    Page<Camp> campPage = new PageImpl<>(camps, pageRequest, camps.size());

    when(campRepository.findByCampNameSearch(city, pageRequest)).thenReturn(campPage);
    when(campMapper.toCampListDto(any(Camp.class))).thenReturn(mockCampListDto);
    when(bookMarkRepository.existsByCampIdAndUserId(anyLong(), anyLong())).thenReturn(false);

    // when
    Page<CampListResponseDto> result = campService.searchCamps(userId, keyword, city, pageRequest);

    // then
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals(mockCampListDto, result.getContent().get(0));

    verify(campRepository).findByCampNameSearch(city, pageRequest);
    verify(campMapper).toCampListDto(any(Camp.class));
    verify(bookMarkRepository).existsByCampIdAndUserId(anyLong(), anyLong());
  }


  // 캠핑장 관리자 테스트 CRUD
  @Test
  void createCamp_success() {
    // given
    when(campRepository.save(any(Camp.class))).thenReturn(mockCamp);
    when(campMapper.toCampDetailDto(mockCamp)).thenReturn(mockCampDetailDto);

    // when
    CampDetailResponseDto result = campService.createCamp(mockCamp);

    // then
    assertNotNull(result);
    assertEquals(mockCampDetailDto.getId(), result.getId());
    assertEquals(mockCampDetailDto.getName(), result.getName());

    verify(campRepository).save(any(Camp.class));
    verify(campMapper).toCampDetailDto(mockCamp);
  }

  @Test
  void updateCamp_success() {
    // given
    Long campId = 1L;
    Camp updatedCamp = Camp.builder()
        .id(campId)
        .campName("Updated Camp")
        .tel("010-9876-5432")
        .lineIntro("Updated Intro")
        .homepage("http://updated.com")
        .outdoorFacility("Updated Facility")
        .induty(mockCampInduties)
        .campAddr(mockCampAddr)
        .campInfo(mockCampInfo)
        .build();

    // 업데이트된 DetailResponseDto 설정
    CampDetailResponseDto updatedDto = CampDetailResponseDto.builder()
        .id(1L)
        .name("Updated Camp")
        .tel("010-9876-5432")
        .lineIntro("Updated Intro")
        .homepage("http://updated.com")
        .outdoorFacility("Updated Facility")
        .indutys("일반야영장, 자동차야영장")
        .campAddr(mockCampAddr)
        .images(Arrays.asList("updated1.jpg", "updated2.jpg"))
        .campInfo(mockCampInfo)
        .build();

    when(campRepository.findById(campId)).thenReturn(Optional.of(mockCamp));
    when(campRepository.save(any(Camp.class))).thenReturn(updatedCamp);
    when(campMapper.toCampDetailDto(updatedCamp)).thenReturn(updatedDto);

    // when
    CampDetailResponseDto result = campService.updateCamp(campId, updatedCamp);

    // then
    assertNotNull(result);
    assertEquals(updatedDto.getId(), result.getId());
    assertEquals(updatedDto.getName(), result.getName());
    assertEquals(updatedDto.getTel(), result.getTel());

    verify(campRepository).findById(campId);
    verify(campRepository).save(any(Camp.class));
    verify(campMapper).toCampDetailDto(any(Camp.class));
  }

  @Test
  void deleteCamp_success() {
    // given
    Long campId = 1L;

    // when
    campService.deleteCamp(campId);

    // then
    verify(campRepository).deleteById(campId);
  }

  @Test
  void getAllCamps_success() {
    // given
    List<Camp> camps = Arrays.asList(mockCamp);
    when(campRepository.findAll()).thenReturn(camps);
    when(campMapper.toCampListDto(any(Camp.class))).thenReturn(mockCampListDto);

    // when
    List<CampListResponseDto> result = campService.getAllCamps();

    // then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(mockCampListDto, result.get(0));

    verify(campRepository).findAll();
    verify(campMapper).toCampListDto(any(Camp.class));
  }
}