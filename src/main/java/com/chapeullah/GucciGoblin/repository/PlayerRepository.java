package com.chapeullah.GucciGoblin.repository;

import com.chapeullah.GucciGoblin.entity.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends JpaRepository<PlayerEntity, String> {}
