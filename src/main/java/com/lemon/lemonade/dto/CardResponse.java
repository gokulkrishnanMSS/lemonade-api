package com.lemon.lemonade.dto;

import com.lemon.lemonade.models.CardType;
import com.lemon.lemonade.models.Rating;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Card owned by the logged-in user")
public record CardResponse(
        String id,
        @Schema(example = "GOLD") CardType type,
        @Schema(example = "someone@gmail.com") String ownerEmail,
        @Schema(description = "Tune the card was created from") String tuneId,
        Rating rating
) {
}
