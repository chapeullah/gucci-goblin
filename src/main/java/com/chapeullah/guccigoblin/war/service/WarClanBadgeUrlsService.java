package com.chapeullah.guccigoblin.war.service;

import com.chapeullah.guccigoblin.war.dto.WarClanBadgeUrlsResponse;
import com.chapeullah.guccigoblin.war.model.War;
import com.chapeullah.guccigoblin.war.model.WarClanBadgeUrls;
import com.chapeullah.guccigoblin.war.repository.WarClanBadgeUrlsRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class WarClanBadgeUrlsService {

    private final WarClanBadgeUrlsRepository warClanBadgeUrlsRepository;

    @Transactional
    public WarClanBadgeUrls syncAndGetByWar(
            @NonNull War war,
            WarClanBadgeUrlsResponse response) {
        WarClanBadgeUrls current = warClanBadgeUrlsRepository
                .findByWar(war).orElse(null);
        WarClanBadgeUrls incoming = response == null ? null
                : new WarClanBadgeUrls(
                        war,
                        response.small(),
                        response.large(),
                        response.medium());
        if (Objects.equals(current, incoming)) {
            return current;
        }
        if (incoming == null) {
            warClanBadgeUrlsRepository.delete(current);
            return null;
        }
        if (current == null) {
            return warClanBadgeUrlsRepository.save(incoming);
        }
        current.update(response.small(), response.large(), response.medium());
        return current;
    }

}
