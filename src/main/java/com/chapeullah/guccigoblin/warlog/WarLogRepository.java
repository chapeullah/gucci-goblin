package com.chapeullah.guccigoblin.warlog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface WarLogRepository extends JpaRepository<WarLogEntry, Long> {

    Optional<WarLogEntry> findByClanTagAndEndTime(String clanTag, Instant endTime);
}