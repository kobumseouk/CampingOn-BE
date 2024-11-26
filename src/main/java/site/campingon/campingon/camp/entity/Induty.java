package site.campingon.campingon.camp.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Induty {
    NORMAL_SITE("일반야영장"),
    CAR_SITE("자동차야영장"),
    GLAMP_SITE("글램핑"),
    CARAV_SITE("카라반"),
    PERSONAL_CARAV_SITE("카라반(개인)");

    private final String type;
}
