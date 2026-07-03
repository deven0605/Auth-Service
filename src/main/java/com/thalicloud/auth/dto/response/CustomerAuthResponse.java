package com.thalicloud.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CustomerAuthResponse {
    private final String accessToken;
    private final String refreshToken;
    // @JsonProperty forces the JSON key to "isNewUser" regardless of the field/getter name
    @JsonProperty("isNewUser")
    private final boolean newUser;
}
