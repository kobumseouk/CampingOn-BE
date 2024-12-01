package site.campingon.campingon.common.public_data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum GoCampingPath {
    BASED_LIST("/basedList"),
    LOCATION_BASED_LIST("/locationBasedList"),
    SEARCH_LIST("/searchList"),
    IMAGE_LIST("/imageList"),
    BASED_SYNC_LIST("/basedSyncList");

    private final String path;
}
