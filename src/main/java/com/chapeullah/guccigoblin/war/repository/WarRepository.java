package com.chapeullah.guccigoblin.war.repository;

import com.chapeullah.guccigoblin.war.model.War;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface WarRepository extends JpaRepository<War, Long> {

    Optional<War> findByClanTagAndOpponentTagAndStartTime(
            String clanTag,
            String opponentTag,
            Instant startTime);

    List<War> findAllByEndTimeLessThanEqualAndStateNot(
            Instant now,
            String state);

    Optional<War> findFirstByStateInOrderByStartTimeDesc(
            List<String> states);

    List<War> findTop5ByStateOrderByEndTimeDesc(
            String state);

}
