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
import com.chapeullah.guccigoblin.clan.model.ClanLabelLink;
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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClanService {

    private final ClashOfClansClient client;

    private final ClanRepository clanRepository;

    private final LocationService locationService;
    private final CapitalLeagueService capitalLeagueService;
    private final WarLeagueService warLeagueService;
    private final LeagueTierService leagueTierService;
    private final ClanLabelService clanLabelService;
    private final ClanBadgeUrlsService clanBadgeUrlsService;
    private final ClanLabelLinkService clanLabelLinkService;
    private final ClanChatLanguageService clanChatLanguageService;

    private final MemberService memberService;

    @Transactional
    public Clan syncClan(@NonNull String clanTag) {
        ClanResponse response = client.getClan(clanTag);
        if (response.memberList() == null) {
            throw new IllegalStateException("Clan member list is missing: clanTag=\"" + clanTag + "\"");
        }

        syncLocationsIfMissing(response);
        syncCapitalLeaguesIfMissing(response);
        syncWarLeaguesIfMissing(response);
        syncClanLabelsIfMissing(response);
        syncLeagueTiersIfMissing(response);

        Clan clan = clanRepository.findById(response.tag()).orElse(null);
        if (clan == null) {
            clan = createClan(response);
        } else {
            updateClan(clan, response);
        }
        clan = clanRepository.findById(response.tag()).orElseThrow(
                () -> new IllegalStateException("Clan not found after creation or update: clanTag\"" +
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
        return clanRepository.save(clan);
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

    public Clan createClan(@NonNull ClanResponse response) {
        Location location = response.location() == null ? null
                : locationService.getById(response.location().id());

        CapitalLeague capitalLeague = response.capitalLeague() == null ? null
                : capitalLeagueService.getById(response.capitalLeague().id());

        WarLeague warLeague = response.warLeague() == null ? null
                : warLeagueService.getById(response.warLeague().id());

        LeagueTier requiredLeagueTier = response.requiredLeagueTier() == null ? null
                : leagueTierService.getById(response.requiredLeagueTier().id());

        ClanChatLanguage chatLanguage = response.chatLanguage() == null ? null
                : clanChatLanguageService.getOrCreate(response.chatLanguage());

        Clan clan = new Clan(
                response.tag(),
                response.name(),
                response.type(),
                response.description(),
                location,
                response.isFamilyFriendly(),
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

        clan = clanRepository.save(clan);

        // TODO: clanBadgeUrls
        ClanBadgeUrls clanBadgeUrls = clanBadgeUrlsService
                .syncAndGetByClan(clan, response.badgeUrls());
        clan.setClanBadgeUrls(clanBadgeUrls);

        // TODO: memberList

        // TODO: labelLinks

        return clan;
    }

    public void updateClan(@NonNull Clan clan, @NonNull ClanResponse response) {
        if (!clan.getTag().equals(response.tag())) {
            throw new IllegalArgumentException("Clan tag mismatch");
        }

        Instant now = Instant.now();

        // type
        // typeChangedAt
        if (!Objects.equals(clan.getType(), response.type())) {
            clan.setType(response.type());
            clan.setTypeChangedAt(now);
        }

        // description
        // descriptionChangedAt
        if (!Objects.equals(clan.getDescription(), response.description())) {
            clan.setDescription(response.description());
            clan.setDescriptionChangedAt(now);
        }

        // location
        // locationChangedAt
        Location location = response.location() == null ? null
                : locationService.getById(response.location().id());
        if (!Objects.equals(clan.getLocation(), location)) {
            clan.setLocation(location);
            clan.setLocationChangedAt(now);
        }

        // clanBadgeUrls
        // clanBadgeUrlsChangedAt
        ClanBadgeUrls currentClanBadgeUrls = clanBadgeUrlsService
                .findByClanTag(clan.getTag()).orElse(null);
        ClanBadgeUrls oldClanBadgeUrls = currentClanBadgeUrls == null ? null
                : new ClanBadgeUrls(
                clan,
                currentClanBadgeUrls.getSmall(),
                currentClanBadgeUrls.getLarge(),
                currentClanBadgeUrls.getMedium());
        ClanBadgeUrls newClanBadgeUrls = clanBadgeUrlsService
                .syncAndGetByClan(clan, response.badgeUrls());
        if (!Objects.equals(oldClanBadgeUrls, newClanBadgeUrls)) {
            clan.setClanBadgeUrls(newClanBadgeUrls);
            clan.setClanBadgeUrlsChangedAt(now);
        }

        // clanLevel
        // clanLevelChangedAt
        if (!Objects.equals(clan.getClanLevel(), response.clanLevel())) {
            clan.setClanLevel(response.clanLevel());
            clan.setClanLevelChangedAt(now);
        }

        // clanPoints
        // clanPointsChanged
        if (!Objects.equals(clan.getClanPoints(), response.clanPoints())) {
            clan.setClanPoints(response.clanPoints());
            clan.setClanPointsChangedAt(now);
        }

        // clanBuilderBasePoints
        // clanBuilderBasePointsChangedAt
        if (!Objects.equals(clan.getClanBuilderBasePoints(), response.clanBuilderBasePoints())) {
            clan.setClanBuilderBasePoints(response.clanBuilderBasePoints());
            clan.setClanBuilderBasePointsChangedAt(now);
        }

        // clanCapitalPoints
        // clanCapitalPointsChangedAt
        if (!Objects.equals(clan.getClanCapitalPoints(), response.clanCapitalPoints())) {
            clan.setClanCapitalPoints(response.clanCapitalPoints());
            clan.setClanCapitalPointsChangedAt(now);
        }

        // capitalLeague
        // capitalLeagueChangedAt
        CapitalLeague capitalLeague = response.capitalLeague() == null ? null :
                capitalLeagueService.getById(response.capitalLeague().id());
        if (!Objects.equals(clan.getCapitalLeague(), capitalLeague)) {
            clan.setCapitalLeague(capitalLeague);
            clan.setCapitalLeagueChangedAt(now);
        }

        // requiredTrophies
        // requiredTrophiesChangedAt
        if (!Objects.equals(clan.getRequiredTrophies(), response.requiredTrophies())) {
            clan.setRequiredTrophies(response.requiredTrophies());
            clan.setRequiredTrophiesChangedAt(now);
        }

        // warFrequency
        // warFrequencyChangedAt
        if (!Objects.equals(clan.getWarFrequency(), response.warFrequency())) {
            clan.setWarFrequency(response.warFrequency());
            clan.setWarFrequencyChangedAt(now);
        }

        // warWinStreak
        // warWinStreakChangedAt
        if (!Objects.equals(clan.getWarWinStreak(), response.warWinStreak())) {
            clan.setWarWinStreak(response.warWinStreak());
            clan.setWarWinStreakChangedAt(now);
        }

        // warWins
        // warWinsChangedAt
        if (!Objects.equals(clan.getWarWins(), response.warWins())) {
            clan.setWarWins(response.warWins());
            clan.setWarWinsChangedAt(now);
        }

        // warTies
        // warTiesChangedAt
        if (!Objects.equals(clan.getWarTies(), response.warTies())) {
            clan.setWarTies(response.warTies());
            clan.setWarTiesChangedAt(now);
        }

        // warLosses
        // warLossesChangedAt
        if (!Objects.equals(clan.getWarLosses(), response.warLosses())) {
            clan.setWarLosses(response.warLosses());
            clan.setWarLossesChangedAt(now);
        }

        // isWarLogPublic
        // isWarLogPublicChangedAt
        if (!Objects.equals(clan.getIsWarLogPublic(), response.isWarLogPublic())) {
            clan.setIsWarLogPublic(response.isWarLogPublic());
            clan.setIsWarLogPublicChangedAt(now);
        }

        // warLeague
        // warLeagueChangedAt
        WarLeague warLeague = response.warLeague() == null ? null
                : warLeagueService.getById(response.warLeague().id());
        if (!Objects.equals(clan.getWarLeague(), warLeague)) {
            clan.setWarLeague(warLeague);
            clan.setWarLeagueChangedAt(now);
        }

        // memberCount
        // memberCountChangedAt
        if (!Objects.equals(clan.getMemberCount(), response.memberCount())) {
            clan.setMemberCount(response.memberCount());
            clan.setMemberCountChangedAt(now);
        }

        // memberList
        // memberListChangedAt
        Set<String> memberSetBefore = memberService
                .getCurrentMembersByClanTag(clan.getTag())
                .stream()
                .map(Member::getTag)
                .collect(Collectors.toSet());
        Set<String> memberSetAfter = memberService
                .syncMembers(clan, response.memberList())
                .stream()
                .map(Member::getTag)
                .collect(Collectors.toSet());
        if (!Objects.equals(memberSetBefore, memberSetAfter)) {
            clan.setMemberListChangedAt(now);
        }

        // labelLinks
        // labelLinksChangedAt
        Set<ClanLabelLink> clanLabelLinkSetBefore =
                new HashSet<>(clanLabelLinkService
                        .getAllByClanTag(clan.getTag()));
        Set<ClanLabelLink> clanLabelLinkSetAfter =
                new HashSet<>(clanLabelLinkService
                        .syncAndGetByClan(clan, response.labels()));
        if (!Objects.equals(clanLabelLinkSetBefore, clanLabelLinkSetAfter)) {
            clan.setLabelLinksChangedAt(now);
        }

        // requiredBuilderBaseTrophies
        // requiredBuilderBaseTrophiesChangedAt
        if (!Objects.equals(
                clan.getRequiredBuilderBaseTrophies(),
                response.requiredBuilderBaseTrophies())) {
            clan.setRequiredBuilderBaseTrophies(
                    response.requiredBuilderBaseTrophies());
            clan.setRequiredBuilderBaseTrophiesChangedAt(now);
        }

        // requiredTownHallLevel
        // requiredTownHallLevelChangedAt
        if (!Objects.equals(clan.getRequiredTownHallLevel(), response.requiredTownHallLevel())) {
            clan.setRequiredTownHallLevel(response.requiredTownHallLevel());
            clan.setRequiredTownHallLevelChangedAt(now);
        }

        // requiredLeagueTier
        // requiredLeagueTierChangedAt
        LeagueTier requiredLeagueTier = response.requiredLeagueTier() == null ? null
                : leagueTierService.getById(response.requiredLeagueTier().id());
        if (!Objects.equals(clan.getRequiredLeagueTier(), requiredLeagueTier)) {
            clan.setRequiredLeagueTier(requiredLeagueTier);
            clan.setRequiredLeagueTierChangedAt(now);
        }

        // clanChatLanguage
        // clanChatLanguageChangedAt
        ClanChatLanguage clanChatLanguage = response.chatLanguage() == null
                ? null
                : clanChatLanguageService.getOrCreate(response.chatLanguage());
        if (!Objects.equals(clan.getClanChatLanguage(), clanChatLanguage)) {
            clan.setClanChatLanguage(clanChatLanguage);
            clan.setClanChatLanguageChangedAt(now);
        }
    }

    public void updateMembers(
            @NonNull List<Member> members,
            @NonNull List<ClanMemberResponse> responses) {
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
