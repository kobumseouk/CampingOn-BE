package site.campingon.campingon.common.public_data.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import site.campingon.campingon.common.public_data.dto.GoCampingRequestDto;
import site.campingon.campingon.common.public_data.dto.GoCampingResponseDto;

import java.util.List;

@Mapper(componentModel = "spring") // Spring에서 관리되는 Bean으로 등록되도록 설정
public interface GoCampingMapper {

    // GoCampingRequestDto의 Item을 GoCampingResponseDto로 매핑
    GoCampingResponseDto toGoCampingResponseDto(GoCampingRequestDto.Item item);

}
