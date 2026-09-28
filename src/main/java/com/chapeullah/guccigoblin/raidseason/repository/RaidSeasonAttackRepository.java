package com.chapeullah.guccigoblin.raidseason.repository;

import com.chapeullah.guccigoblin.raidseason.model.RaidSeason;
import com.chapeullah.guccigoblin.raidseason.model.RaidSeasonAttack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RaidSeasonAttackRepository
        extends JpaRepository<RaidSeasonAttack, Long> {

    void deleteAllByParticipant_RaidSeason(RaidSeason raidSeason);
}