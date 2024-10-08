package org.georchestra.console2.ws.backoffice.users;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

public @Data @Builder class DeletedUserDataInfo {
    private @NonNull String account;
    private Integer metadata;
    private Integer ogcStats;
}
