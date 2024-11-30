package site.campingon.campingon.review.entity;

import jakarta.persistence.*;
import lombok.*;
import site.campingon.campingon.camp.entity.Camp;
import site.campingon.campingon.camp.entity.CampSite;
import site.campingon.campingon.reservation.entity.Reservation;
import site.campingon.campingon.user.entity.User;

import java.util.ArrayList;
import java.util.List;

@ToString
@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "review")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "INT UNSIGNED")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camp_id", nullable = false, columnDefinition = "INT UNSIGNED")
    private Camp camp;

    @ManyToOne
    @JoinColumn(name = "camp_site_id", nullable = false, columnDefinition = "INT UNSIGNED")
    private CampSite campSite;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false, columnDefinition = "INT UNSIGNED")
    private Reservation reservation;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Builder.Default
    @Column(name = "is_recommend", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private boolean isRecommend = false;

    @Builder.Default
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewImage> reviewImages = new ArrayList<>();
}