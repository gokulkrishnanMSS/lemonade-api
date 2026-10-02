package com.lemon.lemonade.services;

import com.lemon.lemonade.Exceptions.EntityNotFountException;
import com.lemon.lemonade.Exceptions.ForbiddenException;
import com.lemon.lemonade.dto.TuneRequest;
import com.lemon.lemonade.dto.TuneResponse;
import com.lemon.lemonade.models.Rating;
import com.lemon.lemonade.models.Tune;
import com.lemon.lemonade.models.User;
import com.lemon.lemonade.repositories.TuneRepository;
import com.lemon.lemonade.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TuneService {

    private final TuneRepository tuneRepository;
    private final UserRepository userRepository;

    public TuneResponse create(String userId, TuneRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("A tune needs a name");
        }
        Rating rating = validated(request.rating());
        Tune tune = Tune.builder()
                .id(UUID.randomUUID().toString())
                .name(request.name())
                .owner(findUser(userId))
                .rating(rating)
                .build();
        return toResponse(tuneRepository.save(tune), userId);
    }

    @Transactional(readOnly = true)
    public List<TuneResponse> listUsable(String userId) {
        return tuneRepository.findUsableBy(userId).stream()
                .map(tune -> toResponse(tune, userId))
                .toList();
    }

    public TuneResponse share(String userId, String tuneId, String email) {
        Tune tune = findUsable(userId, tuneId);
        if (!tune.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Only the owner can share a tune");
        }
        User friend = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFountException("User not found: " + email));
        if (friend.getId().equals(userId)) {
            throw new IllegalArgumentException("You already own this tune");
        }
        tune.getSharedWith().add(friend);
        return toResponse(tune, userId);
    }

    /** Finds a tune the user owns or was shared. Others get a 404 so tune IDs can't be probed. */
    @Transactional(readOnly = true)
    public Tune findUsable(String userId, String tuneId) {
        return tuneRepository.findById(tuneId)
                .filter(tune -> tune.isUsableBy(userId))
                .orElseThrow(() -> new EntityNotFountException("Tune not found: " + tuneId));
    }

    private User findUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFountException("User not found: " + userId));
    }

    private static Rating validated(Rating rating) {
        if (rating == null) {
            throw new IllegalArgumentException("A tune needs a rating");
        }
        requireInRange("maay", rating.getMaay());
        requireInRange("maana", rating.getMaana());
        requireInRange("will", rating.getWill());
        return rating;
    }

    private static void requireInRange(String name, Integer value) {
        if (value == null || value < Rating.MIN || value > Rating.MAX) {
            throw new IllegalArgumentException(
                    "Rating " + name + " must be between " + Rating.MIN + " and " + Rating.MAX + ", got: " + value);
        }
    }

    private static TuneResponse toResponse(Tune tune, String userId) {
        boolean isOwner = tune.getOwner().getId().equals(userId);
        return new TuneResponse(
                tune.getId(),
                tune.getName(),
                tune.getOwner().getEmail(),
                tune.getRating(),
                isOwner ? tune.getSharedWith().stream().map(User::getEmail).sorted().toList() : null);
    }
}
