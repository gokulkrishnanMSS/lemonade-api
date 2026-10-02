package com.lemon.lemonade.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

// A user's ratings, which they can share with other users so they can create cards from it too.
// Not @Data: its equals/hashCode would load the lazy sharedWith collection.
@Entity
@Table(name = "tunes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tune {

    @Id
    private String id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Embedded
    private Rating rating;

    @ManyToMany
    @JoinTable(name = "tune_shares",
            joinColumns = @JoinColumn(name = "tune_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    @Builder.Default
    private Set<User> sharedWith = new HashSet<>();

    public boolean isUsableBy(String userId) {
        return owner.getId().equals(userId)
                || sharedWith.stream().anyMatch(user -> user.getId().equals(userId));
    }
}
