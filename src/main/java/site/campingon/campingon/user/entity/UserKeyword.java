package site.campingon.campingon.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class UserKeyword {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(columnDefinition = "INT UNSIGNED")
  private Long id;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  private String keyword;
}
