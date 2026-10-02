package com.lemon.lemonade.dto;

import com.lemon.lemonade.models.CardType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "New card, rated from a tune the user owns or was shared")
public record CardRequest(
        String tuneId,
        @Schema(example = "GOLD") CardType type
) {
}
