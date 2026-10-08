package com.chapeullah.guccigoblin.clan;

import com.chapeullah.guccigoblin.ClashOfClansClient;
import com.chapeullah.guccigoblin.capitalleague.CapitalLeague;
import com.chapeullah.guccigoblin.capitalleague.CapitalLeagueRepository;
import com.chapeullah.guccigoblin.capitalleague.CapitalLeagueService;
import com.chapeullah.guccigoblin.clan.dto.ClanBadgeUrlsResponse;
import com.chapeullah.guccigoblin.clan.dto.ClanMemberResponse;
import com.chapeullah.guccigoblin.clan.dto.ClanResponse;
import com.chapeullah.guccigoblin.clan.model.Clan;
import com.chapeullah.guccigoblin.clan.model.ClanBadgeUrls;
import com.chapeullah.guccigoblin.clan.model.ClanChatLanguage;
import com.chapeullah.guccigoblin.leaguetier.LeagueTier;
import com.chapeullah.guccigoblin.leaguetier.LeagueTierRepository;
import com.chapeullah.guccigoblin.leaguetier.LeagueTierService;
import com.chapeullah.guccigoblin.leaguetier.LocationService;
import com.chapeullah.guccigoblin.location.Location;
import com.chapeullah.guccigoblin.location.LocationRepository;
import com.chapeullah.guccigoblin.member.MemberService;
import com.chapeullah.guccigoblin.player.model.Player;
import com.chapeullah.guccigoblin.player.service.PlayerService;
import com.chapeullah.guccigoblin.warleague.WarLeague;
import com.chapeullah.guccigoblin.warleague.WarLeagueRepository;
import com.chapeullah.guccigoblin.warleague.WarLeagueService;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClanService {

    private final ClashOfClansClient client;

    private final ClanRepository clanRepository;

    private final LocationRepository locationRepository;
    private final CapitalLeagueRepository capitalLeagueRepository;
    private final WarLeagueRepository warLeagueRepository;
    private final LeagueTierRepository leagueTierRepository;
    private final ClanChatLanguageRepository clanChatLanguageRepository;

    private final LocationService locationService;
    private final CapitalLeagueService capitalLeagueService;
    private final WarLeagueService warLeagueService;
    private final LeagueTierService leagueTierService;

    private final MemberService memberService;
    private final PlayerService playerService;

    @Transactional
    public Clan syncClan(@NonNull String clanTag) {
        ClanResponse clanResponse = client.getClan(clanTag);

        locationService.syncLocations();
        Location location = clanResponse.location() == null
                ? null
                : locationRepository.findById(clanResponse.location().id())
                .orElseThrow(() -> new IllegalStateException(
                        "Location not found: " + clanResponse.location().id()));

        ClanBadgeUrlsResponse badgeResponse = clanResponse.badgeUrls();
        ClanBadgeUrls badgeUrls = badgeResponse == null ? null : new ClanBadgeUrls(
                badgeResponse.small(),
                badgeResponse.large(),
                badgeResponse.medium());

        capitalLeagueService.syncCapitalLeagues();
        CapitalLeague capitalLeague = clanResponse.capitalLeague() == null
                ? null
                : capitalLeagueRepository.findById(clanResponse.capitalLeague().id())
                .orElseThrow(() -> new IllegalStateException(
                        "Capital league not found: " + clanResponse.capitalLeague().id()));

        warLeagueService.syncWarLeagues();
        WarLeague warLeague = clanResponse.warLeague() == null
                ? null
                : warLeagueRepository.findById(clanResponse.warLeague().id())
                .orElseThrow(() -> new IllegalStateException(
                        "War league not found: " + clanResponse.warLeague().id()));

        leagueTierService.syncLeagueTiers();
        LeagueTier requiredLeagueTier = clanResponse.requiredLeagueTier() == null
                ? null
                : leagueTierRepository.findById(clanResponse.requiredLeagueTier().id())
                .orElseThrow(() -> new IllegalStateException(
                        "Required league tier not found: " + clanResponse.requiredLeagueTier().id()));

        var languageResponse = clanResponse.chatLanguage();
        ClanChatLanguage chatLanguage = languageResponse == null
                ? null
                : clanChatLanguageRepository.save(
                new ClanChatLanguage(
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

        if (clanResponse.memberList() == null) {
            throw new IllegalStateException("Clan member list is missing");
        }

        Clan existing = clanRepository.findById(clan.getTag())
                .orElse(null);

        Clan savedClan;

        if (existing == null) {
            savedClan = clanRepository.save(clan);
        } else {
            existing.updateFrom(clan);
            savedClan = existing;
        }

        memberService.syncMembers(savedClan, clanResponse.memberList());

        Set<String> memberTags = clanResponse.memberList().stream()
                .map(ClanMemberResponse::tag)
                .collect(Collectors.toSet());

        savedClan.getMemberList().removeIf(player -> {
            if (!memberTags.contains(player.getTag())) {
                player.changeClan(null);
                return true;
            }
            return false;
        });

        for (ClanMemberResponse memberResponse : clanResponse.memberList()) {
            Player member = playerService.syncPlayer(memberResponse.tag());

            Clan previousClan = member.getClan();

            if (previousClan != null && !previousClan.getTag().equals(savedClan.getTag())) {
                previousClan.getMemberList().removeIf(player -> player.getTag().equals(member.getTag()));
            }

            member.changeClan(savedClan);

            boolean alreadyAdded = savedClan.getMemberList().stream()
                    .anyMatch(player ->
                            player.getTag().equals(member.getTag()));

            if (!alreadyAdded) {
                savedClan.getMemberList().add(member);
            }
        }

        return savedClan;
    }

}
