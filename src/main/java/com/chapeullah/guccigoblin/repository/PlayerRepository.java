package com.chapeullah.guccigoblin.repository;

import com.chapeullah.guccigoblin.player.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends JpaRepository<Player, String> {}
