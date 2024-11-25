package site.campingon.campingon.common.public_data.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.campingon.campingon.common.public_data.GoCampingPath;
import site.campingon.campingon.common.public_data.dto.GoCampingDataDto;
import site.campingon.campingon.common.public_data.dto.GoCampingParsedResponseDto;
import site.campingon.campingon.common.public_data.service.GoCampingService;

import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * https://www.data.go.kr/tcs/dss/selectApiDataDetailView.do?publicDataPk=15101933
 * 공공데이터 고캠핑 정보 조회서비스
 */
@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class GoCampingController {

    private final GoCampingService goCampingService;

    //기본 정보 목록 조회
    @GetMapping("/basedList")
    public ResponseEntity<List<GoCampingParsedResponseDto>> GetGoCampingBasedList(
            @RequestParam("numOfRows") Long numOfRows,
            @RequestParam("pageNo") Long pageNo)
            throws URISyntaxException {
        GoCampingDataDto goCampingDataDto = goCampingService.goCampingDataDtoByGoCampingUrl(
                GoCampingPath.BASED_LIST,
                "numOfRows", numOfRows.toString(),
                "pageNo", pageNo.toString());

        List<GoCampingParsedResponseDto> goCampingParsedResponseDtos
                = goCampingService.publicDataFilters(goCampingDataDto);

        return ResponseEntity.status(HttpStatus.OK).body(goCampingParsedResponseDtos);
    }

    //위치기반정보 목록 조회
    @GetMapping("/locationBasedList")
    public ResponseEntity<List<GoCampingParsedResponseDto>> GetGoCampingLocationBasedList(
            @RequestParam("numOfRows") Long numOfRows,
            @RequestParam("pageNo") Long pageNo,
            @RequestParam("mapX") String mapX,
            @RequestParam("mapY") String mapY,
            @RequestParam("radius") String radius)
            throws URISyntaxException {
        GoCampingDataDto goCampingDataDto = goCampingService.goCampingDataDtoByGoCampingUrl(
                GoCampingPath.LOCATION_BASED_LIST,
                "numOfRows", numOfRows.toString(),
                "pageNo", pageNo.toString(),
                "mapX", mapX, "mapY", mapY, "radius", radius);

        List<GoCampingParsedResponseDto> goCampingParsedResponseDtos
                = goCampingService.publicDataFilters(goCampingDataDto);

        return ResponseEntity.status(HttpStatus.OK).body(goCampingParsedResponseDtos);
    }

    //키워드 검색 목록 조회
    @GetMapping("/searchList")
    public ResponseEntity<List<GoCampingParsedResponseDto>> GetGoCampingKeywordList(
            @RequestParam("numOfRows") Long numOfRows,
            @RequestParam("pageNo") Long pageNo,
            @RequestParam("keyword") String keyword)
            throws URISyntaxException {
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8); //한글 인코딩
        GoCampingDataDto goCampingDataDto = goCampingService.goCampingDataDtoByGoCampingUrl(
                GoCampingPath.SEARCH_LIST,
                "numOfRows", numOfRows.toString(),
                "pageNo", pageNo.toString(),
                "keyword", encodedKeyword);

        List<GoCampingParsedResponseDto> goCampingParsedResponseDtos
                = goCampingService.publicDataFilters(goCampingDataDto);

        return ResponseEntity.status(HttpStatus.OK).body(goCampingParsedResponseDtos);
    }

    //이미지정보 목록 조회
    @GetMapping("/imageList")
    public ResponseEntity<List<GoCampingParsedResponseDto>> GetGoCampingImageList(
            @RequestParam("numOfRows") Long numOfRows,
            @RequestParam("pageNo") Long pageNo,
            @RequestParam("contentId") Long contentId)
            throws URISyntaxException {
        GoCampingDataDto goCampingDataDto = goCampingService.goCampingDataDtoByGoCampingUrl(
                GoCampingPath.IMAGE_LIST,
                "numOfRows", numOfRows.toString(),
                "pageNo", pageNo.toString(),
                "contentId", contentId.toString());

        List<GoCampingParsedResponseDto> goCampingParsedResponseDtos
                = goCampingService.publicDataFilters(goCampingDataDto);

        return ResponseEntity.status(HttpStatus.OK).body(goCampingParsedResponseDtos);
    }

    //동기화 목록 조회
    @GetMapping("/basedSyncList")
    public ResponseEntity<List<GoCampingParsedResponseDto>> GetGoCampingBasedSyncList(
            @RequestParam("numOfRows") Long numOfRows,
            @RequestParam("pageNo") Long pageNo,
            @RequestParam("syncStatus") String syncStatus)
            throws URISyntaxException {
        GoCampingDataDto goCampingDataDto = goCampingService.goCampingDataDtoByGoCampingUrl(
                GoCampingPath.BASED_SYNC_LIST,
                "numOfRows", numOfRows.toString(),
                "pageNo", pageNo.toString(),
                "syncStatus", syncStatus);

        List<GoCampingParsedResponseDto> goCampingParsedResponseDtos = goCampingService.publicDataFilters(goCampingDataDto);

        return ResponseEntity.status(HttpStatus.OK).body(goCampingParsedResponseDtos);
    }
}
