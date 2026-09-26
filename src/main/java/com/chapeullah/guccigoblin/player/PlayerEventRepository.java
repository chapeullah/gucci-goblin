package com.chapeullah.guccigoblin.player;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerEventRepository extends JpaRepository<PlayerEvent, Long> {}
