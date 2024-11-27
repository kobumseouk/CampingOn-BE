package site.campingon.campingon.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OAuthException extends RuntimeException {

    ErrorCode errorCode;
}
