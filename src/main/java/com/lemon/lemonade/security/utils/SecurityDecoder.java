package com.lemon.lemonade.security.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemon.lemonade.Exceptions.TokenNotValidException;
import com.lemon.lemonade.models.User;
import com.lemon.lemonade.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
@AllArgsConstructor
public class SecurityDecoder {

    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    private Boolean isAuthenticated(Date expirationDate){
        return expirationDate.after(new Date());
    }

    public SecurityUser getUserFromJwt(String jwt) throws JsonProcessingException {
        DecodedJWT decodedJWT =  JWT.decode(jwt);
        User admin  = objectMapper.readValue(decodedJWT.getSubject() , User.class);
        Optional<User> userOptional =  userRepository.findById(admin.getId());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if(isAuthenticated(decodedJWT.getExpiresAt())){
                return SecurityUser
                        .builder()
                        .email(user.getEmail())
                        .isValid(true)
                        .build();
            }
        }else {
            throw new TokenNotValidException("Admin not found");
        }

        throw new TokenNotValidException("Token expired... log in again");
    }


}
