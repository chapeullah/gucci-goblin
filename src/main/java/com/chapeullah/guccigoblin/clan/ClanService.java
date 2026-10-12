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
import com.chapeullah.guccigoblin.label.clan.ClanLabelService;
import com.chapeullah.guccigoblin.label.dto.LabelResponse;
import com.chapeullah.guccigoblin.leaguetier.LeagueTier;
import com.chapeullah.guccigoblin.leaguetier.LeagueTierService;
import com.chapeullah.guccigoblin.location.Location;
import com.chapeullah.guccigoblin.location.LocationService;
import com.chapeullah.guccigoblin.member.MemberService;
import com.chapeullah.guccigoblin.member.model.Member;
import com.chapeullah.guccigoblin.warleague.WarLeague;
import com.chapeullah.guccigoblin.warleague.WarLeagueService;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    private final ClanLabelService clanLabelService;

    private final MemberService memberService;

    @Transactional
    public Clan syncClan(@NonNull String clanTag) {
        ClanResponse response = client.getClan(clanTag);
        if (response.memberList() == null) {
            throw new IllegalStateException("Clan member list is missing: clanTag=\"" + clanTag + "\"");
        }

        Clan clan = clanRepository.findById(response.tag()).orElse(null);
        if (clan == null) {
            createClan(response);
        } else {
            updateClan(clan, response);
        }
        clan = clanRepository.findById(response.tag()).orElseThrow(() ->
                new IllegalStateException("Clan not found after creation or update: clanTag\"" +
                        response.tag() + "\""));

//        Clan savedClan;
//        if (existingClan == null) {
//            savedClan = clanRepository.save(clan);
//        } else {
//            existingClan.updateFrom(clan);
//            savedClan = existingClan;
//        }
//        memberService.syncMembers(savedClan, response.memberList());
//        return savedClan;
        return clan;
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

    private void syncClanLabelsIfMissing(@NonNull ClanResponse clanResponse) {
        for (LabelResponse labelResponse : clanResponse.labels()) {
            if (labelResponse == null) {
                log.debug("Clan label is missing: clanTag=\"{}\"", clanResponse.tag());
                continue;
            }
            Integer clanLabelId = labelResponse.id();
            if (clanLabelService.findById(clanLabelId).isEmpty()) {
                log.info("Clan label is missing locally; synchronizing clan labels: clanLabelId={}", clanLabelId);
                clanLabelService.syncAll();
            }
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

    public void createClan(@NonNull ClanResponse response) {
        ClanBadgeUrlsResponse badgeResponse = response.badgeUrls();
        ClanBadgeUrls badgeUrls = badgeResponse == null ? null : new ClanBadgeUrls(
                badgeResponse.small(),
                badgeResponse.large(),
                badgeResponse.medium());

        syncLocationsIfMissing(response);
        Location location = response.location() == null ? null
                : locationService.getById(response.location().id());

        syncCapitalLeaguesIfMissing(response);
        CapitalLeague capitalLeague = response.capitalLeague() == null ? null
                : capitalLeagueService.getById(response.capitalLeague().id());

        syncWarLeaguesIfMissing(response);
        WarLeague warLeague = response.warLeague() == null ? null
                : warLeagueService.getById(response.warLeague().id());

        syncClanLabelsIfMissing(response);


        syncLeagueTiersIfMissing(response);
        LeagueTier requiredLeagueTier = response.requiredLeagueTier() == null ? null
                : leagueTierService.getById(response.requiredLeagueTier().id());

        ClanChatLanguageResponse languageResponse = response.chatLanguage();
        ClanChatLanguage chatLanguage = languageResponse == null ? null
                : clanChatLanguageRepository.save(new ClanChatLanguage(
                languageResponse.id(),
                languageResponse.name(),
                languageResponse.languageCode()));

        Clan clan = new Clan(
                response.tag(),
                response.name(),
                response.type(),
                response.description(),
                location,
                response.isFamilyFriendly(),
                badgeUrls,
                response.clanLevel(),
                response.clanPoints(),
                response.clanBuilderBasePoints(),
                response.clanCapitalPoints(),
                capitalLeague,
                response.requiredTrophies(),
                response.warFrequency(),
                response.warWinStreak(),
                response.warWins(),
                response.warTies(),
                response.warLosses(),
                response.isWarLogPublic(),
                warLeague,
                response.memberCount(),
                response.requiredBuilderBaseTrophies(),
                response.requiredTownHallLevel(),
                requiredLeagueTier,
                chatLanguage);
        clanRepository.save(clan);
    }

    public void updateClan(@NonNull Clan clan, @NonNull ClanResponse response) {
        if (!clan.getTag().equals(response.tag())) {
            throw new IllegalArgumentException("Clan tag mismatch");
        }

        Instant now = Instant.now();

        if (!Objects.equals(clan.getType(), response.type())) {
            clan.setType(response.type());
            clan.setTypeChangedAt(now);
        }

        if (!Objects.equals(clan.getDescription(), response.description())) {
            clan.setDescription(response.description());
            clan.setDescriptionChangedAt(now);
        }

        Location location = locationService.getById(response.location().id());
        if (!Objects.equals(clan.getLocation(), location)) {
            clan.setLocation(location);
            clan.setLocationChangedAt(now);
        }

        ClanBadgeUrls clanBadgeUrls = toClanBadgeUrls(response.badgeUrls());
        if (!Objects.equals(clan.getClanBadgeUrls(), clanBadgeUrls)) {
            clan.setClanBadgeUrls(clanBadgeUrls);
            clan.setClanBadgeUrlsChangedAt(now);
        }

        if (!Objects.equals(clan.getClanLevel(), response.clanLevel())) {
            clan.setClanLevel(response.clanLevel());
            clan.setClanLevelChangedAt(now);
        }

        if (!Objects.equals(clan.getClanPoints(), response.clanPoints())) {
            clan.setClanPoints(response.clanPoints());
            clan.setClanPointsChangedAt(now);
        }

        if (!Objects.equals(clan.getClanBuilderBasePoints(), response.clanBuilderBasePoints())) {
            clan.setClanBuilderBasePoints(response.clanBuilderBasePoints());
            clan.setClanBuilderBasePointsChangedAt(now);
        }

        if (!Objects.equals(clan.getClanCapitalPoints(), response.clanCapitalPoints())) {
            clan.setClanCapitalPoints(response.clanCapitalPoints());
            clan.setClanCapitalPointsChangedAt(now);
        }

        CapitalLeague capitalLeague = capitalLeagueService.getById(response.capitalLeague().id());
        if (!Objects.equals(clan.getCapitalLeague(), capitalLeague)) {
            clan.setCapitalLeague(capitalLeague);
            clan.setCapitalLeagueChangedAt(now);
        }

        if (!Objects.equals(clan.getRequiredTrophies(), response.requiredTrophies())) {
            clan.setRequiredTrophies(response.requiredTrophies());
            clan.setRequiredTrophiesChangedAt(now);
        }

        if (!Objects.equals(clan.getWarFrequency(), response.warFrequency())) {
            clan.setWarFrequency(response.warFrequency());
            clan.setWarFrequencyChangedAt(now);
        }

        if (!Objects.equals(clan.getWarWinStreak(), response.warWinStreak())) {
            clan.setWarWinStreak(response.warWinStreak());
            clan.setWarWinStreakChangedAt(now);
        }

        if (!Objects.equals(clan.getWarWins(), response.warWins())) {
            clan.setWarWins(response.warWins());
            clan.setWarWinsChangedAt(now);
        }

        if (!Objects.equals(clan.getWarTies(), response.warTies())) {
            clan.setWarTies(response.warTies());
            clan.setWarTiesChangedAt(now);
        }

        if (!Objects.equals(clan.getWarLosses(), response.warLosses())) {
            clan.setWarLosses(response.warLosses());
            clan.setWarLossesChangedAt(now);
        }

        if (!Objects.equals(clan.getIsWarLogPublic(), response.isWarLogPublic())) {
            clan.setIsWarLogPublic(response.isWarLogPublic());
            clan.setIsWarLogPublicChangedAt(now);
        }

        WarLeague warLeague = warLeagueService.getById(response.warLeague().id());
        if (!Objects.equals(clan.getWarLeague(), warLeague)) {
            clan.setWarLeague(warLeague);
            clan.setWarLeagueChangedAt(now);
        }

        if (!Objects.equals(clan.getMemberCount(), response.memberCount())) {
            clan.setMemberCount(response.memberCount());
            clan.setMemberCountChangedAt(now);
        }

        if (!Objects.equals(clan.getRequiredBuilderBaseTrophies(), response.requiredBuilderBaseTrophies())) {
            clan.setRequiredBuilderBaseTrophies(response.requiredBuilderBaseTrophies());
            clan.setRequiredBuilderBaseTrophiesChangedAt(now);
        }

        if (!Objects.equals(clan.getRequiredTownHallLevel(), response.requiredTownHallLevel())) {
            clan.setRequiredTownHallLevel(response.requiredTownHallLevel());
            clan.setRequiredTownHallLevelChangedAt(now);
        }

        LeagueTier requiredLeagueTier = leagueTierService.getById(response.requiredLeagueTier().id());
        if (!Objects.equals(clan.getRequiredLeagueTier(), requiredLeagueTier)) {
            clan.setRequiredLeagueTier(requiredLeagueTier);
            clan.setRequiredLeagueTierChangedAt(now);
        }

        ClanChatLanguage clanChatLanguage = toClanChatLanguage(response.chatLanguage());
        if (!Objects.equals(clan.getClanChatLanguage(), clanChatLanguage)) {
            clan.setClanChatLanguage(clanChatLanguage);
            clan.setClanChatLanguageChangedAt(now);
        }
    }

    public void updateMembers(@NonNull List<Member> members, @NonNull List<ClanMemberResponse> responses) {
//        Map<String, ClanMemberResponse> clanMemberResponsesByTag = new HashMap<>();
//        responses.forEach(response -> clanMemberResponsesByTag.put(response.tag(), response));
//
//        for (Member member : members) {
//            if (!clanMemberResponsesByTag.containsKey(member.getTag())) {
//                member.set
//            }
//        }
//
//        if (this.memberList.stream().noneMatch(existing ->existing.getTag().equals(member.getTag()))) {
//            this.memberList.add(member);
//        } ЭтО НАВЕРНОЕ ДОЛЖНО БЫТЬ В MemberService
    }

    public void setMemberListChangedAt(@NonNull Instant instant) {
//        this.memberListChangedAt = instant; Так же
    }

    private ClanBadgeUrls toClanBadgeUrls(
            ClanBadgeUrlsResponse response) {
        return response == null
                ? null
                : new ClanBadgeUrls(
                        response.small(),
                        response.large(),
                        response.medium());
    }

    private ClanChatLanguage toClanChatLanguage(
            ClanChatLanguageResponse response) {
        return response == null
                ? null
                : new ClanChatLanguage(
                        response.id(),
                        response.name(),
                        response.languageCode());
    }

}
