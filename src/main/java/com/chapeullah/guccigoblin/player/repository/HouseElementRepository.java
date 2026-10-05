package com.chapeullah.guccigoblin.player.repository;

import com.chapeullah.guccigoblin.player.model.HouseElement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HouseElementRepository extends JpaRepository<HouseElement, Long> {

    List<HouseElement> findAllByPlayerTag(String playerTag);

}
