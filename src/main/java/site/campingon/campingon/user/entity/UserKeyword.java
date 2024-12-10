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
@Table(indexes = {
        @Index(name = "idx_keyword", columnList = "keyword")
})
public class UserKeyword {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(columnDefinition = "INT UNSIGNED")
  private Long id;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private String keyword;

  @Override
  public String toString() {
    return "{user_id: " + user.getId() + ", keyword: " +  keyword + "}";
  }
}