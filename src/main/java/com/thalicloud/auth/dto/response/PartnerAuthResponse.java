package com.thalicloud.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.thalicloud.auth.enums.PartnerLifecycleState;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PartnerAuthResponse {
    private final String accessToken;
    private final String refreshToken;
    // @JsonProperty forces the JSON key to "isNewUser" regardless of the field/getter name
    @JsonProperty("isNewUser")
    private final boolean newUser;
    // FR-1.8: current lifecycleState, used by the app to route (FR-1.10)
    private final PartnerLifecycleState lifecycleState;
}
