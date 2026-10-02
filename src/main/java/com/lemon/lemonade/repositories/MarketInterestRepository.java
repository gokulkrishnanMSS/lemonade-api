package com.lemon.lemonade.repositories;

import com.lemon.lemonade.models.MarketInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketInterestRepository extends JpaRepository<MarketInterest, String> {

    List<MarketInterest> findByMarketId(String marketId);

    List<MarketInterest> findByUserId(String userId);
}
