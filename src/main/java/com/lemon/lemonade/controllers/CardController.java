package com.lemon.lemonade.controllers;

import com.lemon.lemonade.dto.CardRequest;
import com.lemon.lemonade.dto.CardResponse;
import com.lemon.lemonade.security.userDetails.UserDetail;
import com.lemon.lemonade.services.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
@Tag(name = "Cards", description = "Gold, silver, platinum and diamond cards")
public class CardController {

    private final CardService cardService;

    @PostMapping
    @Operation(summary = "Create a card", description = "Creates a card for the logged-in user, copying the ratings of a tune they own or were shared")
    public ResponseEntity<CardResponse> create(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetail principal,
            @RequestBody CardRequest request) {
        CardResponse created = cardService.create(principal.getUser().getId(), request);
        return ResponseEntity.created(URI.create("/cards/" + created.id())).body(created);
    }

    @GetMapping
    @Operation(summary = "List cards", description = "Lists the logged-in user's cards")
    public List<CardResponse> list(@Parameter(hidden = true) @AuthenticationPrincipal UserDetail principal) {
        return cardService.listOwned(principal.getUser().getId());
    }
}
