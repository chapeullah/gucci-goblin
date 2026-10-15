package com.chapeullah.guccigoblin.warlog;

import com.chapeullah.guccigoblin.ClashOfClansClient;
import com.chapeullah.guccigoblin.warlog.dto.WarLogClanBadgeUrlsResponse;
import com.chapeullah.guccigoblin.warlog.dto.WarLogClanResponse;
import com.chapeullah.guccigoblin.warlog.dto.WarLogEntryResponse;
import com.chapeullah.guccigoblin.warlog.dto.WarLogResponse;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WarLogService {

    private static final DateTimeFormatter WAR_TIME_FORMAT = DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss.SSSX");

    private final ClashOfClansClient client;
    private final WarLogRepository warLogRepository;

    @Transactional
    public List<WarLogEntry> syncWarLog(@NonNull String clanTag) {
        WarLogResponse response = client.getWarLog(clanTag);
        if (response.items() == null) {
            throw new IllegalStateException("War log items are missing: clanTag=" + clanTag);
        }

        List<WarLogEntry> changedEntries = new ArrayList<>();

        for (WarLogEntryResponse entryResponse : response.items()) {
            WarLogEntry incoming = toEntry(entryResponse);
            WarLogEntry existing = warLogRepository
                    .findByClanTagAndEndTime(
                            incoming.getClanTag(),
                            incoming.getEndTime())
                    .orElse(null);
            if (existing == null) {
                changedEntries.add(incoming);
            } else if (existing.updateFrom(incoming)) {
                changedEntries.add(existing);
            }
        }

        return warLogRepository.saveAll(changedEntries);
    }

    private WarLogEntry toEntry(@NonNull WarLogEntryResponse response) {
        WarLogClanResponse clan = response.clan();
        WarLogClanResponse opponent = response.opponent();

        if (clan == null || clan.tag() == null || response.endTime() == null) {
            throw new IllegalStateException("War log entry is missing clan tag or end time");
        }

        WarLogClanBadgeUrlsResponse clanBadgeUrls = clan.badgeUrls();
        WarLogClanBadgeUrlsResponse opponentBadgeUrls =
                opponent == null ? null : opponent.badgeUrls();

        return new WarLogEntry(
                response.result(),
                Instant.from(WAR_TIME_FORMAT.parse(response.endTime())),
                response.teamSize(),
                response.attacksPerMember(),
                response.battleModifier(),

                clan.tag(),
                clan.name(),
                clan.clanLevel(),
                clan.attacks(),
                clan.stars(),
                clan.destructionPercentage(),
                clan.expEarned(),
                clanBadgeUrls == null ? null : clanBadgeUrls.small(),
                clanBadgeUrls == null ? null : clanBadgeUrls.medium(),
                clanBadgeUrls == null ? null : clanBadgeUrls.large(),

                opponent == null ? null : opponent.tag(),
                opponent == null ? null : opponent.name(),
                opponent == null ? null : opponent.clanLevel(),
                opponent == null ? null : opponent.stars(),
                opponent == null ? null : opponent.destructionPercentage(),
                opponentBadgeUrls == null ? null : opponentBadgeUrls.small(),
                opponentBadgeUrls == null ? null : opponentBadgeUrls.medium(),
                opponentBadgeUrls == null ? null : opponentBadgeUrls.large());
    }
}