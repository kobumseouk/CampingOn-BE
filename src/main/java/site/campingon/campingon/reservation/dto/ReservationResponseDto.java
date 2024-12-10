package site.campingon.campingon.reservation.dto;

import lombok.*;
import site.campingon.campingon.reservation.entity.ReservationStatus;

import java.time.LocalDateTime;

@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponseDto {

    private Long id;

    private LocalDateTime checkin;

    private LocalDateTime checkout;

    private int guestCnt;

    private ReservationStatus status;

    private int totalPrice;

    private CampResponseDto campResponseDto;

    private CampAddrResponseDto campAddrResponseDto;

    private CampSiteResponseDto campSiteResponseDto;

    private ReviewResponseDto reviewDto;  // 리뷰 작성시 ReviewResponseDto명이 겹쳐서 변경

}
