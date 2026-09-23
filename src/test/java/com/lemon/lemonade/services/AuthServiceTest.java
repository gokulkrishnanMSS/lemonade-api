package com.lemon.lemonade.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemon.lemonade.Exceptions.OtpNotFoundException;
import com.lemon.lemonade.Exceptions.PasswordNotValidException;
import com.lemon.lemonade.Exceptions.UserAlreadyExistException;
import com.lemon.lemonade.dto.LoginRequest;
import com.lemon.lemonade.dto.LoginResponse;
import com.lemon.lemonade.dto.SignupRequest;
import com.lemon.lemonade.models.User;
import com.lemon.lemonade.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private static final String EMAIL = "someone@gmail.com";
    private static final String SECRET = "test-secret";

    private final UserRepository userRepository = mock(UserRepository.class);
    private final MailSenderService mailSenderService = mock(MailSenderService.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final AuthService authService =
            new AuthService(userRepository, mailSenderService, passwordEncoder, new ObjectMapper());

    {
        ReflectionTestUtils.setField(authService, "secretKey", SECRET);
        ReflectionTestUtils.setField(authService, "issuer", "lemonade");
    }

    @Test
    void storesTheUserWithAHashedPasswordOnceTheEmailIsVerified() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        authService.signup(new SignupRequest("Gokul", EMAIL, "sunny-day-42"));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getEmail()).isEqualTo(EMAIL);
        assertThat(saved.getValue().getPassword()).isNotEqualTo("sunny-day-42");
        assertThat(passwordEncoder.matches("sunny-day-42", saved.getValue().getPassword())).isTrue();
        verify(mailSenderService).clearOtp(EMAIL);
    }

    @Test
    void refusesSignupWhenTheEmailWasNeverVerified() {
        doThrow(new OtpNotFoundException("No verified OTP")).when(mailSenderService).requireVerified(EMAIL);

        assertThatThrownBy(() -> authService.signup(new SignupRequest("Gokul", EMAIL, "sunny-day-42")))
                .isInstanceOf(OtpNotFoundException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void refusesShortPasswordsAndEmailsThatAlreadySignedUp() {
        assertThatThrownBy(() -> authService.signup(new SignupRequest("Gokul", EMAIL, "short")))
                .isInstanceOf(IllegalArgumentException.class);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(new User()));
        assertThatThrownBy(() -> authService.signup(new SignupRequest("Gokul", EMAIL, "sunny-day-42")))
                .isInstanceOf(UserAlreadyExistException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void logsInWithASignedTokenThatLeavesOutThePassword() {
        User user = User.builder().id("user-1").email(EMAIL).name("Gokul")
                .password(passwordEncoder.encode("sunny-day-42")).build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        LoginResponse response = authService.login(new LoginRequest(EMAIL, "sunny-day-42"));

        DecodedJWT token = JWT.require(Algorithm.HMAC256(SECRET)).withIssuer("lemonade").build().verify(response.token());
        assertThat(token.getSubject()).contains(EMAIL).contains("user-1").doesNotContain(user.getPassword());
        assertThat(token.getExpiresAt()).isInTheFuture();
    }

    @Test
    void refusesAWrongPasswordAndAnUnknownEmailTheSameWay() {
        User user = User.builder().id("user-1").email(EMAIL).password(passwordEncoder.encode("sunny-day-42")).build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest(EMAIL, "wrong-password")))
                .isInstanceOf(PasswordNotValidException.class)
                .hasMessage("Email or password is not valid");
        assertThatThrownBy(() -> authService.login(new LoginRequest("nobody@gmail.com", "sunny-day-42")))
                .isInstanceOf(PasswordNotValidException.class)
                .hasMessage("Email or password is not valid");
    }
}
