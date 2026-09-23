package com.lemon.lemonade.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Plain result message")
public record MessageResponse(@Schema(example = "OTP sent") String message) {
}
