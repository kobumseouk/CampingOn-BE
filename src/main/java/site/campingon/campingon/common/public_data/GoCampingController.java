package site.campingon.campingon.common.public_data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static site.campingon.campingon.common.public_data.PublicDataConstants.GO_CAMPING_END_POINT;

/**
 * https://www.data.go.kr/tcs/dss/selectApiDataDetailView.do?publicDataPk=15101933
 * 공공데이터 고캠핑 정보 조회서비스
 */

@RequestMapping("/api")
@RestController
public class GoCampingController {

    @Value("${public-data.go-camping}")
    private String serviceKey;

    //기본 정보 목록 조회
    @GetMapping("/basedList")
    public ResponseEntity<?> GetGoCampingBasedList() throws URISyntaxException {

        RestTemplate restTemplate = new RestTemplate();
        String info = "/basedList";
        String type = "json";
        String numOfRows = "10";
        String pageNo = "1";
        String mobileOS = "ETC";
        String mobileApp = "AppTest";

        String url = GO_CAMPING_END_POINT
                + info
                + "?_type=" + type
                + "&numOfRows=" + numOfRows
                + "&pageNo=" + pageNo
                + "&MobileOS=" + mobileOS
                + "&MobileApp=" + mobileApp
                + "&serviceKey=" + serviceKey;

        URI uri = new URI(url);
        String forObject = restTemplate.getForObject(uri, String.class);

        return ResponseEntity.status(HttpStatus.OK).body(forObject);
    }

    //위치기반정보 목록 조회
    @GetMapping("/locationBasedList")
    public ResponseEntity<?> GetGoCampingLocationBasedList() throws URISyntaxException {
        RestTemplate restTemplate = new RestTemplate();
        String info = "/locationBasedList";
        String type = "json";
        String numOfRows = "10";
        String pageNo = "1";
        String mobileOS = "ETC";
        String mobileApp = "AppTest";
        String mapX = "128.6142847";
        String mapY = "36.0345423";
        String radius = "2000";

        String url = GO_CAMPING_END_POINT
                + info
                + "?_type=" + type
                + "&numOfRows=" + numOfRows
                + "&pageNo=" + pageNo
                + "&MobileOS=" + mobileOS
                + "&MobileApp=" + mobileApp
                + "&mapX=" + mapX
                + "&mapY=" + mapY
                + "&radius=" + radius
                + "&serviceKey=" + serviceKey;

        URI uri = new URI(url);
        String forObject = restTemplate.getForObject(uri, String.class);

        return ResponseEntity.status(HttpStatus.OK).body(forObject);
    }

    //키워드 검색 목록 조회
    @GetMapping("/searchList")
    public ResponseEntity<?> GetGoCampingKeywordList(@RequestParam("keyword") String keyword) throws URISyntaxException {
        RestTemplate restTemplate = new RestTemplate();
        String info = "/searchList";
        String type = "json";
        String numOfRows = "10";
        String pageNo = "1";
        String mobileOS = "ETC";
        String mobileApp = "AppTest";

        // 입력된 keyword를 URL 인코딩된 값으로 변환
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);

        String url = GO_CAMPING_END_POINT
                + info
                + "?_type=" + type
                + "&numOfRows=" + numOfRows
                + "&pageNo=" + pageNo
                + "&MobileOS=" + mobileOS
                + "&MobileApp=" + mobileApp
                + "&keyword=" + encodedKeyword
                + "&serviceKey=" + serviceKey;

        URI uri = new URI(url);
        String forObject = restTemplate.getForObject(uri, String.class);

        return ResponseEntity.status(HttpStatus.OK).body(forObject);
    }

    //이미지정보 목록 조회
    @GetMapping("/imageList")
    public ResponseEntity<?> GetGoCampingImageList(@RequestParam("contentId") Long contentId) throws URISyntaxException {
        RestTemplate restTemplate = new RestTemplate();
        String info = "/imageList";
        String type = "json";
        String numOfRows = "10";
        String pageNo = "1";
        String mobileOS = "ETC";
        String mobileApp = "AppTest";

        String url = GO_CAMPING_END_POINT
                + info
                + "?_type=" + type
                + "&numOfRows=" + numOfRows
                + "&pageNo=" + pageNo
                + "&MobileOS=" + mobileOS
                + "&MobileApp=" + mobileApp
                + "&contentId=" + contentId
                + "&serviceKey=" + serviceKey;

        URI uri = new URI(url);
        String forObject = restTemplate.getForObject(uri, String.class);

        return ResponseEntity.status(HttpStatus.OK).body(forObject);
    }

    //동기화 목록 조회
    @GetMapping("/basedSyncList")
    public ResponseEntity<?> GetGoCampingBasedSyncList(@RequestParam("syncStatus") String syncStatus) throws URISyntaxException {
        RestTemplate restTemplate = new RestTemplate();
        String info = "/basedSyncList";
        String type = "json";
        String numOfRows = "10";
        String pageNo = "1";
        String mobileOS = "ETC";
        String mobileApp = "AppTest";

        String url = GO_CAMPING_END_POINT
                + info
                + "?_type=" + type
                + "&numOfRows=" + numOfRows
                + "&pageNo=" + pageNo
                + "&MobileOS=" + mobileOS
                + "&MobileApp=" + mobileApp
                + "&syncStatus=" + syncStatus
                + "&serviceKey=" + serviceKey;

        URI uri = new URI(url);
        String forObject = restTemplate.getForObject(uri, String.class);

        return ResponseEntity.status(HttpStatus.OK).body(forObject);
    }


}
