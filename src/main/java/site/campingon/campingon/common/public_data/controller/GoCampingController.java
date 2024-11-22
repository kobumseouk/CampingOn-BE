package site.campingon.campingon.common.public_data.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import site.campingon.campingon.common.public_data.dto.GoCampingRequestDto;
import site.campingon.campingon.common.public_data.dto.GoCampingResponseDto;
import site.campingon.campingon.common.public_data.service.GoCampingService;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static site.campingon.campingon.common.public_data.PublicDataConstants.*;

/**
 * https://www.data.go.kr/tcs/dss/selectApiDataDetailView.do?publicDataPk=15101933
 * 공공데이터 고캠핑 정보 조회서비스
 */
@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class GoCampingController {

    @Value("${public-data.go-camping}")
    private String serviceKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private final GoCampingService goCampingService;

    //todo 엔티티 주입시 numOfRows 상수화, 키워드검색인 경우 보류
    //기본 정보 목록 조회
    @GetMapping("/basedList")
    public ResponseEntity<?> GetGoCampingBasedList(@RequestParam("numOfRows") Long numOfRows,
                                                   @RequestParam("pageNo") Long pageNo)
            throws URISyntaxException, JsonProcessingException {
        String url = buildUrl("/basedList",
                "numOfRows",numOfRows.toString(),
                "pageNo",pageNo.toString());
        GoCampingResponseDto response = fetchData(url);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //위치기반정보 목록 조회
    @GetMapping("/locationBasedList")
    public ResponseEntity<?> GetGoCampingLocationBasedList(@RequestParam("numOfRows") Long numOfRows,
                                                           @RequestParam("pageNo") Long pageNo,
                                                           @RequestParam("mapX") String mapX,
                                                           @RequestParam("mapY") String mapY,
                                                           @RequestParam("radius") String radius) throws URISyntaxException {
        String url = buildUrl("/locationBasedList",
                "numOfRows", numOfRows.toString()
                , "pageNo", pageNo.toString(),
                "mapX", mapX,
                "mapY", mapY,
                "radius", radius);
        GoCampingResponseDto response = fetchData(url);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //키워드 검색 목록 조회
    @GetMapping("/searchList")
    public ResponseEntity<?> GetGoCampingKeywordList(@RequestParam("numOfRows") Long numOfRows,
                                                     @RequestParam("pageNo") Long pageNo,
                                                     @RequestParam("keyword") String keyword)
            throws URISyntaxException {
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        String url = buildUrl("/searchList",
                "numOfRows",numOfRows.toString(),
                "pageNo",pageNo.toString(),
                "keyword", encodedKeyword);
        GoCampingResponseDto response = fetchData(url);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //이미지정보 목록 조회
    @GetMapping("/imageList")
    public ResponseEntity<?> GetGoCampingImageList(@RequestParam("numOfRows") Long numOfRows,
                                                   @RequestParam("pageNo") Long pageNo,
                                                   @RequestParam("contentId") Long contentId)
            throws URISyntaxException {
        String url = buildUrl("/imageList",
                "numOfRows",numOfRows.toString(),
                "pageNo",pageNo.toString(),
                "contentId", contentId.toString());
        GoCampingResponseDto response = fetchData(url);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //동기화 목록 조회
    @GetMapping("/basedSyncList")
    public ResponseEntity<?> GetGoCampingBasedSyncList(@RequestParam("numOfRows") Long numOfRows,
                                                       @RequestParam("pageNo") Long pageNo,
                                                       @RequestParam("syncStatus") String syncStatus)
            throws URISyntaxException {
        String url = buildUrl("/basedSyncList",
                "numOfRows",numOfRows.toString(),
                "pageNo",pageNo.toString(),
                "syncStatus", syncStatus);
        GoCampingResponseDto response = fetchData(url);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // 공통 URL 생성 메소드
    private String buildUrl(String endpoint, String... params) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(GO_CAMPING_END_POINT + endpoint)
                .queryParam("_type", CONTENT_TYPE)
                .queryParam("MobileOS", MOBILE_OS)
                .queryParam("MobileApp", MOBILE_APP)
                .queryParam("serviceKey", serviceKey);

        for (int i = 0; i < params.length; i += 2) {
            uriBuilder.queryParam(params[i], params[i + 1]);
        }

        return uriBuilder.build().toUriString();
    }

    // 공공데이터 요청 및 응답 처리
    private GoCampingResponseDto fetchData(String url) throws URISyntaxException {
        URI uri = new URI(url);
        GoCampingRequestDto request = restTemplate.getForObject(uri, GoCampingRequestDto.class);
        return goCampingService.publicDataFilter(request);
    }
}
