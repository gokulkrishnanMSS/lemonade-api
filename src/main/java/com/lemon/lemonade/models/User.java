package com.lemon.lemonade.models;

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
    private String id; // We will use the Google User ID as the primary key

    private String email;
    private String name;
    private String profilePicture;

    // This automatically encrypts/decrypts the token when saving/reading from PostgreSQL
    @Convert(converter = TokenEncryptionConverter.class) 
    @Column(name = "google_refresh_token", length = 500)
    private String googleRefreshToken; 
}
