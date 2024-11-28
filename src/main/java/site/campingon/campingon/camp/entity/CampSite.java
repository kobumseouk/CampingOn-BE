package site.campingon.campingon.camp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import site.campingon.campingon.common.converter.IndutyConverter;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "camp_site")
public class CampSite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "camp_id", nullable = false)
    private Camp camp; // 캠핑장 (N:1 관계)

//    @Column(name = "maximum_people", nullable = false)
//    @Builder.Default
//    private Integer maximumPeople = null; // 최대 수용 인원
//
//    @Column(nullable = false)
//    @Builder.Default
//    private Integer price = null; // 가격

    @Column(length = 50, nullable = false)
    @Convert(converter = IndutyConverter.class) //converter 사용
    private Induty siteType; // 업종 구분

    @Column(name = "indoor_facility", length = 255)
    private String indoorFacility;

// @Builder.Default
    @Column(name = "is_available", nullable = false)
    private boolean isAvailable;
//
//    @PrePersist
//    @PreUpdate
//    private void setDefaultValues() {
//        if (price == null) {
//            this.price = siteType.getPrice();
//        }
//        if (maximumPeople == null) {
//            this.maximumPeople = siteType.getMaximum_people();
//        }
//    }
}