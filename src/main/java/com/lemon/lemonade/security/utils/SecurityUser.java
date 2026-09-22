package com.lemon.lemonade.security.utils;

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
}
