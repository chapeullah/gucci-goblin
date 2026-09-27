package com.chapeullah.guccigoblin.war.repository;

import com.chapeullah.guccigoblin.war.model.War;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface WarRepository extends JpaRepository<War, Long> {

    Optional<War> findByClanTagAndOpponentTagAndStartsAt(
            String clanTag,
            String opponentTag,
            Instant startsAt);

    List<War> findAllByEndsAtLessThanEqualAndStateNot(
            Instant now,
            String state);

    Optional<War> findFirstByStateInOrderByStartsAtDesc(
            List<String> states);

    List<War> findTop5ByStateOrderByEndsAtDesc(
            String state);

}
