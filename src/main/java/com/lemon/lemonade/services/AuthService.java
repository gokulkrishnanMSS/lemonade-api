package com.lemon.lemonade.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemon.lemonade.Exceptions.PasswordNotValidException;
import com.lemon.lemonade.Exceptions.UserAlreadyExistException;
import com.lemon.lemonade.dto.LoginRequest;
import com.lemon.lemonade.dto.LoginResponse;
import com.lemon.lemonade.dto.SignupRequest;
import com.lemon.lemonade.models.User;
import com.lemon.lemonade.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Duration TOKEN_TTL = Duration.ofDays(14);
    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserRepository userRepository;
    private final MailSenderService mailSenderService;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Value("${security.secret.key}")
    private String secretKey;

    @Value("${jwt.issuer.name}")
    private String issuer;

    /** Creates an account, but only for an email whose OTP was verified in the last 5 minutes. */
    public String signup(SignupRequest request) {
        String email = request.email() == null ? "" : request.email().strip();
        mailSenderService.requireVerified(email);

        if (request.password() == null || request.password().length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistException("A user with this email already exists");
        }

        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .email(email)
                .name(request.name().strip())
                .password(passwordEncoder.encode(request.password()))
                .build();
        userRepository.save(user);
        mailSenderService.clearOtp(email);
        return "Signup successful";
    }

    public LoginResponse login(LoginRequest request) {
        // The same message either way, so this can't be used to find out which emails have accounts
        PasswordNotValidException notValid = new PasswordNotValidException("Email or password is not valid");
        User user = userRepository.findByEmail(request.email() == null ? "" : request.email().strip())
                .orElseThrow(() -> notValid);
        if (request.password() == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw notValid;
        }

        Instant expiresAt = Instant.now().plus(TOKEN_TTL);
        return new LoginResponse(createToken(user, expiresAt), expiresAt.toString());
    }

    private String createToken(User user, Instant expiresAt) {
        try {
            return JWT.create()
                    .withIssuer(issuer)
                    // SecurityDecoder reads the user back out of the subject; the password is @JsonIgnore'd
                    .withSubject(objectMapper.writeValueAsString(user))
                    .withIssuedAt(new Date())
                    .withExpiresAt(Date.from(expiresAt))
                    .sign(Algorithm.HMAC256(secretKey));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not create the login token", e);
        }
    }
}
