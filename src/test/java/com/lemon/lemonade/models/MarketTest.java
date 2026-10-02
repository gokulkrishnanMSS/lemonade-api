package com.lemon.lemonade.models;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MarketTest {

    @Test
    void canBuildMarketWithAllProperties() {
        User seller = User.builder().id("seller-1").email("seller@test.com").build();
        Card card = Card.builder().id("card-100").type(CardType.GOLD).owner(seller).build();
        BigDecimal minBit = new BigDecimal("50.00");

        Market market = Market.builder()
                .id(UUID.randomUUID().toString())
                .seller(seller)
                .card(card)
                .minBit(minBit)
                .build();

        market.ensureDefaults();

        assertThat(market.getSeller()).isEqualTo(seller);
        assertThat(market.getUser()).isEqualTo(seller);
        assertThat(market.getSellerId()).isEqualTo("seller-1");
        assertThat(market.getCard()).isEqualTo(card);
        assertThat(market.getCardId()).isEqualTo("card-100");
        assertThat(market.getMinBit()).isEqualTo(minBit);
        assertThat(market.getMinBid()).isEqualTo(minBit);
        assertThat(market.getStatus()).isEqualTo(MarketStatus.ACTIVE);
        assertThat(market.getCreatedAt()).isNotNull();
        assertThat(market.getInterests()).isEmpty();
    }

    @Test
    void canAddInterestsWithBitValues() {
        User seller = User.builder().id("seller-1").email("seller@test.com").build();
        User bidder1 = User.builder().id("bidder-1").email("bidder1@test.com").build();
        User bidder2 = User.builder().id("bidder-2").email("bidder2@test.com").build();
        Card card = Card.builder().id("card-100").type(CardType.DIAMOND).owner(seller).build();

        Market market = Market.builder()
                .seller(seller)
                .card(card)
                .minBit(new BigDecimal("100.00"))
                .build();

        market.ensureDefaults();

        MarketInterest interest1 = market.addInterest(bidder1, new BigDecimal("120.00"));
        interest1.ensureDefaults();

        MarketInterest interest2 = MarketInterest.builder()
                .user(bidder2)
                .bitValue(new BigDecimal("150.00"))
                .build();
        interest2.ensureDefaults();
        market.addInterest(interest2);

        assertThat(market.getInterests()).hasSize(2);
        assertThat(market.getUserInterests()).hasSize(2);
        assertThat(market.getBids()).hasSize(2);

        MarketInterest first = market.getInterests().get(0);
        assertThat(first.getUser()).isEqualTo(bidder1);
        assertThat(first.getUserId()).isEqualTo("bidder-1");
        assertThat(first.getBitValue()).isEqualByComparingTo("120.00");
        assertThat(first.getBidValue()).isEqualByComparingTo("120.00");
        assertThat(first.getMarket()).isEqualTo(market);
        assertThat(first.getCreatedAt()).isNotNull();

        MarketInterest second = market.getInterests().get(1);
        assertThat(second.getUser()).isEqualTo(bidder2);
        assertThat(second.getUserId()).isEqualTo("bidder-2");
        assertThat(second.getBitValue()).isEqualByComparingTo("150.00");
        assertThat(second.getMarket()).isEqualTo(market);

        market.removeInterest(first);
        assertThat(market.getInterests()).hasSize(1);
        assertThat(first.getMarket()).isNull();
    }

    @Test
    void supportsBuilderConvenienceAliases() {
        User seller = User.builder().id("seller-1").build();
        Card card = Card.builder().id("card-1").build();

        Market market = Market.builder()
                .user(seller)
                .card(card)
                .minBid(new BigDecimal("25.50"))
                .build();

        assertThat(market.getUser()).isEqualTo(seller);
        assertThat(market.getSeller()).isEqualTo(seller);
        assertThat(market.getMinBit()).isEqualTo(new BigDecimal("25.50"));
        assertThat(market.getMinBid()).isEqualTo(new BigDecimal("25.50"));

        MarketInterest interest = MarketInterest.builder()
                .userId("user-abc")
                .bidValue(new BigDecimal("30.00"))
                .build();

        assertThat(interest.getUserId()).isEqualTo("user-abc");
        assertThat(interest.getBidValue()).isEqualTo(new BigDecimal("30.00"));
        assertThat(interest.getBitValue()).isEqualTo(new BigDecimal("30.00"));
    }
}
