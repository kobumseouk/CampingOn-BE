package site.campingon.campingon.reservation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;
import site.campingon.campingon.camp.entity.CampSite;
import site.campingon.campingon.common.entity.BaseEntity;
import site.campingon.campingon.user.entity.User;

import java.time.LocalDateTime;

@Entity
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
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
