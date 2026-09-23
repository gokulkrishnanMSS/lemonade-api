package com.lemon.lemonade.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Login details")
public record LoginRequest(
        @Schema(example = "someone@gmail.com") String email,
        @Schema(example = "sunny-day-42") String password
) {
}
