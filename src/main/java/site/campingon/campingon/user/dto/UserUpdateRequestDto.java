package site.campingon.campingon.user.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserUpdateRequestDto {

    @Pattern(regexp = "^[a-zA-Z0-9가-힣]{1,8}$", message = "닉네임은 알파벳, 숫자, 한글만 포함할 수 있습니다.")
    private String nickname; // 닉네임은 공백이나 null이 될 수 있음

    private String currentPassword; // 현재 비밀번호 (비밀번호 변경이 필요할 때만 필수)

    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$",
        message = "비밀번호는 8자 이상 20자 이하여야 하며, 알파벳, 숫자, 특수문자를 포함해야 합니다.")
    private String newPassword; // 새 비밀번호 (변경하지 않는 경우 null 또는 공백)
}