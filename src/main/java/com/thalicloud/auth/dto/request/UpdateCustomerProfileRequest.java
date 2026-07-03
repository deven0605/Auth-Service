package com.thalicloud.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpdateCustomerProfileRequest {

    @Size(max = 100)
    private String fullName;

    @Email
    @Size(max = 150)
    private String email;

    @Size(max = 500)
    private String profilePicUrl;
}
