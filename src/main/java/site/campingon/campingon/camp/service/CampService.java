package site.campingon.campingon.camp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.campingon.campingon.camp.dto.CampDetailResponseDto;
import site.campingon.campingon.camp.dto.CampListResponseDto;
import site.campingon.campingon.camp.dto.CampSiteListResponseDto;
import site.campingon.campingon.camp.entity.Camp;
import site.campingon.campingon.camp.entity.CampSite;
import site.campingon.campingon.camp.mapper.CampMapper;
import site.campingon.campingon.camp.repository.CampRepository;
import site.campingon.campingon.camp.repository.CampSiteRepository;
import site.campingon.campingon.bookmark.repository.BookmarkRepository;
import site.campingon.campingon.user.repository.UserKeywordRepository;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CampService {

  private final CampRepository campRepository;
  private final CampSiteRepository campSiteRepository;
  private final UserKeywordRepository userKeywordRepository;
  private final BookmarkRepository bookMarkRepository;
  private final CampMapper campMapper;

  // 추천 캠핑장 조회 (페이지네이션 - 횡스크롤 3개)
  public Page<CampListResponseDto> getMatchedCampsByKeywords(Long userId, Pageable pageable) {
    List<String> userKeywords = userKeywordRepository.findKeywordsByUserId(userId);

    // 사용자에 저장된 키워드가 없는 경우
    if (userKeywords.isEmpty()) {
      return Page.empty(pageable);
    }

    return campRepository.findMatchedCampsByKeywords(userKeywords, pageable)
        .map(camp -> {
          CampListResponseDto dto = campMapper.toCampListDto(camp);
          dto.setMarked(bookMarkRepository.existsByCampIdAndUserId(camp.getId(), userId));
          return dto;
        });
  }

  // 인기 캠핑장 조회
  public Page<CampListResponseDto> getPopularCamps(Long userId, Pageable pageable) {
    return campRepository.findPopularCamps(pageable)
        .map(camp -> {
          CampListResponseDto dto = campMapper.toCampListDto(camp);
          dto.setMarked(bookMarkRepository.existsByCampIdAndUserId(camp.getId(), userId));
          return dto;
        });
  }

  // 캠핑장 상세 조회
  public CampDetailResponseDto getCampDetail(Long campId) {
    Camp camp = campRepository.findById(campId)
        .orElseThrow(() -> new RuntimeException("캠핑장을 찾을 수 없습니다."));

    return campMapper.toCampDetailDto(camp);
  }

  // 캠핑장의 캠핑지 목록 조회
  public List<CampSiteListResponseDto> getCampSites(Long campId) {
    List<CampSite> campSites = campSiteRepository.findByCampId(campId);

    return campSites.stream()
        .map(campMapper::toCampSiteListDto)
        .collect(Collectors.toList());
  }

  // 사용자의 찜한 캠핑장 목록 조회
  public Page<CampListResponseDto> getBookmarkedCamps(Long userId, Pageable pageable) {
    Page<Camp> bookmarkedCamps = campRepository.findByBookmarks_User_IdAndBookmarks_IsMarkedTrue(userId, pageable);

    List<CampListResponseDto> campDtos = bookmarkedCamps.getContent().stream()
        .map(camp -> {
          CampListResponseDto dto = campMapper.toCampListDto(camp);
          dto.setMarked(true);
          return dto;
        })
        .collect(Collectors.toList());

    return new PageImpl<>(campDtos, pageable, bookmarkedCamps.getTotalElements());
  }

  // 지역(시/도)과 검색어를 통한 캠핑장 목록 조회
  public Page<CampListResponseDto> searchCamps(Long userId, String keyword, String city, PageRequest pageRequest) {
    // 검색어를 소문자로 변환
    String searchKeyword = keyword != null ? keyword.toLowerCase() : null;

    // 먼저 캠핑장명으로 검색
    List<CampListResponseDto> nameSearchResult = campRepository.findByCampNameSearch(city, pageRequest)
        .getContent().stream()
        .map(camp -> {
          CampListResponseDto dto = campMapper.toCampListDto(camp);
          dto.setMarked(bookMarkRepository.existsByCampIdAndUserId(camp.getId(), userId));
          return dto;
        })
        .filter(dto -> {
          if (searchKeyword == null) return true;
          String campName = dto.getName().toLowerCase();
          return campName.equals(searchKeyword)
              || Arrays.asList(campName.split(" ")).contains(searchKeyword);
        })
        .collect(Collectors.toList());


    // 캠핑장명 검색 결과가 없으면 키워드 검색
    if (nameSearchResult.isEmpty()) {
      // 지역 조건과 키워드 검색(업종, 부대시설, 시/군/구, 캠핑장 키워드)을 추천수로 페이지 정렬
      return campRepository.searchCampsByKeywordAndCity(searchKeyword, city, pageRequest)
          .map(camp -> {
            CampListResponseDto dto = campMapper.toCampListDto(camp);
            dto.setMarked(bookMarkRepository.existsByCampIdAndUserId(camp.getId(), userId));
            return dto;
          });
    }

    return new PageImpl<>(nameSearchResult, pageRequest, nameSearchResult.size());
  }


  // 캠핑장 생성
  @Transactional
  public CampDetailResponseDto createCamp(Camp camp) {
      return campMapper.toCampDetailDto(campRepository.save(camp));
  }

  // 캠핑장 수정
  @Transactional
  public CampDetailResponseDto updateCamp(Long campId, Camp updatedCamp) {
      Camp existingCamp = campRepository.findById(campId)
              .orElseThrow(() -> new RuntimeException("캠핑장을 찾을 수 없습니다."));
      campMapper.updateCampFromDto(updatedCamp, existingCamp);
      return campMapper.toCampDetailDto(campRepository.save(existingCamp));
  }

  // 캠핑장 삭제
  @Transactional
  public void deleteCamp(Long id) {
      campRepository.deleteById(id);
  }

  // 모든 캠핑장 조회
  public List<CampListResponseDto> getAllCamps() {
      List<Camp> camps = campRepository.findAll();
      return camps.stream()
              .map(campMapper::toCampListDto)
              .toList();
  }

}