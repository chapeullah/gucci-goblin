package com.chapeullah.guccigoblin.war.service;

import com.chapeullah.guccigoblin.ClashOfClansClient;
import com.chapeullah.guccigoblin.war.dto.WarClanBadgeUrlsResponse;
import com.chapeullah.guccigoblin.war.dto.WarResponse;
import com.chapeullah.guccigoblin.war.model.War;
import com.chapeullah.guccigoblin.war.model.WarClanBadgeUrls;
import com.chapeullah.guccigoblin.war.repository.WarRepository;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarService {

    private static final DateTimeFormatter WAR_TIME_FORMAT = DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss.SSSX");

    private final ClashOfClansClient client;

    private final WarRepository warRepository;
    private final Clock clock;

    private final WarParticipantService warParticipantService;
    private final WarAttackService warAttackService;

    @Transactional
    public Optional<War> syncCurrentWar(@NonNull String clanTag) {
        WarResponse response = client.getCurrentWar(clanTag);
        if (response == null) {
            throw new IllegalStateException("Empty current war response: clanTag=" + clanTag);
        }
        if ("notInWar".equals(response.state())) {
            return Optional.empty();
        }

        War war = warRepository
                .findByClanTagAndOpponentClanTagAndStartTime(
                        response.clan().tag(),
                        response.opponent().tag(),
                        Instant.from(WAR_TIME_FORMAT.parse(response.startTime())))
                .orElse(null);

        if (war == null) {
            war = createWar(response);
        } else {
            updateWar(war, response);
        }

        war.setWarClanBadgeUrls(toWarClanBadgeUrls(response.clan().badgeUrls()));

        if (!clock.instant().isBefore(war.getEndTime())) {
            war.finish();
        }

        war = warRepository.save(war);

        warParticipantService.syncWarParticipants(war, response.clan(), response.opponent());

        warAttackService.syncAttacks(war, response.clan(), response.opponent());

        return war;
    }

    @Transactional
    public List<War> finishEndedWars() {
        return warRepository.saveAll(warRepository
                .findAllByEndTimeLessThanEqualAndStateNot(
                        clock.instant(),
                        "warEnded")
                .stream()
                .map(War::finish)
                .toList());
    }

    private War createWar(@NonNull WarResponse response) {
        return new War(
                response.clan().tag(),
                response.clan().name(),
                response.clan().attacks(),
                response.clan().stars(),
                response.clan().destructionPercentage(),
                response.clan().clanLevel(),

                response.opponent().tag(),
                response.opponent().name(),
                response.opponent().attacks(),
                response.opponent().stars(),
                response.opponent().destructionPercentage(),
                response.opponent().clanLevel(),

                response.state(),
                response.teamSize(),
                response.attacksPerMember(),
                response.battleModifier(),
                Instant.from(WAR_TIME_FORMAT.parse(response.preparationStartTime())),
                Instant.from(WAR_TIME_FORMAT.parse(response.startTime())),
                Instant.from(WAR_TIME_FORMAT.parse(response.endTime())));
    }

    private void updateWar(@NonNull War war, @NonNull WarResponse response) {
        war.update(
                response.clan().attacks(),
                response.clan().stars(),
                response.clan().destructionPercentage(),
                response.clan().clanLevel(),
                response.opponent().attacks(),
                response.opponent().stars(),
                response.opponent().destructionPercentage(),
                response.opponent().clanLevel(),
                response.state());
    }

    private WarClanBadgeUrls toWarClanBadgeUrls(WarClanBadgeUrlsResponse response) {
        return response == null ? null
                : WarClanBadgeUrls.of(response.small(), response.large(), response.medium());
    }
}
