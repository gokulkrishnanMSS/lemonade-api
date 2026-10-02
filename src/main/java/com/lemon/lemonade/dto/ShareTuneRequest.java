package com.lemon.lemonade.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User to share a tune with")
public record ShareTuneRequest(
        @Schema(example = "friend@gmail.com") String email
) {
}
