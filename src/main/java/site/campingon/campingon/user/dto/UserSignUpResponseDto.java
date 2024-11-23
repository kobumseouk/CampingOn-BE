package site.campingon.campingon.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSignUpResponseDto {
    private Long id;       // 사용자 ID
    private String email;  // 이메일
    private String nickname; // 닉네임
}