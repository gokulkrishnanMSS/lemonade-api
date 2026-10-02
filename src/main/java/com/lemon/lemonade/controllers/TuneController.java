package com.lemon.lemonade.controllers;

import com.lemon.lemonade.dto.ShareTuneRequest;
import com.lemon.lemonade.dto.TuneRequest;
import com.lemon.lemonade.dto.TuneResponse;
import com.lemon.lemonade.security.userDetails.UserDetail;
import com.lemon.lemonade.services.TuneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/tunes")
@RequiredArgsConstructor
@Tag(name = "Tunes", description = "Shareable ratings that cards are created from")
public class TuneController {

    private final TuneService tuneService;

    @PostMapping
    @Operation(summary = "Create a tune", description = "Creates a tune owned by the logged-in user")
    public ResponseEntity<TuneResponse> create(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetail principal,
            @RequestBody TuneRequest request) {
        TuneResponse created = tuneService.create(principal.getUser().getId(), request);
        return ResponseEntity.created(URI.create("/tunes/" + created.id())).body(created);
    }

    @GetMapping
    @Operation(summary = "List tunes", description = "Lists the tunes the logged-in user owns or was shared")
    public List<TuneResponse> list(@Parameter(hidden = true) @AuthenticationPrincipal UserDetail principal) {
        return tuneService.listUsable(principal.getUser().getId());
    }

    @PostMapping("/{tuneId}/share")
    @Operation(summary = "Share a tune", description = "Lets another user create cards from a tune. Only the owner can share.")
    public TuneResponse share(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetail principal,
            @PathVariable String tuneId,
            @RequestBody ShareTuneRequest request) {
        return tuneService.share(principal.getUser().getId(), tuneId, request.email());
    }
}
