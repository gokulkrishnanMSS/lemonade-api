package com.lemon.lemonade.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.lemon.lemonade.models.User;
import com.lemon.lemonade.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public String loginWithGoogle(String serverAuthCode) throws IOException {
        NetHttpTransport transport = new NetHttpTransport();
        GsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        
        // Load the client secrets from the resources directory
        GoogleClientSecrets clientSecrets;
        try (InputStreamReader reader = new InputStreamReader(
                new ClassPathResource("client_secret.json").getInputStream())) {
            clientSecrets = GoogleClientSecrets.load(jsonFactory, reader);
        }

        GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                transport, jsonFactory, "https://oauth2.googleapis.com/token",
                clientSecrets.getDetails().getClientId(),
                clientSecrets.getDetails().getClientSecret(),
                serverAuthCode, ""
        ).execute();

        // 1. Get the Drive Tokens
        String refreshToken = tokenResponse.getRefreshToken();
        String accessToken = tokenResponse.getAccessToken();

        // 2. Parse the ID Token to get the User Profile
        GoogleIdToken idToken = tokenResponse.parseIdToken();
        GoogleIdToken.Payload payload = idToken.getPayload();

        // 3. Extract the credentials
        String googleUserId = payload.getSubject(); // Google's unique ID for this user
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String profilePicture = (String) payload.get("picture");

        System.out.println("New user signed in: " + name + " (" + email + ")");
        
        // 4. Save or update user in the Database
        User user = userRepository.findById(googleUserId).orElse(new User());
        user.setId(googleUserId);
        user.setEmail(email);
        user.setName(name);
        user.setProfilePicture(profilePicture);
        
        if (refreshToken != null) {
            user.setGoogleRefreshToken(refreshToken);
        }
        
        userRepository.save(user);
        
        return accessToken;
    }
}
