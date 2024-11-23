package site.campingon.campingon.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSignUpRequestDto {
    private String email;
    private String password; // 평문 비밀번호
    private String nickname;
    private String name;
}
