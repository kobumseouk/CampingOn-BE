package site.campingon.campingon.common.public_data.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.campingon.campingon.common.public_data.dto.GoCampingRequestDto;
import site.campingon.campingon.common.public_data.dto.GoCampingResponseDto;
import site.campingon.campingon.common.public_data.mapper.GoCampingMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GoCampingService {

    private final GoCampingMapper goCampingMapper;

    public GoCampingResponseDto publicDataFilter(GoCampingRequestDto response) {
        GoCampingRequestDto.Item item = response.getResponse().getBody().getItems().getItem().getFirst();

        return goCampingMapper.toGoCampingResponseDto(item);
    }
}
