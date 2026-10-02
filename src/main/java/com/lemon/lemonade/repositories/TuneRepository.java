package com.lemon.lemonade.repositories;

import com.lemon.lemonade.models.Tune;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TuneRepository extends JpaRepository<Tune, String> {

    // Tunes the user owns plus those shared with them
    @Query("select distinct t from Tune t left join t.sharedWith s where t.owner.id = :userId or s.id = :userId")
    List<Tune> findUsableBy(@Param("userId") String userId);
}
