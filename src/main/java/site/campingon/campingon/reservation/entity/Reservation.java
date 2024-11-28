package site.campingon.campingon.reservation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import site.campingon.campingon.camp.entity.CampSite;
import site.campingon.campingon.common.entity.BaseEntity;
import site.campingon.campingon.user.entity.User;

import java.time.LocalDateTime;

@Entity
@ToString
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, columnDefinition = "INT UNSIGNED")
    private Long Id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(nullable = false)
    private CampSite campSite;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @Column(nullable = false, columnDefinition = "DATETIME(0)")
    private LocalDateTime checkIn;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @Column(nullable = false, columnDefinition = "DATETIME(0)")
    private LocalDateTime checkOut;

    @Column(nullable = false)
    private int guestCnt;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "DEFAULT 'RESERVED'")
    private ReservationStatus status = ReservationStatus.RESERVED;

    private int totalPrice;

}
