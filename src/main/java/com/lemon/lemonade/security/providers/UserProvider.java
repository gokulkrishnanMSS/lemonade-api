package com.lemon.lemonade.security.providers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lemon.lemonade.security.authentication.UserAuthentication;
import com.lemon.lemonade.security.utils.SecurityDecoder;
import com.lemon.lemonade.security.utils.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserProvider implements AuthenticationProvider {

    private final SecurityDecoder securityDecoder;

    @Override
    public  Authentication authenticate(Authentication authentication) throws AuthenticationException {

        UserAuthentication userAuthentication = (UserAuthentication) authentication;
        String token = userAuthentication.getToken();
        try {
            SecurityUser user = securityDecoder.getUserFromJwt(token);
            if(user.getIsValid()){
                userAuthentication.setAuthenticated(true);
                return userAuthentication;
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return authentication;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return true;
    }
}
