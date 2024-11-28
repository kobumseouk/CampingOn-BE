package site.campingon.campingon.common.public_data.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import site.campingon.campingon.camp.entity.Camp;
import site.campingon.campingon.camp.repository.*;
import site.campingon.campingon.common.exception.GlobalException;
import site.campingon.campingon.common.public_data.GoCampingPath;
import site.campingon.campingon.common.public_data.dto.GoCampingDataDto;
import site.campingon.campingon.common.public_data.dto.GoCampingImageDto;
import site.campingon.campingon.common.public_data.mapper.GoCampingMapper;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class) //InjectMocks, Mock 어노테이션 감지
class GoCampingServiceTest {

    @InjectMocks
    private GoCampingService goCampingService;

    @Mock
    private GoCampingMapper goCampingMapper;

    @Mock
    private CampAddrRepository campAddrRepository;

    @Mock
    private CampImageRepository campImageRepository;

    @Mock
    private CampRepository campRepository;

    @Mock
    private CampSiteRepository campSiteRepository;

    @Mock
    private CampIndutyRepository campIndutyRepository;

    @Spy
    private RestTemplate restTemplate;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("공공데이터 전체 API 조회 테스트 : 성공")
    void testGetAndConvertToGoCampingDataDto_Success() throws Exception {
        //given
        String numOfRows = "1";
        String pageNo = "1";

        // Item 객체 생성
        GoCampingDataDto.Item item = GoCampingDataDto.Item.builder()
                .contentId(123L).facltNm("Sample Camping Site")
                .lineIntro("Beautiful camping site with scenic views")
                .intro("Enjoy a perfect getaway with all amenities.")
                .doNm("Seoul").sigunguNm("Gangnam-gu").zipcode("12345")
                .addr1("123 Camping Rd").addr2("Apt 456").tel("123-456-7890")
                .homepage("http://samplecamping.com").gnrlSiteCo(10).autoSiteCo(5)
                .glampSiteCo(3).caravSiteCo(2).indvdlCaravSiteCo(1)
                .glampInnerFclty("Glamping facilities available")
                .caravInnerFclty("Caravan facilities available")
                .sbrsCl("Shower, BBQ Area").animalCmgCl("가능")
                .firstImageUrl("http://imageurl.com/sample.jpg")
                .mapX(37.5665).mapY(126.9780)
                .build();

        // GoCampingDataDto 객체 생성
        GoCampingDataDto goCampingDataDto = GoCampingDataDto.builder()
                .response(GoCampingDataDto.Response.builder()
                        .body(
                                GoCampingDataDto.Body.builder()
                                        .items(
                                                GoCampingDataDto.Items.builder()
                                                        .item(List.of(item)) // Item 리스트 추가
                                                        .build()
                                        )
                                        .build()
                        )
                        .build()
                )
                .build();
        System.out.println("goCampingDataDto :" +goCampingDataDto);

        doReturn(goCampingDataDto).when(restTemplate).getForObject(any(URI.class), eq(GoCampingDataDto.class));

        //when
        GoCampingDataDto result = goCampingService.getAndConvertToGoCampingDataDto("numOfRows", numOfRows, "pageNo", pageNo);

        // then
        assertThat(result).isNotNull();

        GoCampingDataDto.Item resultItem = result.getResponse().getBody().getItems().getItem().getFirst();
        assertThat(resultItem.getContentId()).isEqualTo(123L);
        assertThat(resultItem.getFacltNm()).isEqualTo("Sample Camping Site");
        assertThat(resultItem.getLineIntro()).isEqualTo("Beautiful camping site with scenic views");
        assertThat(resultItem.getDoNm()).isEqualTo("Seoul");
        assertThat(resultItem.getSigunguNm()).isEqualTo("Gangnam-gu");
        assertThat(resultItem.getZipcode()).isEqualTo("12345");
        assertThat(resultItem.getFirstImageUrl()).isEqualTo("http://imageurl.com/sample.jpg");
    }

}
