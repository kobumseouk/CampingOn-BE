package site.campingon.campingon.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDeactivateRequestDto {
    private Long id;
    private String deleteReason;
}
