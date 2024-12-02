package site.campingon.campingon.reservation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import site.campingon.campingon.camp.dto.CampSiteResponseDto;
import site.campingon.campingon.common.jwt.CustomUserDetails;
import site.campingon.campingon.reservation.dto.*;
import site.campingon.campingon.reservation.service.ReservationService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationController {

    @Value("${app.pagination.reservation.page}")
    private int page;

    @Value("${app.pagination.reservation.size}")
    private int size;


    private final ReservationService reservationService;

    // 유저의 예약 목록 조회
    @GetMapping
    public ResponseEntity<Page<ReservationResponseDto>> getReservations(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(reservationService.getReservations(userDetails.getId(), pageable));
    }

    // 단일 예약 정보 조회
    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationResponseDto> getReservation(@PathVariable("reservationId") Long reservationId) {

        return ResponseEntity.ok(reservationService.getReservation(reservationId));
    }

    // 새로운 예약 생성
    @PostMapping
    public ResponseEntity<Void> createReservation(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody ReservationCreateRequestDto requestDto) {

        reservationService.createReservation(userDetails.getId(), requestDto);

        return ResponseEntity.ok().build();
    }

    // 예약 취소
    @PatchMapping("/{reservationId}")
    public ResponseEntity<Void> cancelReservation(@PathVariable("reservationId") Long reservationId, @RequestBody ReservationCancelRequestDto requestDto) {
        
        reservationService.cancelReservation(reservationId, requestDto);

        return ResponseEntity.ok().build();
    }

    // 해당 날짜에 이미 예약된 캠프사이트 ID 목록 조회
    @GetMapping("/reserved")
    public ResponseEntity<ReservedCampSiteIdListResponseDto> getReservedCampSiteIds(@RequestBody ReservationCheckDateRequestDto requestDto) {

        return ResponseEntity.ok(reservationService.getReservedCampSiteIds(requestDto));
    }

    // 해당 날짜에 예약 가능한 캠프사이트 조회
    @GetMapping("/available")
    public ResponseEntity<List<CampSiteResponseDto>> getAvailableCampSites(@RequestBody ReservationCheckDateRequestDto requestDto) {

        return ResponseEntity.ok(reservationService.getAvailableCampSites(requestDto));
    }
}
