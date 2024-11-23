package site.campingon.campingon.user.dto;

import lombok.Getter;

@Getter
public class UserDeactivateRequestDto {
    private Long id;
    private String deleteReason;
}
