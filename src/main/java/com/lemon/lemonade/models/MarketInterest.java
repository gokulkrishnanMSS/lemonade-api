package com.lemon.lemonade.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "market_interests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketInterest {

    @Id
    private String id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "market_id", nullable = false)
    private Market market;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "bit_value", nullable = false)
    private BigDecimal bitValue;

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
    }

    public String getUserId() {
        return user != null ? user.getId() : null;
    }

    public void setUserId(String userId) {
        if (userId != null) {
            this.user = User.builder().id(userId).build();
        } else {
            this.user = null;
        }
    }

    public BigDecimal getBidValue() {
        return bitValue;
    }

    public void setBidValue(BigDecimal bidValue) {
        this.bitValue = bidValue;
    }

    public static class MarketInterestBuilder {
        public MarketInterestBuilder userId(String userId) {
            if (userId != null) {
                this.user = User.builder().id(userId).build();
            }
            return this;
        }

        public MarketInterestBuilder bidValue(BigDecimal bidValue) {
            this.bitValue = bidValue;
            return this;
        }
    }
}
