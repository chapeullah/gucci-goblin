package com.chapeullah.guccigoblin.player.repository;

import com.chapeullah.guccigoblin.player.model.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {

    List<Achievement> findAllByPlayerTag(String playerTag);

}
