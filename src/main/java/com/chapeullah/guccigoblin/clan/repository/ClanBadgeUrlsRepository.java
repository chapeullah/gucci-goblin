package com.chapeullah.guccigoblin.clan.repository;

import com.chapeullah.guccigoblin.clan.model.ClanBadgeUrls;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClanBadgeUrlsRepository extends JpaRepository<ClanBadgeUrls, String> {}
