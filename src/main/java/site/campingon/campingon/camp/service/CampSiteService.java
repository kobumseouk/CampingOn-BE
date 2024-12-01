package site.campingon.campingon.camp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.campingon.campingon.camp.dto.CampSiteResponseDto;
import site.campingon.campingon.camp.dto.CampSiteListResponseDto;
import site.campingon.campingon.camp.dto.admin.CampSiteCreateRequestDto;
import site.campingon.campingon.camp.dto.admin.CampSiteUpdateRequestDto;
import site.campingon.campingon.camp.entity.Camp;
import site.campingon.campingon.camp.entity.CampSite;
import site.campingon.campingon.camp.mapper.CampSiteMapper;
import site.campingon.campingon.camp.repository.CampRepository;
import site.campingon.campingon.camp.repository.CampSiteRepository;
import site.campingon.campingon.common.exception.ErrorCode;
import site.campingon.campingon.common.exception.GlobalException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CampSiteService {

    private final CampRepository campRepository;
    private final CampSiteRepository campSiteRepository;
    private final CampSiteMapper campSiteMapper;

    @Transactional
    public CampSiteResponseDto createCampSite(Long campId, CampSiteCreateRequestDto createRequestDto) {
        Camp camp = campRepository.findById(campId)
            .orElseThrow(() -> new GlobalException(ErrorCode.CAMP_NOT_FOUND_BY_ID));

        CampSite campSite = campSiteMapper.toCampSite(createRequestDto, camp);
        return campSiteMapper.toCampSiteResponseDto(campSiteRepository.save(campSite));
    }

    @Transactional
    public CampSiteResponseDto updateCampSite(Long campId, Long siteId, CampSiteUpdateRequestDto updateRequestDto) {
        // 캠핑지 조회
        // Optional을 반환하여 존재 여부를 처리
        CampSite campSite = campSiteRepository.findByIdAndCampId(siteId, campId)
            .orElseThrow(() -> new GlobalException(ErrorCode.CAMPSITE_NOT_FOUND_BY_ID));

        campSiteMapper.updateCampSiteFromDto(updateRequestDto, campSite);
        return campSiteMapper.toCampSiteResponseDto(campSiteRepository.save(campSite));
    }

    @Transactional
    public void deleteCampSite(Long campId, Long siteId) {
        // Optional을 반환하여 존재 여부를 처리
        CampSite campSite = campSiteRepository.findByIdAndCampId(siteId, campId)
            .orElseThrow(() -> new GlobalException(ErrorCode.CAMPSITE_NOT_FOUND_BY_ID));

        campSiteRepository.delete(campSite);
    }

    // 캠핑지 전체 조회
    public List<CampSiteListResponseDto> getCampSites(Long campId) {
        List<CampSite> campSites = campSiteRepository.findAllByCampId(campId);
        return campSites.stream()
                .map(campSiteMapper::toCampSiteListResponseDto)
                .toList();
    }

    // 캠핑장의 예약 가능한 SiteType별 캠핑지 조회
    public List<CampSiteListResponseDto> getAvailableCampSites(Long campId, List<Long> reservedSiteIds) {
        // 해당 캠핑장의 모든 캠프사이트 조회
        List<CampSite> allCampSites = campSiteRepository.findByCampId(campId);

        // 예약 불가능한 사이트들 제외하고 타입별 그룹화
        return allCampSites.stream()
            .filter(site -> !reservedSiteIds.contains(site.getId()))  // 이미 예약된 사이트 제외
            .collect(Collectors.groupingBy(CampSite::getSiteType)) // 그룹화
            .values().stream()
            .map(sites -> sites.get(0))  // 각 타입별 첫 번째 사이트만 선택
            .map(campSiteMapper::toCampSiteListDto)
            .collect(Collectors.toList());
    }

    // 특정 캠핑지 조회
    public CampSiteResponseDto getCampSite(Long campId, Long siteId) {
        CampSite campSite = campSiteRepository.findByIdAndCampId(siteId, campId)
                .orElseThrow(() -> new GlobalException(ErrorCode.CAMPSITE_NOT_FOUND_BY_ID));
        return campSiteMapper.toCampSiteResponseDto(campSite);
    }

    // isAvailable 상태를 토글하고 변경된 상태 반환
    @Transactional
    public boolean toggleAvailability(Long campSiteId) {
        CampSite campSite = campSiteRepository.findById(campSiteId)
                .orElseThrow(() -> new GlobalException(ErrorCode.CAMPSITE_NOT_FOUND_BY_ID));

        boolean newAvailability = !campSite.isAvailable(); // 현재 상태를 반대로 변경
        CampSite updatedCampSite = campSite.toBuilder()
                .isAvailable(newAvailability) // 변경된 상태로 업데이트
                .build();

        campSiteRepository.save(updatedCampSite); // 저장
        return newAvailability; // 변경된 상태 반환
    }

    // isAvailable 상태 조회
    public boolean getAvailability(Long campSiteId) {
        CampSite campSite = campSiteRepository.findById(campSiteId)
                .orElseThrow(() -> new RuntimeException("캠프 사이트를 찾을 수 없습니다."));
        return campSite.isAvailable();
    }
}