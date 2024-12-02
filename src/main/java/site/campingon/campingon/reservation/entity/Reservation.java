package site.campingon.campingon.reservation.entity;

import jakarta.persistence.*;
import lombok.*;
import site.campingon.campingon.camp.entity.Camp;
import site.campingon.campingon.camp.entity.CampSite;
import site.campingon.campingon.common.entity.BaseEntity;
import site.campingon.campingon.user.entity.User;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@ToString
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
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

    @Column(nullable = false, columnDefinition = "DATE")
    private LocalDate checkInDate;

    @Column(nullable = false, columnDefinition = "DATE")
    private LocalDate checkOutDate;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "TIME(0)")
    private CheckTime checkInTime = CheckTime.CHECKIN;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "TIME(0)")
    private CheckTime checkOutTime = CheckTime.CHECKOUT;

    @Column(nullable = false)
    private int guestCnt;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'RESERVED'")
    private ReservationStatus status = ReservationStatus.RESERVED;

    private String cancelReason;

    private int totalPrice;

}
