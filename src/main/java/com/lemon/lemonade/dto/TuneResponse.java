package com.lemon.lemonade.dto;

import com.lemon.lemonade.models.Rating;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Tune owned by or shared with the logged-in user")
public record TuneResponse(
        String id,
        @Schema(example = "Morning tune") String name,
        @Schema(example = "someone@gmail.com") String ownerEmail,
        Rating rating,
        @Schema(description = "Emails the tune is shared with. Only shown to the owner.") List<String> sharedWith
) {
}
