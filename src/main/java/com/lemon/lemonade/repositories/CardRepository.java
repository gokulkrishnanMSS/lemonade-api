package com.lemon.lemonade.repositories;

import com.lemon.lemonade.models.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, String> {

    List<Card> findByOwnerId(String ownerId);
}
