package site.campingon.campingon.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkin;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkout;

    private int guestCnt;

    private ReservationStatus status;

    private int totalPrice;

    private CampResponseDto campResponseDto;

    private CampAddrResponseDto campAddrResponseDto;

    private CampSiteResponseDto campSiteResponseDto;

    private ReviewResponseDto reviewDto;  // 리뷰 작성시 ReviewResponseDto명이 겹쳐서 변경

}
