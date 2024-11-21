package site.campingon.campingon.user.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.campingon.campingon.common.entity.BaseEntity;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 24, unique = true)
    private String nickname;

    @Builder.Default
    @Column(name="is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name="delete_reason", columnDefinition = "TEXT")
    private String deleteReason;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // 회원 탈퇴 로직
    public void deleteUser(String deleteReason) {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deleteReason = deleteReason;
    }

}
