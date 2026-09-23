package com.lemon.lemonade.controllers;

import com.lemon.lemonade.dto.LoginRequest;
import com.lemon.lemonade.dto.LoginResponse;
import com.lemon.lemonade.dto.MessageResponse;
import com.lemon.lemonade.dto.SignupRequest;
import com.lemon.lemonade.services.AuthService;
import com.lemon.lemonade.services.MailSenderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Email verification, signup and login")
public class AuthController {

    private final MailSenderService mailSenderService;
    private final AuthService authService;

    @PostMapping("/send-otp")
    @Operation(summary = "Send an OTP", description = "Emails a 6-digit OTP that is valid for 5 minutes")
    public MessageResponse sendOtp(@Parameter(example = "someone@gmail.com") @RequestParam String email) {
        return new MessageResponse(mailSenderService.sendOtpForVerification(email));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify an OTP", description = "Marks the email as verified for 5 minutes, long enough to sign up")
    public MessageResponse verifyOtp(
            @Parameter(example = "someone@gmail.com") @RequestParam String email,
            @Parameter(example = "123456") @RequestParam String otp) {
        return new MessageResponse(mailSenderService.verifyOtp(email, otp));
    }

    @PostMapping("/user")
    @Operation(summary = "Sign up", description = "Creates an account for an email whose OTP has been verified")
    public ResponseEntity<MessageResponse> signup(@RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse(authService.signup(request)));
    }

    @PostMapping("/user/login")
    @Operation(summary = "Log in", description = "Returns a token to send in the Authorization header")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
