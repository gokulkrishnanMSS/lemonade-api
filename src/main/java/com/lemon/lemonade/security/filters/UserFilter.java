package com.lemon.lemonade.security.filters;


import com.lemon.lemonade.Exceptions.TokenNotValidException;
import com.lemon.lemonade.security.authentication.UserAuthentication;
import com.lemon.lemonade.security.manager.AuthenticationManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class UserFilter extends OncePerRequestFilter {

    private final AuthenticationManager authenticationManager;
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String token = request.getHeader("Authorization");
        if(token == null){
            throw new  TokenNotValidException("Token not valid");
        }

        UserAuthentication authentication =  UserAuthentication
                .builder()
                .token(token)
                .isValid(false)
                .userDetails(null)
                .build();
        Authentication authenticated  = authenticationManager.authenticate(authentication);
        if (authenticated.isAuthenticated()) {
            SecurityContextHolder.getContext().setAuthentication(authenticated);
        }
        filterChain.doFilter(request, response);

    }
}
