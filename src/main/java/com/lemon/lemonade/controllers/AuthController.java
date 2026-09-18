package com.lemon.lemonade.controllers;

import com.lemon.lemonade.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login with Google", description = "Authenticates a user using a Google server auth code")
    public ResponseEntity<String> login(
            @Parameter(description = "Google Server Auth Code") @RequestBody String serverAuthCode) {
        try {
            String accessToken = authService.loginWithGoogle(serverAuthCode);
            return ResponseEntity.ok(accessToken);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error during authentication: " + e.getMessage());
        }
    }
}
