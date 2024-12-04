package site.campingon.campingon.camp.service.mongodb;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import site.campingon.campingon.camp.dto.CampListResponseDto;
import site.campingon.campingon.camp.entity.mongodb.SearchInfo;
import site.campingon.campingon.camp.mapper.mongodb.SearchInfoMapper;
import site.campingon.campingon.camp.repository.mongodb.SearchInfoRepository;

import java.util.List;


@Service
@RequiredArgsConstructor
public class SearchInfoService {
  private final SearchInfoRepository searchInfoRepository;
  private final SearchInfoMapper searchInfoMapper;

  public Page<CampListResponseDto> searchExactMatchByLocationAndName(String city, String name, Pageable pageable) {

    // 빈 문자열을 null로 변환
    city = StringUtils.hasText(city) ? city : null;
    name = StringUtils.hasText(name) ? name : null;

    Page<SearchInfo> results;
    if (city == null && name == null) {
      results = searchInfoRepository.findAll(pageable);
    } else if (city == null) {
      results = searchInfoRepository.findByName(name, pageable);
    } else if (name == null) {
      results = searchInfoRepository.findByAddress_City(city, pageable);
    } else {
      results = searchInfoRepository.findByAddress_CityAndName(city, name, pageable);
    }

    return searchInfoMapper.toPageDto(results);
  }
}
