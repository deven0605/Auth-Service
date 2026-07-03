package com.thalicloud.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SendOtpRequest {

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "\\+91[6-9]\\d{9}", message = "Phone must be a valid Indian mobile number in +91XXXXXXXXXX format")
    private String phone;
}
