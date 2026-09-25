package com.chapeullah.guccigoblin.war.repository;

import com.chapeullah.guccigoblin.war.model.War;
import com.chapeullah.guccigoblin.war.model.WarParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WarParticipantRepository extends JpaRepository<WarParticipant, Long> {

    boolean existsByWarAndPlayerTag(War war, String playerTag);

}
