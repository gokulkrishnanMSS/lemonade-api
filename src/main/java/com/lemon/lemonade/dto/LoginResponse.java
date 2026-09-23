package com.lemon.lemonade.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Login token, sent back in the Authorization header on later requests")
public record LoginResponse(
        @Schema(example = "eyJhbGciOiJIUzI1NiJ9...") String token,
        @Schema(description = "RFC 3339 timestamp the token stops working at") String expiresAt
) {
}
