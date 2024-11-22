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
  @JoinColumn(name = "camp_id")
  private Camp camp;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  private boolean isLike;
}
