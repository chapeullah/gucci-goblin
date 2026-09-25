package com.chapeullah.guccigoblin.war.repository;

import com.chapeullah.guccigoblin.war.model.War;
import com.chapeullah.guccigoblin.war.model.WarAttack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WarAttackRepository
        extends JpaRepository<WarAttack, Long> {

    boolean existsByWarAndAttackOrder(War war, Integer attackOrder);
}
