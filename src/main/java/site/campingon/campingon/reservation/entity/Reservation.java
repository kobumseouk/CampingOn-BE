package site.campingon.campingon.reservation.entity;

import jakarta.persistence.*;
import lombok.*;
import site.campingon.campingon.camp.entity.Camp;
import site.campingon.campingon.camp.entity.CampSite;
import site.campingon.campingon.common.entity.BaseEntity;
import site.campingon.campingon.review.entity.Review;
import site.campingon.campingon.user.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Entity
@Getter
@ToString
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reservation", indexes = {
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_checkin", columnList = "checkin"),
        @Index(name = "idx_checkout", columnList = "checkout")
})
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, columnDefinition = "INT UNSIGNED")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Camp camp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private CampSite campSite;

    @OneToOne(mappedBy = "reservation", fetch = FetchType.EAGER)
    private Review review;

    @Column(nullable = false, columnDefinition = "DATETIME(0)")
    private LocalDateTime checkin;

    @Column(nullable = false, columnDefinition = "DATETIME(0)")
    private LocalDateTime checkout;

    @Column(nullable = false)
    private int guestCnt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'RESERVED'")
    private ReservationStatus status;

    private String cancelReason;

    private int totalPrice;

    // 예약 취소
    public void cancel(String reason) {
        this.status = ReservationStatus.CANCELED;
        this.cancelReason = reason;
    }

    // 체크인 체크아웃 시간 고정 설정
    public void setDefaultCheckTime(LocalDateTime checkin, LocalDateTime checkout) {
        this.checkin = checkin.withHour(15).truncatedTo(ChronoUnit.HOURS);
        this.checkout = checkout.withHour(11).truncatedTo(ChronoUnit.HOURS);
    }


    // json 반환 시 파싱해서 반환
    public String[] parseDateTime(LocalDateTime checkin, LocalDateTime checkout) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return new String[] {
                checkin.format(formatter),
                checkout.format(formatter)
        };
    }

}
