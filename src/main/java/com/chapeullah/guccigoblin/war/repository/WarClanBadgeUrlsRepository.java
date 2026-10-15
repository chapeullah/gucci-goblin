package com.chapeullah.guccigoblin.war.repository;

import com.chapeullah.guccigoblin.war.model.War;
import com.chapeullah.guccigoblin.war.model.WarClanBadgeUrls;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WarClanBadgeUrlsRepository extends JpaRepository<WarClanBadgeUrls, Long> {

    Optional<WarClanBadgeUrls> findByWar(War war);

}
