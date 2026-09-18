package com.lemon.lemonade.security.userDetails;

import com.lemon.lemonade.models.User;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class UserDetail implements UserDetails {

    private final User user;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public @Nullable String getPassword() {
        return "OAUTH_USER";
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }
}
