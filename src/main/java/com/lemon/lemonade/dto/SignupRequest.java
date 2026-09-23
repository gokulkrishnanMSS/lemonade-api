package com.lemon.lemonade.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "New account, for an email whose OTP has been verified")
public record SignupRequest(
        @Schema(example = "Gokul") String name,
        @Schema(example = "someone@gmail.com") String email,
        @Schema(description = "At least 8 characters", example = "sunny-day-42") String password
) {
}
