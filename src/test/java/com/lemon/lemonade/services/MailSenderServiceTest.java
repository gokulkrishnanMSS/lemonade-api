package com.lemon.lemonade.services;

import com.lemon.lemonade.Exceptions.InvalidOtpException;
import com.lemon.lemonade.Exceptions.OtpExpiredException;
import com.lemon.lemonade.Exceptions.OtpNotFoundException;
import com.lemon.lemonade.Exceptions.UserAlreadyExistException;
import com.lemon.lemonade.models.User;
import com.lemon.lemonade.repositories.UserRepository;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MailSenderServiceTest {

    private static final String EMAIL = "someone@gmail.com";

    private final RedisService redisService = mock(RedisService.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final MailSenderService mailSenderService = new MailSenderService(redisService, userRepository, mailSender);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mailSenderService, "sender", "lemonade@gmail.com");
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((jakarta.mail.Session) null));
    }

    @Test
    void mailsASixDigitOtpAndKeepsItForFiveMinutes() {
        mailSenderService.sendOtpForVerification(EMAIL);

        ArgumentCaptor<String> otp = ArgumentCaptor.forClass(String.class);
        verify(redisService).addItem(eq("otp:" + EMAIL), otp.capture(), eq(Duration.ofMinutes(5)));
        assertThat(otp.getValue()).matches("\\d{6}");
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void refusesToSendToAnEmailThatAlreadySignedUp() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> mailSenderService.sendOtpForVerification(EMAIL))
                .isInstanceOf(UserAlreadyExistException.class);
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void refusesInvalidEmailAddresses() {
        assertThatThrownBy(() -> mailSenderService.sendOtpForVerification("not-an-email"))
                .isInstanceOf(IllegalArgumentException.class);
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void marksTheEmailVerifiedWhenTheOtpMatches() {
        when(redisService.getItem("otp:" + EMAIL)).thenReturn("123456");

        mailSenderService.verifyOtp(EMAIL, "123456");

        ArgumentCaptor<String> stored = ArgumentCaptor.forClass(String.class);
        verify(redisService).addItem(eq("otp:" + EMAIL), stored.capture(), eq(Duration.ofMinutes(5)));
        assertThat(stored.getValue()).isEqualTo("VERIFIED:123456");

        // signup accepts exactly what verifyOtp stored
        when(redisService.getItem("otp:" + EMAIL)).thenReturn(stored.getValue());
        mailSenderService.requireVerified(EMAIL);
    }

    @Test
    void rejectsAWrongOtpAndGivesUpAfterFiveTries() {
        when(redisService.getItem("otp:" + EMAIL)).thenReturn("123456");
        when(redisService.increment(eq("otp:attempts:" + EMAIL), any())).thenReturn(1L, 2L, 3L, 4L, 5L);

        for (int attempt = 1; attempt < 5; attempt++) {
            assertThatThrownBy(() -> mailSenderService.verifyOtp(EMAIL, "000000")).isInstanceOf(InvalidOtpException.class);
        }
        assertThatThrownBy(() -> mailSenderService.verifyOtp(EMAIL, "000000"))
                .isInstanceOf(OtpExpiredException.class)
                .hasMessageContaining("Too many wrong attempts");
        verify(redisService).removeItem("otp:" + EMAIL);
    }

    @Test
    void reportsAnExpiredOrUnverifiedOtp() {
        when(redisService.getItem("otp:" + EMAIL)).thenReturn(null);
        assertThatThrownBy(() -> mailSenderService.verifyOtp(EMAIL, "123456")).isInstanceOf(OtpExpiredException.class);
        assertThatThrownBy(() -> mailSenderService.requireVerified(EMAIL)).isInstanceOf(OtpNotFoundException.class);

        when(redisService.getItem("otp:" + EMAIL)).thenReturn("123456");
        assertThatThrownBy(() -> mailSenderService.requireVerified(EMAIL)).isInstanceOf(OtpExpiredException.class);
    }
}
