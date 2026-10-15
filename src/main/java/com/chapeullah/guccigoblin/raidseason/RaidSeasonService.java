package com.chapeullah.guccigoblin.raidseason;

import com.chapeullah.guccigoblin.ClashOfClansClient;
import com.chapeullah.guccigoblin.clan.model.Clan;
import com.chapeullah.guccigoblin.clan.repository.ClanRepository;
import com.chapeullah.guccigoblin.clan.service.ClanService;
import com.chapeullah.guccigoblin.raidseason.dto.RaidSeasonResponse;
import com.chapeullah.guccigoblin.raidseason.dto.RaidSeasonsResponse;
import com.chapeullah.guccigoblin.raidseason.model.RaidSeason;
import com.chapeullah.guccigoblin.raidseason.repository.RaidSeasonRepository;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@DependsOn("environmentVariablesValidator")
public class RaidSeasonService {

    private static final DateTimeFormatter RAID_TIME_FORMAT =
            DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss.SSSX");

    private final ClashOfClansClient client;
    private final RaidSeasonRepository raidSeasonRepository;

    private final ClanService clanService;

    @Value("${coc.clan-tag}")
    private String clanTag;

    @Transactional
    public Optional<RaidSeason> syncRaidSeason(@NonNull String clanTag) {
        RaidSeasonsResponse response = client.getCurrentRaid(clanTag);
        if (response == null) {
            throw new IllegalStateException("Raid seasons response is null: clanTag=\"" + clanTag + "\"");
        }
        if (response.items() == null) {
            throw new IllegalStateException("Raid season response items is null: clanTag=\"" + clanTag + "\"");
        }
        if (response.items().isEmpty()) {
            return Optional.empty();
        }
        if (response.items().size() != 1) {
            throw new IllegalStateException("Expected exactly one raid season: clanTag=\"" + clanTag
                    + "\", actualCount=" + response.items().size());
        }

        RaidSeasonResponse raidSeasonResponse = response.items().getFirst();
        RaidSeason raidSeason = raidSeasonRepository
                .findByClanTagAndStartTime(
                        clanTag,
                        Instant.from(RAID_TIME_FORMAT.parse(raidSeasonResponse.startTime())))
                .orElse(null);
        if (raidSeason == null) {
            raidSeason = createRaidSeason(clanService.getClanByTag(clanTag), raidSeasonResponse);
        } else {
            updateRaidSeason(raidSeason, raidSeasonResponse);
        }

        return Optional.of(raidSeason);
    }

    private RaidSeason createRaidSeason(@NonNull Clan clan, @NonNull RaidSeasonResponse response) {
        RaidSeason raidSeason = new RaidSeason(
                clan,
                response.state(),
                Instant.from(RAID_TIME_FORMAT.parse(response.startTime())),
                Instant.from(RAID_TIME_FORMAT.parse(response.endTime())),
                response.capitalTotalLoot(),
                response.raidsCompleted(),
                response.totalAttacks(),
                response.enemyDistrictsDestroyed(),
                response.offensiveReward(),
                response.defensiveReward());
        return null;
    }

    private void updateRaidSeason(RaidSeason raidSeason, RaidSeasonResponse response) {

    }

}
