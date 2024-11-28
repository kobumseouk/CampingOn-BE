package site.campingon.campingon.user.dto;

import lombok.Builder;
import lombok.Getter;
import site.campingon.campingon.user.entity.Role;


@Getter
@Builder
public class UserResponseDto {
    private Long id;          // 사용자 ID
    private String name;        // 사용자 이름
    private String email;     // 이메일
    private String nickname;  // 닉네임
    private Role role;      // 권한 (USER/ADMIN 등)
}
