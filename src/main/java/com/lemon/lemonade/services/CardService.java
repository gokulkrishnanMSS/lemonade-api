package com.lemon.lemonade.services;

import com.lemon.lemonade.Exceptions.EntityNotFountException;
import com.lemon.lemonade.dto.CardRequest;
import com.lemon.lemonade.dto.CardResponse;
import com.lemon.lemonade.models.Card;
import com.lemon.lemonade.models.Rating;
import com.lemon.lemonade.models.Tune;
import com.lemon.lemonade.repositories.CardRepository;
import com.lemon.lemonade.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final TuneService tuneService;

    public CardResponse create(String userId, CardRequest request) {
        if (request.type() == null) {
            throw new IllegalArgumentException("A card needs a type: GOLD, SILVER, PLATINUM or DIAMOND");
        }
        if (request.tuneId() == null) {
            throw new IllegalArgumentException("A card needs a tune to be rated from");
        }
        Tune tune = tuneService.findUsable(userId, request.tuneId());
        Rating rating = tune.getRating().toBuilder().build();
        Card card = Card.builder()
                .id(UUID.randomUUID().toString())
                .type(request.type())
                .owner(userRepository.findById(userId)
                        .orElseThrow(() -> new EntityNotFountException("User not found: " + userId)))
                .rating(rating)
                .tune(tune)
                .build();
        return toResponse(cardRepository.save(card));
    }

    @Transactional(readOnly = true)
    public List<CardResponse> listOwned(String userId) {
        return cardRepository.findByOwnerId(userId).stream()
                .map(CardService::toResponse)
                .toList();
    }

    private static CardResponse toResponse(Card card) {
        return new CardResponse(
                card.getId(),
                card.getType(),
                card.getOwner().getEmail(),
                card.getTune() != null ? card.getTune().getId() : null,
                card.getRating());
    }
}
