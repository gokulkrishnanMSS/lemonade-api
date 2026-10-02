package com.lemon.lemonade.dto;

import com.lemon.lemonade.models.Rating;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "New tune")
public record TuneRequest(
        @Schema(example = "Morning tune") String name,
        Rating rating
) {
}
