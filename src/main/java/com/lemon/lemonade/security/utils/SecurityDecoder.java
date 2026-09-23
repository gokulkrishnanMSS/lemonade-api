package com.lemon.lemonade.security.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemon.lemonade.Exceptions.TokenNotValidException;
import com.lemon.lemonade.models.User;
import com.lemon.lemonade.repositories.UserRepository;
import com.lemon.lemonade.security.userDetails.UserDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SecurityDecoder {

    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    @Value("${security.secret.key}")
    private String secretKey;

    @Value("${jwt.issuer.name}")
    private String issuer;

    public SecurityUser getUserFromJwt(String jwt) throws JsonProcessingException {
        // Checks the signature and the expiry, so a token this app didn't issue is rejected
        DecodedJWT decodedJWT;
        try {
            decodedJWT = JWT.require(Algorithm.HMAC256(secretKey))
                    .withIssuer(issuer)
                    .build()
                    .verify(jwt);
        } catch (JWTVerificationException e) {
            throw new TokenNotValidException("Token not valid, log in again");
        }

        User tokenUser = objectMapper.readValue(decodedJWT.getSubject(), User.class);
        Optional<User> userOptional = userRepository.findById(tokenUser.getId());
        if (userOptional.isEmpty()) {
            throw new TokenNotValidException("User not found");
        }

        User user = userOptional.get();
        return SecurityUser
                .builder()
                .email(user.getEmail())
                .userDetails(new UserDetail(user))
                .isValid(true)
                .build();
    }
}
