package com.chapeullah.guccigoblin.war.repository;

import com.chapeullah.guccigoblin.war.model.WarClanBadgeUrls;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WarClanBadgeUrlsRepository extends JpaRepository<WarClanBadgeUrls, Long> {}
