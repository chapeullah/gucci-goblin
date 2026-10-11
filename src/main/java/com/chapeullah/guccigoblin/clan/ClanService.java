package com.chapeullah.guccigoblin.clan;

import com.chapeullah.guccigoblin.ClashOfClansClient;
import com.chapeullah.guccigoblin.capitalleague.CapitalLeague;
import com.chapeullah.guccigoblin.capitalleague.CapitalLeagueService;
import com.chapeullah.guccigoblin.clan.dto.ClanBadgeUrlsResponse;
import com.chapeullah.guccigoblin.clan.dto.ClanChatLanguageResponse;
import com.chapeullah.guccigoblin.clan.dto.ClanMemberResponse;
import com.chapeullah.guccigoblin.clan.dto.ClanResponse;
import com.chapeullah.guccigoblin.clan.model.Clan;
import com.chapeullah.guccigoblin.clan.model.ClanBadgeUrls;
import com.chapeullah.guccigoblin.clan.model.ClanChatLanguage;
import com.chapeullah.guccigoblin.leaguetier.LeagueTier;
import com.chapeullah.guccigoblin.leaguetier.LeagueTierService;
import com.chapeullah.guccigoblin.location.Location;
import com.chapeullah.guccigoblin.location.LocationService;
import com.chapeullah.guccigoblin.member.MemberService;
import com.chapeullah.guccigoblin.warleague.WarLeague;
import com.chapeullah.guccigoblin.warleague.WarLeagueService;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClanService {

    private final ClashOfClansClient client;

    private final ClanRepository clanRepository;

    private final ClanChatLanguageRepository clanChatLanguageRepository;

    private final LocationService locationService;
    private final CapitalLeagueService capitalLeagueService;
    private final WarLeagueService warLeagueService;
    private final LeagueTierService leagueTierService;

    private final MemberService memberService;

    @Transactional
    public Clan syncClan(@NonNull String clanTag) {
        ClanResponse clanResponse = client.getClan(clanTag);
        if (clanResponse.memberList() == null) {
            throw new IllegalStateException("Clan member list is missing: clanTag=\"" + clanTag + "\"");
        }
        
        ClanBadgeUrlsResponse badgeResponse = clanResponse.badgeUrls();
        ClanBadgeUrls badgeUrls = badgeResponse == null ? null : new ClanBadgeUrls(
                badgeResponse.small(),
                badgeResponse.large(),
                badgeResponse.medium());

        syncLocationsIfMissing(clanResponse);
        Location location = clanResponse.location() == null ? null
                : locationService.getById(clanResponse.location().id());

        syncCapitalLeaguesIfMissing(clanResponse);
        CapitalLeague capitalLeague = clanResponse.capitalLeague() == null ? null
                : capitalLeagueService.getById(clanResponse.capitalLeague().id());

        syncWarLeaguesIfMissing(clanResponse);
        WarLeague warLeague = clanResponse.warLeague() == null ? null
                : warLeagueService.getById(clanResponse.warLeague().id());

        syncLeagueTiersIfMissing(clanResponse);
        LeagueTier requiredLeagueTier = clanResponse.requiredLeagueTier() == null ? null
                : leagueTierService.getById(clanResponse.requiredLeagueTier().id());

        ClanChatLanguageResponse languageResponse = clanResponse.chatLanguage();
        ClanChatLanguage chatLanguage = languageResponse == null ? null
                : clanChatLanguageRepository.save(new ClanChatLanguage(
                        languageResponse.id(),
                        languageResponse.name(),
                        languageResponse.languageCode()));
        Clan clan = new Clan(
                clanResponse.tag(),
                clanResponse.name(),
                clanResponse.type(),
                clanResponse.description(),
                location,
                clanResponse.isFamilyFriendly(),
                badgeUrls,
                clanResponse.clanLevel(),
                clanResponse.clanPoints(),
                clanResponse.clanBuilderBasePoints(),
                clanResponse.clanCapitalPoints(),
                capitalLeague,
                clanResponse.requiredTrophies(),
                clanResponse.warFrequency(),
                clanResponse.warWinStreak(),
                clanResponse.warWins(),
                clanResponse.warTies(),
                clanResponse.warLosses(),
                clanResponse.isWarLogPublic(),
                warLeague,
                clanResponse.members(),
                clanResponse.requiredBuilderBaseTrophies(),
                clanResponse.requiredTownhallLevel(),
                requiredLeagueTier,
                chatLanguage);
        Clan existingClan = clanRepository.findById(clan.getTag()).orElse(null);
        Clan savedClan;
        if (existingClan == null) {
            savedClan = clanRepository.save(clan);
        } else {
            existingClan.updateFrom(clan);
            savedClan = existingClan;
        }
        memberService.syncMembers(savedClan, clanResponse.memberList());
        return savedClan;
    }

    private void syncLocationsIfMissing(
            @NonNull ClanResponse response) {
        if (response.location() == null) {
            log.debug("Clan location is missing: clanTag=\"{}\"", response.tag());
            return;
        }
        Integer locationId = response.location().id();
        if (locationService.findById(locationId).isEmpty()) {
            log.info("Location is missing locally; synchronizing locations: locationId={}", locationId);
            locationService.syncAll();
        }
    }

    private void syncCapitalLeaguesIfMissing(
            @NonNull ClanResponse response) {
        if (response.capitalLeague() == null) {
            log.debug("Clan capital league is missing in response: clanTag=\"{}\"", response.tag());
            return;
        }
        Integer capitalLeagueId = response.capitalLeague().id();
        if (capitalLeagueService.findById(capitalLeagueId).isEmpty()) {
            log.info("Capital league is missing locally; synchronizing capital leagues: capitalLeagueId={}",
                    capitalLeagueId);
            capitalLeagueService.syncAll();
        }
    }

    private void syncWarLeaguesIfMissing(
            @NonNull ClanResponse response) {
        if (response.warLeague() == null) {
            log.debug("Clan war league is missing: clanTag=\"{}\"", response.tag());
            return;
        }
        Integer warLeagueId = response.warLeague().id();
        if (warLeagueService.findById(warLeagueId).isEmpty()) {
            log.info("War league is missing locally; synchronizing war leagues: warLeagueId={}", warLeagueId);
            warLeagueService.syncAll();
        }
    }

    private void syncLeagueTiersIfMissing(
            @NonNull ClanResponse response) {
        if (response.requiredLeagueTier() == null) {
            log.debug("Clan required league tier is missing: clanTag=\"{}\"", response.tag());
            return;
        }
        Integer leagueTierId = response.requiredLeagueTier().id();
        if (leagueTierService.findById(leagueTierId).isEmpty()) {
            log.info("League tier is missing locally; synchronizing league tiers: leagueTierId={}", leagueTierId);
            leagueTierService.syncAll();
        }
    }

}
