package site.campingon.campingon.common.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.ResponseEntity;

@ToString
@Getter
@Builder
public class ErrorResponse {

    // 프론트에서 if(!data.success)로 에러를 확인해 메시지를 반환하기 위해 추가
    private final boolean success = false;
    private final String code;
    private final String message;

    public static ResponseEntity<ErrorResponse> toResponseEntity(ErrorCode e) {
        return ResponseEntity
                .status(e.getHttpStatus())
                .body(ErrorResponse.builder()
                        .code(e.getCode())
                        .message(e.getMessage())
                        .build());
    }

}
