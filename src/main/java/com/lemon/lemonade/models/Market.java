package com.lemon.lemonade.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "markets")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Market {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @Column(name = "min_bit", nullable = false)
    private BigDecimal minBit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MarketStatus status = MarketStatus.ACTIVE;

    @OneToMany(mappedBy = "market", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MarketInterest> interests = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @PrePersist
    public void ensureDefaults() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.status == null) {
            this.status = MarketStatus.ACTIVE;
        }
        if (this.interests == null) {
            this.interests = new ArrayList<>();
        }
    }

    public String getCardId() {
        return card != null ? card.getId() : null;
    }

    public void setCardId(String cardId) {
        if (cardId != null) {
            this.card = Card.builder().id(cardId).build();
        } else {
            this.card = null;
        }
    }

    public String getSellerId() {
        return seller != null ? seller.getId() : null;
    }

    public void setSellerId(String sellerId) {
        if (sellerId != null) {
            this.seller = User.builder().id(sellerId).build();
        } else {
            this.seller = null;
        }
    }

    public User getUser() {
        return seller;
    }

    public void setUser(User user) {
        this.seller = user;
    }

    public BigDecimal getMinBid() {
        return minBit;
    }

    public void setMinBid(BigDecimal minBid) {
        this.minBit = minBid;
    }

    public List<MarketInterest> getUserInterests() {
        return interests;
    }

    public void setUserInterests(List<MarketInterest> userInterests) {
        this.interests = userInterests;
    }

    public List<MarketInterest> getBids() {
        return interests;
    }

    public void setBids(List<MarketInterest> bids) {
        this.interests = bids;
    }

    public void addInterest(MarketInterest interest) {
        if (this.interests == null) {
            this.interests = new ArrayList<>();
        }
        this.interests.add(interest);
        interest.setMarket(this);
    }

    public MarketInterest addInterest(User user, BigDecimal bitValue) {
        MarketInterest interest = MarketInterest.builder()
                .id(UUID.randomUUID().toString())
                .market(this)
                .user(user)
                .bitValue(bitValue)
                .build();
        addInterest(interest);
        return interest;
    }

    public void removeInterest(MarketInterest interest) {
        if (this.interests != null) {
            this.interests.remove(interest);
            interest.setMarket(null);
        }
    }

    public static class MarketBuilder {
        public MarketBuilder cardId(String cardId) {
            if (cardId != null) {
                this.card = Card.builder().id(cardId).build();
            }
            return this;
        }

        public MarketBuilder sellerId(String sellerId) {
            if (sellerId != null) {
                this.seller = User.builder().id(sellerId).build();
            }
            return this;
        }

        public MarketBuilder user(User user) {
            this.seller = user;
            return this;
        }

        public MarketBuilder minBid(BigDecimal minBid) {
            this.minBit = minBid;
            return this;
        }
    }
}
