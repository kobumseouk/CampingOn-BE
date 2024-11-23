package site.campingon.campingon.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequestDto {
    private String nickname;
    private String currentPassword; // 비밀번호 변경을 위해 기존 비밀번호 확인
    private String newPassword; // 새 비밀번호
}