package com.lemon.lemonade.security.authentication;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class UserAuthentication implements Authentication {

    private final String token;
    private final UserDetails userDetails;
    private boolean isValid ;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public @Nullable Object getDetails() {
        return userDetails;
    }

    @Override
    public @Nullable Object getPrincipal() {
        return userDetails;
    }

    @Override
    public boolean isAuthenticated() {
        return isValid;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.isValid = isAuthenticated;
    }

    @Override
    public String getName() {
        return userDetails == null ? "" : userDetails.getUsername();
    }
}
