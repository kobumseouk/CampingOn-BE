package site.campingon.campingon.common.public_data.mapper;

import org.mapstruct.Mapper;
import site.campingon.campingon.common.public_data.dto.GoCampingDataDto;
import site.campingon.campingon.common.public_data.dto.GoCampingImageDto;
import site.campingon.campingon.common.public_data.dto.GoCampingImageParsedResponseDto;
import site.campingon.campingon.common.public_data.dto.GoCampingParsedResponseDto;

import java.util.List;

@Mapper(componentModel = "spring") // Spring에서 관리되는 Bean으로 등록되도록 설정
public interface GoCampingMapper {

    GoCampingParsedResponseDto toGoCampingParsedResponseDto(GoCampingDataDto.Item item);

    List<GoCampingParsedResponseDto> toGoCampingParsedResponseDtoList(List<GoCampingDataDto.Item> items);

    List<GoCampingImageParsedResponseDto> toGoCampingImageParsedResponseDtoList(List<GoCampingImageDto.Item> items);
}
