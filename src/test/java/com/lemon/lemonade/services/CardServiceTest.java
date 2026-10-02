package com.lemon.lemonade.services;

import com.lemon.lemonade.Exceptions.EntityNotFountException;
import com.lemon.lemonade.dto.CardRequest;
import com.lemon.lemonade.dto.CardResponse;
import com.lemon.lemonade.models.Card;
import com.lemon.lemonade.models.CardType;
import com.lemon.lemonade.models.Rating;
import com.lemon.lemonade.models.Tune;
import com.lemon.lemonade.models.User;
import com.lemon.lemonade.repositories.CardRepository;
import com.lemon.lemonade.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CardServiceTest {

    private final User owner = User.builder().id("owner").email("owner@gmail.com").build();
    private final User friend = User.builder().id("friend").email("friend@gmail.com").build();
    private final Tune tune = Tune.builder().id("tune").name("Morning").owner(owner).rating(new Rating(80, 65, 90)).build();

    private final CardRepository cardRepository = mock(CardRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final TuneService tuneService = mock(TuneService.class);
    private final CardService cardService = new CardService(cardRepository, userRepository, tuneService);

    @Test
    void aSharedTuneRatesTheFriendsCard() {
        when(tuneService.findUsable("friend", "tune")).thenReturn(tune);
        when(userRepository.findById("friend")).thenReturn(Optional.of(friend));
        when(cardRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CardResponse card = cardService.create("friend", new CardRequest("tune", CardType.DIAMOND));

        assertThat(card.ownerEmail()).isEqualTo("friend@gmail.com");
        assertThat(card.type()).isEqualTo(CardType.DIAMOND);
        assertThat(card.tuneId()).isEqualTo("tune");
        assertThat(card.rating()).isEqualTo(new Rating(80, 65, 90));
    }

    @Test
    void theCardKeepsItsRatingsWhenTheTuneChanges() {
        when(tuneService.findUsable("owner", "tune")).thenReturn(tune);
        when(userRepository.findById("owner")).thenReturn(Optional.of(owner));
        when(cardRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        cardService.create("owner", new CardRequest("tune", CardType.GOLD));
        tune.getRating().setMaay(10);

        ArgumentCaptor<Card> saved = ArgumentCaptor.forClass(Card.class);
        verify(cardRepository).save(saved.capture());
        assertThat(saved.getValue().getRating().getMaay()).isEqualTo(80);
    }

    @Test
    void refusesACardWithoutAType() {
        assertThatThrownBy(() -> cardService.create("owner", new CardRequest("tune", null)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(cardRepository, never()).save(any());
    }

    @Test
    void refusesATuneTheUserCannotUse() {
        when(tuneService.findUsable("friend", "tune")).thenThrow(new EntityNotFountException("Tune not found: tune"));

        assertThatThrownBy(() -> cardService.create("friend", new CardRequest("tune", CardType.SILVER)))
                .isInstanceOf(EntityNotFountException.class);
        verify(cardRepository, never()).save(any());
    }
}
