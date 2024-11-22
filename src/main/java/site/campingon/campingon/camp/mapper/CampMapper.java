package site.campingon.campingon.camp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import site.campingon.campingon.camp.dto.*;
import site.campingon.campingon.camp.entity.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Mapper(componentModel = "spring")
public class CampMapper {

  @Mapping(source = "camp.campName", target = "name")
  @Mapping(source = "campAddr", target = "address")
  @Mapping(source = "campKeywords", target = "keywords")
  CampListResponseDto toCampListDto(
      Camp camp,
      CampAddr campAddr,
      List<CampKeyword> campKeywords,
      boolean isLike
  );

  @Mapping(source = "camp.campName", target = "name")
  @Mapping(source = "campAddr", target = "address")
  @Mapping(source = "campImages", target = "images")
  @Mapping(source = "campKeywords", target = "keywords")
  @Mapping(source = "campInfo.recommendCnt", target = "recommendCnt")
  @Mapping(source = "campInfo.likeCnt", target = "likeCnt")
  CampDetailResponseDto toCampDetailDto(
      Camp camp,
      CampAddr campAddr,
      List<CampImage> campImages,
      List<CampKeyword> campKeywords,
      CampInfo campInfo
  );

  CampSiteListResponseDto toCampSiteListDto(CampSite campSite);

  CampAddrDto toCampAddrDto(CampAddr address);


  // 캠핑장 키워드 리스트를 String 리스트로 변환
  default List<String> mapKeywords(List<CampKeyword> keywords) {
    if (keywords == null) return new ArrayList<>();
    return keywords.stream()
        .map(CampKeyword::getKeyword)
        .collect(Collectors.toList());
  }

  // 이미지url 리스트를 String 리스트로 변환
  default List<String> mapImages(List<CampImage> images) {
    if (images == null) return new ArrayList<>();
    return images.stream()
        .map(CampImage::getImageUrl)
        .collect(Collectors.toList());
  }

}
