package com.lemon.lemonade.security.utils;

import com.lemon.lemonade.models.User;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@Builder
public class SecurityUser {
    private String email;
    private String password;
    private String role;
    private Boolean isValid;
    private String token;
    private UserDetails userDetails;
    private User user;
}
