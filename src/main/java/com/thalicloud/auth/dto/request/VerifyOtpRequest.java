package com.thalicloud.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VerifyOtpRequest {

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "\\+91[6-9]\\d{9}", message = "Phone must be a valid Indian mobile number in +91XXXXXXXXXX format")
    private String phone;

    @NotBlank(message = "OTP is required")
    @Size(min = 4, max = 4, message = "OTP must be exactly 4 digits")
    @Pattern(regexp = "\\d{4}", message = "OTP must contain only digits")
    private String otp;
}
