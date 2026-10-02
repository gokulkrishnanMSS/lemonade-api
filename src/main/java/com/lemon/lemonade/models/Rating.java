package com.lemon.lemonade.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Shared by tunes and cards: a card copies the ratings of the tune it was made from
@Embeddable
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ratings from 0 to 100")
public class Rating {

    public static final int MIN = 0;
    public static final int MAX = 100;

    @Schema(example = "80")
    private Integer maay;
    @Schema(example = "65")
    private Integer maana;
    @Schema(example = "90")
    private Integer will;
}
