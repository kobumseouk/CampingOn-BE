//package site.campingon.campingon.camp.repository;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.*;
//
//import java.util.List;
//import java.util.Optional;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.transaction.annotation.Transactional;
//import site.campingon.campingon.camp.entity.Camp;
//import site.campingon.campingon.camp.entity.CampSite;
//
//@SpringBootTest
//@Transactional
//@TestPropertySource(properties = "GO_CAMPING_SERVICE_KEY=test-key")
//class CampSiteRepositoryTest {
//
//    @Autowired
//    private CampSiteRepository campSiteRepository;
//
//    @Autowired
//    private CampRepository campRepository;
//
//    @DisplayName("TEST - 특정 캠프장의 캠핑지를 모두 조회")
//    @Test
//    void testFindByCampId() {
//        // Given
//        Camp camp = Camp.builder()
//                .campName("Test Camp")
//                .tel("010-1111-2222")
//                .intro("ㅎㅇ")
//                .lineIntro("ㅎㅇㅎㅇ")
//                .homepage("www.alice.com")
//                .outdoorFacility("편의점")
//                .thumbImage("image.jpg")
////                .induty("카라반", "글램핑")
//                .build();
//        camp = campRepository.save(camp);
//
//        CampSite campSite1 = CampSite.builder()
//                .camp(camp)
//                .maximumPeople(10)
//                .price(100)
////                .type("Tent")
//                .indoorFacility("None")
//                .isAvailable(true)
//                .build();
//
//        CampSite campSite2 = CampSite.builder()
//                .camp(camp)
//                .maximumPeople(5)
//                .price(50)
////                .type("RV")
//                .indoorFacility("Heater")
//                .isAvailable(true)
//                .build();
//
//        campSiteRepository.saveAll(List.of(campSite1, campSite2));
//
//        // When
//        List<CampSite> campSites = campSiteRepository.findByCampId(camp.getId());
//
//        // Then
//        assertNotNull(campSites);
//        assertEquals(2, campSites.size());
//    }
//
//    @DisplayName("TEST - 캠핑지 ID와 캠프장 ID로 캠핑지 조회")
//    @Test
//    void testFindByIdAndCampId() {
//        // Given: 캠프와 캠핑지 데이터를 생성하고 저장합니다.
//        Camp camp = Camp.builder()
//                .campName("Test Camp")
//                .tel("010-1111-2222")
//                .intro("Test Intro")
//                .lineIntro("Test Line Intro")
//                .homepage("https://test-camp.com")
////                .induty("야영장")
//                .outdoorFacility("편의시설")
//                .thumbImage("test-image.jpg")
//                .build();
//        camp = campRepository.save(camp);
//
//        CampSite campSite = CampSite.builder()
//                .camp(camp)
//                .maximumPeople(6)
//                .price(50000)
////                .type("Tent")
//                .indoorFacility("Heater")
//                .isAvailable(true)
//                .build();
//        campSite = campSiteRepository.save(campSite);
//
//        // When: 캠핑지 ID와 캠프장 ID로 데이터를 조회합니다.
//        Optional<CampSite> foundCampSite = campSiteRepository.findByIdAndCampId(campSite.getId(), camp.getId());
//
//        // Then: 결과 검증
//        assertTrue(foundCampSite.isPresent(), "캠핑지를 찾을 수 있어야 합니다.");
//        assertEquals(campSite.getId(), foundCampSite.get().getId(), "캠핑지 ID가 일치해야 합니다.");
//        assertEquals(camp.getId(), foundCampSite.get().getCamp().getId(), "캠프장 ID가 일치해야 합니다.");
//    }
//
//    @DisplayName("TEST - 특정 캠프장에 속한 모든 캠핑지를 JPQL로 조회")
//    @Test
//    void testFindAllByCampId() {
//        // Given
//        Camp camp = Camp.builder()
//                .campName("Test Camp")
//                .tel("010-1111-2222")
//                .intro("ㅎㅇ")
//                .lineIntro("ㅎㅇㅎㅇ")
//                .homepage("www.alice.com")
////                .induty("야외")
//                .outdoorFacility("편의점")
//                .thumbImage("image.jpg")
//                .build();
//        camp = campRepository.save(camp);
//
//        CampSite campSite1 = CampSite.builder()
//                .camp(camp)
//                .maximumPeople(10)
//                .price(100)
////                .type("Tent")
//                .indoorFacility("None")
//                .isAvailable(true)
//                .build();
//
//        CampSite campSite2 = CampSite.builder()
//                .camp(camp)
//                .maximumPeople(5)
//                .price(50)
////                .type("RV")
//                .indoorFacility("Heater")
//                .isAvailable(true)
//                .build();
//
//        campSiteRepository.saveAll(List.of(campSite1, campSite2));
//
//        // When
//        List<CampSite> campSites = campSiteRepository.findByCampId(camp.getId());
//
//        // Then
//        assertNotNull(campSites);
//        assertEquals(2, campSites.size());
//    }
//
//    @DisplayName("TEST - 특정 캠프장 내 캠핑지 삭제")
//    @Test
//    void testDeleteByIdAndCampId() {
//        // Given
//        Long siteId = 1L;
//        Long campId = 1L;
//
//        // When
//        campSiteRepository.deleteByIdAndCampId(siteId, campId);
//        Optional<CampSite> deletedCampSite = campSiteRepository.findByIdAndCampId(siteId, campId);
//
//        // Then
//        assertThat(deletedCampSite).isNotPresent(); // 삭제된 캠핑지가 더 이상 조회되지 않는지 확인
//    }
//}