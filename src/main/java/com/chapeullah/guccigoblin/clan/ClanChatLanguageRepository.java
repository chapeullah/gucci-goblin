package com.chapeullah.guccigoblin.clan;

import com.chapeullah.guccigoblin.clan.model.ClanChatLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClanChatLanguageRepository extends JpaRepository<ClanChatLanguage, Integer> {}
