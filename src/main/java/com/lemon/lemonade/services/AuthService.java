package com.lemon.lemonade.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemon.lemonade.Exceptions.PasswordNotValidException;
import com.lemon.lemonade.Exceptions.TokenNotValidException;
import com.lemon.lemonade.Exceptions.UserAlreadyExistException;
import com.lemon.lemonade.dto.LoginRequest;
import com.lemon.lemonade.dto.LoginResponse;
import com.lemon.lemonade.dto.SignupRequest;
import com.lemon.lemonade.dto.TokenResponse;
import com.lemon.lemonade.models.User;
import com.lemon.lemonade.repositories.UserRepository;
import com.lemon.lemonade.security.utils.SecurityDecoder;
import com.lemon.lemonade.security.utils.SecurityUser;
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

    private static final Duration TOKEN_TTL = Duration.ofHours(1);
    private static final Duration REF_TOKEN_TTL = Duration.ofDays(300);
    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserRepository userRepository;
    private final MailSenderService mailSenderService;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private final SecurityDecoder securityDecoder;

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
        Instant refTokenExpAt =  Instant.now().plus(REF_TOKEN_TTL);
        return new LoginResponse(createToken(user, expiresAt ,  refTokenExpAt), expiresAt.toString());
    }

    public TokenResponse getRefToken(String token) {
        if (token == null || token.isBlank()) {
            throw new TokenNotValidException("Token not valid");
        }
        String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        try {
            SecurityUser securityUser = securityDecoder.getUserFromJwt(cleanToken);
            User user = securityUser.getUser();
            Instant expiresAt = Instant.now().plus(TOKEN_TTL);
            String accessToken = createAccessToken(user, expiresAt);
            return new TokenResponse(cleanToken, accessToken);
        } catch (JsonProcessingException e) {
            throw new TokenNotValidException("Token not valid, log in again");
        }
    }

    private String createAccessToken(User user, Instant expiresAt) {
        try {
            return JWT.create()
                    .withIssuer(issuer)
                    // SecurityDecoder reads the user back out of the subject; the password is @JsonIgnore'd
                    .withSubject(objectMapper.writeValueAsString(user))
                    .withIssuedAt(new Date())
                    .withExpiresAt(Date.from(expiresAt))
                    .sign(Algorithm.HMAC256(secretKey));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not create the access token", e);
        }
    }

    private TokenResponse createToken(User user, Instant expiresAt, Instant refTokenExp) {
        try {
            String accessToken = createAccessToken(user, expiresAt);

            String refreshToken = JWT.create()
                    .withIssuer(issuer)
                    // SecurityDecoder reads the user back out of the subject; the password is @JsonIgnore'd
                    .withSubject(objectMapper.writeValueAsString(user))
                    .withIssuedAt(new Date())
                    .withExpiresAt(Date.from(refTokenExp))
                    .sign(Algorithm.HMAC256(secretKey));

            return new TokenResponse(refreshToken, accessToken);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not create the login token", e);
        }
    }
}
