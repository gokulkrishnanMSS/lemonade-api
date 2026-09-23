package com.lemon.lemonade.services;

import com.lemon.lemonade.Exceptions.InvalidOtpException;
import com.lemon.lemonade.Exceptions.OtpExpiredException;
import com.lemon.lemonade.Exceptions.OtpNotFoundException;
import com.lemon.lemonade.Exceptions.UserAlreadyExistException;
import com.lemon.lemonade.repositories.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;

/** Emails a one-time password and checks it back, keeping it in Redis for as long as it's valid. */
@Service
@RequiredArgsConstructor
public class MailSenderService {

    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private static final int MAX_ATTEMPTS = 5;
    private static final String VERIFIED_PREFIX = "VERIFIED:";

    private static final SecureRandom RANDOM = new SecureRandom();

    private final RedisService redisService;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String sender;

    /** Mails a fresh OTP to an address that hasn't signed up yet. */
    public String sendOtpForVerification(String email) {
        String address = requireEmail(email);
        if (userRepository.findByEmail(address).isPresent()) {
            throw new UserAlreadyExistException("A user with this email already exists");
        }

        String otp = generateOtp();
        redisService.addItem(otpKey(address), otp, OTP_TTL);
        redisService.removeItem(attemptsKey(address));
        send(address, otp);
        return "OTP sent to " + address;
    }

    /** Checks an OTP and, when it matches, remembers the address as verified for another 5 minutes. */
    public String verifyOtp(String email, String otp) {
        String address = requireEmail(email);
        String storedOtp = redisService.getItem(otpKey(address));
        if (storedOtp == null) {
            throw new OtpExpiredException("OTP expired, request a new one");
        }
        if (!storedOtp.equals(otp)) {
            // A 6-digit OTP is easy to guess at, so only a few tries are allowed
            if (redisService.increment(attemptsKey(address), OTP_TTL) >= MAX_ATTEMPTS) {
                redisService.removeItem(otpKey(address));
                throw new OtpExpiredException("Too many wrong attempts, request a new OTP");
            }
            throw new InvalidOtpException("OTP is not valid");
        }

        redisService.removeItem(attemptsKey(address));
        redisService.addItem(otpKey(address), VERIFIED_PREFIX + otp, OTP_TTL);
        return "OTP verified";
    }

    /** Fails unless {@link #verifyOtp} has accepted an OTP for this address within the last 5 minutes. */
    void requireVerified(String email) {
        String storedOtp = redisService.getItem(otpKey(email));
        if (storedOtp == null) {
            throw new OtpNotFoundException("No verified OTP for this email, request one and verify it");
        }
        if (!storedOtp.startsWith(VERIFIED_PREFIX)) {
            throw new OtpExpiredException("Email is not verified yet");
        }
    }

    void clearOtp(String email) {
        redisService.removeItem(otpKey(email));
        redisService.removeItem(attemptsKey(email));
    }

    private void send(String email, String otp) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(sender);
            helper.setTo(email);
            helper.setSubject("Your Lemonade OTP");
            helper.setText(template(otp), true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new MailSendException("Could not send the OTP email", e);
        }
    }

    private static String generateOtp() {
        return String.valueOf(100000 + RANDOM.nextInt(900000));
    }

    private static String requireEmail(String email) {
        String address = email == null ? "" : email.strip();
        if (!address.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new IllegalArgumentException("Not a valid email address: " + email);
        }
        return address;
    }

    private static String otpKey(String email) {
        return "otp:" + email;
    }

    private static String attemptsKey(String email) {
        return "otp:attempts:" + email;
    }

    private static String template(String otp) {
        return """
                <html>
                  <head>
                    <style>
                      body { font-family: Arial, sans-serif; background-color: #f9f9fb; padding: 30px; }
                      .container { background-color: #ffffff; padding: 30px; max-width: 600px; margin: 0 auto;
                        border-radius: 12px; box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08); text-align: center; }
                      h2 { color: #FFC107; margin-bottom: 10px; font-size: 24px; }
                      .otp-box { display: inline-block; background-color: #fff3cd; color: #856404; padding: 18px 32px;
                        border-radius: 10px; font-size: 34px; font-weight: bold; letter-spacing: 10px; margin: 20px 0;
                        border: 1px solid #ffeeba; }
                      p { color: #444; font-size: 16px; line-height: 1.5; }
                      .footer { margin-top: 30px; font-size: 12px; color: #777; }
                    </style>
                  </head>
                  <body>
                    <div class="container">
                      <h2>&#128274; OTP Verification</h2>
                      <p>Please use the following One-Time Password (OTP) to continue:</p>
                      <div class="otp-box">%s</div>
                      <p>This OTP will expire in <strong>5 minutes</strong>.</p>
                      <p>&#128683; Do not share this OTP with anyone for your security.</p>
                      <div class="footer">&copy; %d Lemonade. All rights reserved.</div>
                    </div>
                  </body>
                </html>
                """.formatted(otp, LocalDate.now().getYear());
    }
}
