package site.campingon.campingon.common.public_data.schedule;

import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.io.ParseException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import site.campingon.campingon.common.public_data.GoCampingPath;
import site.campingon.campingon.common.public_data.dto.GoCampingDataDto;
import site.campingon.campingon.common.public_data.dto.GoCampingImageDto;
import site.campingon.campingon.common.public_data.dto.GoCampingImageParsedResponseDto;
import site.campingon.campingon.common.public_data.dto.GoCampingParsedResponseDto;
import site.campingon.campingon.common.public_data.service.GoCampingService;

import java.net.URISyntaxException;
import java.util.List;

import static site.campingon.campingon.common.public_data.PublicDataConstants.SYNC_STATUS_UPDATE;

@RequiredArgsConstructor
@Component
@Slf4j
public class GoCampingScheduler {
    private final GoCampingService goCampingService;

    //신규, 업데이트, 삭제
    private static final Long NUM_OF_ROWS = 10L;
//    private static final Long IMAGE_CNT = 100L;
    private static final Long IMAGE_CNT = 10L;  // 테스트 용


    @Scheduled(initialDelay = 10000)    //스케줄러에 등록되고 10초뒤에 메소드 실행 -> 최초에 한번 실행
//    @Scheduled(cron = "0 0 1 * * ?", zone = "Asia/Seoul")  //매달 1일 오전 00:00에 실행
    public void scheduleCampCreation() {
        try {
            // 처음에 api 를 호출해서 totalCount 값을 가져온다.
            Long pageNo = 1L;      // 현재 페이지 번호

            log.info("스케줄러 실행");
            while (true) {
                // 1. 공공데이터 API 호출 및 데이터 처리
                GoCampingDataDto goCampingDataDto = goCampingService.getAndConvertToGoCampingDataDto(
                        GoCampingPath.BASED_SYNC_LIST,
                        "numOfRows", NUM_OF_ROWS.toString(),
                        "pageNo", pageNo.toString(),
                        "syncStatus", SYNC_STATUS_UPDATE
                );

                List<GoCampingParsedResponseDto> goCampingParsedResponseDtos =
                        goCampingService.updateCampByGoCampingData(goCampingDataDto);

                // 성공 로그
                log.info("캠프 데이터 생성 성공: " + goCampingParsedResponseDtos.size() + "개");


                //그 다음에 몇초 기다리기
//                Thread.sleep(10000);  //일단보류

                //공공데이터를 조회하고 dto로 변환(이미지)
                List<GoCampingImageDto> goCampingImageDto = goCampingService.getAndConvertToGoCampingImageDataDto(IMAGE_CNT);

                //CampImage 를 생성하고 DB에 저장한다.
                List<List<GoCampingImageParsedResponseDto>> goCampingImageParsedResponseDtos
                        = goCampingService.createOrUpdateCampImageByGoCampingImageData(goCampingImageDto);

                log.info("캠프 이미지 데이터 생성 성공: " + goCampingImageParsedResponseDtos.size() + "개");

                //그 다음에 몇초 기다리기
//                Thread.sleep(10000); //일단보류
                pageNo++;   //페이지 번호 증가

                //더이상 가져올 데이터가 없다면 break
                if (goCampingDataDto.getResponse().getBody().getNumOfRows() == 0) {
                    break;
                }
            }

        } catch (URISyntaxException e) {
            // 예외 처리
            log.error("캠프 데이터 생성 실패: " + e.getMessage());
        } /*catch (InterruptedException e) {
            log.error("시간지연 에러발생 : {}", e.getMessage());
        }*/
    }
}
