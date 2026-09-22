package com.lemon.lemonade.security.manager;

import com.lemon.lemonade.security.providers.UserProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RequiredArgsConstructor
public class AuthenticationManager implements org.springframework.security.authentication.AuthenticationManager {

    private final UserProvider userProvider;
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if(userProvider.supports(authentication.getClass())){
            HttpServletRequest request =
                    ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
           return userProvider.authenticate(authentication);
        }

        return authentication;
    }
}
