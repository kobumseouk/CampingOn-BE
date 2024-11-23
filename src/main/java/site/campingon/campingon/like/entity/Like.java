package site.campingon.campingon.like.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.campingon.campingon.camp.entity.Camp;
import site.campingon.campingon.user.entity.User;


@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Like {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(columnDefinition = "INT UNSIGNED")
  private Long id;

  @ManyToOne
  @JoinColumn(name = "camp_id", nullable = false)
  private Camp camp;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "is_like", nullable = false)
  private boolean isLike;
}