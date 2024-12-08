package site.campingon.campingon.reservation.dto;

import lombok.*;
import site.campingon.campingon.reservation.entity.CheckTime;
import site.campingon.campingon.reservation.entity.ReservationStatus;

import java.time.LocalDate;

@Getter
@ToString
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponseDto {

    private Long id;

    private LocalDate checkinDate;

    private LocalDate checkoutDate;

    private CheckTime checkinTime;

    private CheckTime checkoutTime;

    private int guestCnt;

    private ReservationStatus status;

    private int totalPrice;

    private CampResponseDto campResponseDto;

    private CampAddrResponseDto campAddrResponseDto;

    private CampSiteResponseDto campSiteResponseDto;

    private ReviewResponseDto reviewDto;  // 리뷰 작성시 ReviewResponseDto명이 겹쳐서 변경

}
