package site.campingon.campingon.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // TODO: 참고 예시입니다.
    /*
    400 BAD_REQUEST : 잘못된 요청
    HAS_EMAIL(HttpStatus.BAD_REQUEST, "ACCOUNT-002", "존재하는 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "ACCOUNT-003", "비밀번호가 일치하지 않습니다."),

    401 UNAUTHORIZED : 인증되지 않은 사용자
    INVALID_AUTH_TOKEN(HttpStatus.UNAUTHORIZED, "권한 정보가 없는 토큰입니다."),

    404 NOT_FOUND : Resource를 찾을 수 없음
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 정보의 사용자를 찾을 수 없습니다."),

    409 : CONFLICT : Resource의 현재 상태와 충돌. 보통 중복된 데이터 존재
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "데이터가 이미 존재합니다."),
    DUPLICATE_CATEGORY(HttpStatus.CONFLICT, "CATEGORY-03", "이미 존재하는 카테고리입니다.");
    */

    TEST_ERROR_TYPE(HttpStatus.NOT_FOUND, "TEST-01", "테스트 에러 객체입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}
