package com.lemon.lemonade.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String id;

    @Column(unique = true)
    private String email;
    private String name;
    private String profilePicture;

    // BCrypt hash. Ignored by Jackson so it can't leak into a login token's subject or an API response.
    @JsonIgnore
    private String password;
}
