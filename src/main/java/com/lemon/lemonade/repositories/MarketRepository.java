package com.lemon.lemonade.repositories;

import com.lemon.lemonade.models.Market;
import com.lemon.lemonade.models.MarketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketRepository extends JpaRepository<Market, String> {

    List<Market> findBySellerId(String sellerId);

    List<Market> findByCardId(String cardId);

    List<Market> findByStatus(MarketStatus status);
}
