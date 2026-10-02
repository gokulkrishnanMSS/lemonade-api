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
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TuneServiceTest {

    private final User owner = User.builder().id("owner").email("owner@gmail.com").build();
    private final User friend = User.builder().id("friend").email("friend@gmail.com").build();
    private final User stranger = User.builder().id("stranger").email("stranger@gmail.com").build();

    private final TuneRepository tuneRepository = mock(TuneRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final TuneService tuneService = new TuneService(tuneRepository, userRepository);

    private Tune tune() {
        return Tune.builder().id("tune").name("Morning").owner(owner).rating(new Rating(80, 65, 90)).build();
    }

    @Test
    void createsATuneOwnedByTheUser() {
        when(userRepository.findById("owner")).thenReturn(Optional.of(owner));
        when(tuneRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        TuneResponse created = tuneService.create("owner", new TuneRequest("Morning", new Rating(80, 65, 90)));

        assertThat(created.ownerEmail()).isEqualTo("owner@gmail.com");
        assertThat(created.rating()).isEqualTo(new Rating(80, 65, 90));
        assertThat(created.sharedWith()).isEmpty();
    }

    @Test
    void refusesRatingsOutsideTheRange() {
        assertThatThrownBy(() -> tuneService.create("owner", new TuneRequest("Morning", new Rating(80, 101, 90))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maana");
        assertThatThrownBy(() -> tuneService.create("owner", new TuneRequest("Morning", new Rating(80, 65, null))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("will");
        verify(tuneRepository, never()).save(any());
    }

    @Test
    void theOwnerCanShareATuneSoTheFriendCanUseIt() {
        Tune tune = tune();
        when(tuneRepository.findById("tune")).thenReturn(Optional.of(tune));
        when(userRepository.findByEmail("friend@gmail.com")).thenReturn(Optional.of(friend));

        TuneResponse shared = tuneService.share("owner", "tune", "friend@gmail.com");

        assertThat(shared.sharedWith()).containsExactly("friend@gmail.com");
        assertThat(tuneService.findUsable("friend", "tune")).isSameAs(tune);
    }

    @Test
    void onlyTheOwnerCanShare() {
        Tune tune = tune();
        tune.getSharedWith().add(friend);
        when(tuneRepository.findById("tune")).thenReturn(Optional.of(tune));

        assertThatThrownBy(() -> tuneService.share("friend", "tune", "stranger@gmail.com"))
                .isInstanceOf(ForbiddenException.class);
        assertThat(tune.getSharedWith()).containsExactly(friend);
    }

    @Test
    void aTuneThatWasNotSharedLooksMissing() {
        when(tuneRepository.findById("tune")).thenReturn(Optional.of(tune()));

        assertThatThrownBy(() -> tuneService.findUsable(stranger.getId(), "tune"))
                .isInstanceOf(EntityNotFountException.class);
    }
}
