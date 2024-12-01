package site.campingon.campingon.common.public_data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoCampingImageParsedResponseDto {
    private long contentId;
    private long serialnum;
    private String imageUrl;
    private String createdtime; //등록일
    private String modifiedtime;    //수정일
}
