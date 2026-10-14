package com.chapeullah.guccigoblin.clan.service;

import com.chapeullah.guccigoblin.clan.repository.ClanBadgeUrlsRepository;
import com.chapeullah.guccigoblin.clan.dto.ClanBadgeUrlsResponse;
import com.chapeullah.guccigoblin.clan.model.Clan;
import com.chapeullah.guccigoblin.clan.model.ClanBadgeUrls;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClanBadgeUrlsService {

    private final ClanBadgeUrlsRepository clanBadgeUrlsRepository;

    @Transactional(readOnly = true)
    public Optional<ClanBadgeUrls> findByClanTag(@NonNull String clanTag) {
        return clanBadgeUrlsRepository.findById(clanTag);
    }

    @Transactional(readOnly = true)
    public ClanBadgeUrls getByClanTag(@NonNull String clanTag) {
        return clanBadgeUrlsRepository.findById(clanTag)
                .orElseThrow(() -> new IllegalStateException(
                        "Clan badge URLs not found: clanTag=" + clanTag));
    }

    @Transactional
    public ClanBadgeUrls syncAndGetByClan(
            @NonNull Clan clan,
            ClanBadgeUrlsResponse response) {
        ClanBadgeUrls current = clanBadgeUrlsRepository
                .findById(clan.getTag()).orElse(null);
        ClanBadgeUrls incoming = response == null ? null
                : new ClanBadgeUrls(
                        clan,
                        response.small(),
                        response.large(),
                        response.medium());
        if (Objects.equals(current, incoming)) {
            return current;
        }
        ClanBadgeUrls saved = null;
        if (incoming == null) {
            clanBadgeUrlsRepository.delete(current);
        } else {
            saved = clanBadgeUrlsRepository.save(incoming);
        }
        return saved;
    }

}
