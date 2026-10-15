package com.chapeullah.guccigoblin.member;

import com.chapeullah.guccigoblin.builderbaseleague.BuilderBaseLeague;
import com.chapeullah.guccigoblin.builderbaseleague.BuilderBaseLeagueService;
import com.chapeullah.guccigoblin.clan.dto.ClanMemberResponse;
import com.chapeullah.guccigoblin.clan.model.Clan;
import com.chapeullah.guccigoblin.leaguetier.LeagueTier;
import com.chapeullah.guccigoblin.leaguetier.LeagueTierService;
import com.chapeullah.guccigoblin.member.model.Member;
import com.chapeullah.guccigoblin.member.model.MemberHouseElement;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    private final LeagueTierService leagueTierService;
    private final BuilderBaseLeagueService builderBaseLeagueService;
    private final MemberHouseElementService memberHouseElementService;
    private final MemberDeltaService memberDeltaService;

    public List<Member> getAllMembersByClanTag(@NonNull String clanTag) {
        return memberRepository.findAllByClanTag(clanTag);
    }

    @Transactional
    public List<Member> getCurrentMembersByClanTag(@NonNull String clanTag) {
        return memberRepository.findAllByClanTagAndInClanTrueOrderByClanRankAsc(clanTag);
    }

    @Transactional
    public List<Member> syncMembers(
            @NonNull Clan clan,
            @NonNull List<ClanMemberResponse> clanMemberResponses) {
        if (clanMemberResponses.isEmpty()) {
            throw new IllegalArgumentException("Clan member list is empty: clanTag=\"" + clan.getTag() + "\"");
        }

        syncLeagueTiersAndBuilderBaseLeaguesIfMissing(clanMemberResponses);

        Map<String, Member> remainingMembersByTag = memberRepository
                .findAllByClanTag(clan.getTag())
                .stream()
                .collect(Collectors.toMap(
                        Member::getTag,
                        Function.identity()));

        Map<String, Member> oldMembersByTag =
                remainingMembersByTag.values().stream()
                        .collect(Collectors.toMap(
                                Member::getTag,
                                this::copyMemberForDelta));

        List<Member> currentMembers = new ArrayList<>();

        for (ClanMemberResponse response : clanMemberResponses) {
            Member member = remainingMembersByTag.remove(response.tag());

            if (member == null) {
                member = createMember(clan, response);
            } else {
                updateMember(member, response);
            }

            currentMembers.add(member);
        }

        Instant now = Instant.now();

        for (Member member : remainingMembersByTag.values()) {
            if (Boolean.TRUE.equals(member.getInClan())) {
                member.setInClan(false);
                member.setInClanChangedAt(now);
            }
        }

        Map<String, Member> newMembersByTag = currentMembers
                .stream()
                .collect(Collectors.toMap(
                        Member::getTag,
                        Function.identity()));

        memberDeltaService.memberDeltaOutput(
                oldMembersByTag,
                newMembersByTag);

        return currentMembers;
    }

    private Member createMember(
            @NonNull Clan clan,
            @NonNull ClanMemberResponse response) {
        LeagueTier leagueTier = response.leagueTier() == null ? null
                : leagueTierService.getById(response.leagueTier().id());
        BuilderBaseLeague builderBaseLeague = response.builderBaseLeague() == null ? null
                : builderBaseLeagueService.getById(response.builderBaseLeague().id());

        Member member = new Member(
                response.tag(),
                clan,
                response.name(),
                response.role(),
                response.townHallLevel(),
                response.expLevel(),
                leagueTier,
                response.trophies(),
                response.builderBaseTrophies(),
                response.clanRank(),
                response.previousClanRank(),
                response.donations(),
                response.donationsReceived(),
                builderBaseLeague);

        Member savedMember = memberRepository.save(member);

        memberHouseElementService.syncAndGetByMember(savedMember, response.playerHouse());

        return savedMember;
    }

    private void updateMember(
            @NonNull Member member,
            @NonNull ClanMemberResponse response) {
        if (!Objects.equals(member.getTag(), response.tag())) {
            throw new IllegalArgumentException(
                    "Member tags mismatch: currentMemberTag=\"" + member.getTag() + "\", sourceMemberTag=\""
                            + response.tag() + "\"");
        }

        Instant now = Instant.now();

        // name
        // nameChangedAt
        if (!Objects.equals(member.getName(), response.name())) {
            member.setName(response.name());
            member.setNameChangedAt(now);
        }

        // role
        // roleChangedAt
        if (!Objects.equals(member.getRole(), response.role())) {
            member.setRole(response.role());
            member.setRoleChangedAt(now);
        }

        // townHallLevel
        // townHallLevelChangedAt
        if (!Objects.equals(member.getTownHallLevel(), response.townHallLevel())) {
            member.setTownHallLevel(response.townHallLevel());
            member.setTownHallLevelChangedAt(now);
        }

        // expLevel
        // expLevelChangedAt
        if (!Objects.equals(member.getExpLevel(), response.expLevel())) {
            member.setExpLevel(response.expLevel());
            member.setExpLevelChangedAt(now);
        }

        // leagueTier
        // leagueTierChangedAt
        LeagueTier newLeagueTier = response.leagueTier() == null ? null
                : leagueTierService.getById(response.leagueTier().id());
        if (!Objects.equals(member.getLeagueTier(), newLeagueTier)) {
            member.setLeagueTier(newLeagueTier);
            member.setLeagueTierChangedAt(now);
        }

        // trophies
        // trophiesChangedAt
        if (!Objects.equals(member.getTrophies(), response.trophies())) {
            member.setTrophies(response.trophies());
            member.setTrophiesChangedAt(now);
        }

        // builderBaseTrophies
        // builderBaseTrophiesChangedAt
        if (!Objects.equals(member.getBuilderBaseTrophies(), response.builderBaseTrophies())) {
            member.setBuilderBaseTrophies(response.builderBaseTrophies());
            member.setBuilderBaseTrophiesChangedAt(now);
        }

        // clanRank
        // clanRankChangedAt
        if (!Objects.equals(member.getClanRank(), response.clanRank())) {
            member.setClanRank(response.clanRank());
            member.setClanRankChangedAt(now);
        }

        // previousClanRank
        // previousClanRankChangedAt
        if (!Objects.equals(member.getPreviousClanRank(), response.previousClanRank())) {
            member.setPreviousClanRank(response.previousClanRank());
            member.setPreviousClanRankChangedAt(now);
        }

        // donations
        // donationsChangedAt
        // totalDonations
        // totalDonationsChangedAt
        Integer oldDonations = member.getDonations();
        Integer newDonations = response.donations();
        if (!Objects.equals(oldDonations, newDonations)) {
            member.setDonations(newDonations);
            member.setDonationsChangedAt(now);
        }
        Integer addedDonations = (!Boolean.TRUE.equals(member.getInClan())
                || newDonations < oldDonations)
                ? newDonations
                : newDonations - oldDonations;
        if (addedDonations > 0) {
            member.setTotalDonations(member.getTotalDonations() + addedDonations);
            member.setTotalDonationsChangedAt(now);
        }

        // donationsReceived
        // donationsReceivedChangedAt
        // totalDonationsReceived
        // totalDonationsReceivedChangedAt
        Integer oldDonationsReceived = member.getDonationsReceived();
        Integer newDonationsReceived = response.donationsReceived();
        if (!Objects.equals(oldDonationsReceived, newDonationsReceived)) {
            member.setDonationsReceived(newDonationsReceived);
            member.setDonationsReceivedChangedAt(now);
        }
        Integer addedDonationsReceived = (!Boolean.TRUE.equals(member.getInClan())
                || newDonationsReceived < oldDonationsReceived)
                ? newDonationsReceived
                : newDonationsReceived - oldDonationsReceived;
        if (addedDonationsReceived > 0) {
            member.setTotalDonationsReceived(member.getTotalDonationsReceived() + addedDonationsReceived);
            member.setTotalDonationsReceivedChangedAt(now);
        }

        // memberHouseElements
        // memberHouseElementsChangedAt
        Map<Integer, String> elementsBefore =
                member.getMemberHouseElements().stream()
                        .collect(Collectors.toMap(
                                MemberHouseElement::getElementId,
                                MemberHouseElement::getElementType));
        Map<Integer, String> elementsAfter =
                memberHouseElementService
                        .syncAndGetByMember(member, response.playerHouse())
                        .stream()
                        .collect(Collectors.toMap(
                                MemberHouseElement::getElementId,
                                MemberHouseElement::getElementType));
        if (!Objects.equals(elementsBefore, elementsAfter)) {
            member.setMemberHouseElementsChangedAt(now);
        }

        // builderBaseLeague
        // builderBaseLeagueChangedAt
        BuilderBaseLeague newBuilderBaseLeague =
                response.builderBaseLeague() == null
                        ? null
                        : builderBaseLeagueService.getById(
                        response.builderBaseLeague().id());
        if (!Objects.equals(
                member.getBuilderBaseLeague(),
                newBuilderBaseLeague)) {
            member.setBuilderBaseLeague(newBuilderBaseLeague);
            member.setBuilderBaseLeagueChangedAt(now);
        }

        // inClan
        // inClanChangedAt
        if (!Boolean.TRUE.equals(member.getInClan())) {
            member.setInClan(true);
            member.setInClanChangedAt(now);
        }

    }

    private void syncLeagueTiersAndBuilderBaseLeaguesIfMissing(
            @NonNull List<ClanMemberResponse> responses) {
        boolean leagueTiersSyncRequired = false, builderBaseLeaguesSyncRequired = false;
        for (ClanMemberResponse response : responses) {
            if (response.leagueTier() == null && response.builderBaseLeague() == null) {
                continue;
            }

            // leagueTier
            Integer leagueTierId = response.leagueTier() == null ? null
                    : response.leagueTier().id();
            if (!leagueTiersSyncRequired
                    && leagueTierId != null
                    && leagueTierService.findById(leagueTierId).isEmpty()) {
                leagueTiersSyncRequired = true;
            }

            // builderBaseLeague
            Integer builderBaseLeagueId = response.builderBaseLeague() == null ? null
                    : response.builderBaseLeague().id();
            if (!builderBaseLeaguesSyncRequired
                    && builderBaseLeagueId != null
                    && builderBaseLeagueService.findById(builderBaseLeagueId).isEmpty()) {
                builderBaseLeaguesSyncRequired = true;
            }

            if (leagueTiersSyncRequired && builderBaseLeaguesSyncRequired) {
                break;
            }
        }
        if (leagueTiersSyncRequired) {
            leagueTierService.syncAll();
        }
        if (builderBaseLeaguesSyncRequired) {
            builderBaseLeagueService.syncAll();
        }
    }

    private Member copyMemberForDelta(@NonNull Member member) {
        Member copy = new Member(
                member.getTag(),
                member.getClan(),
                member.getName(),
                member.getRole(),
                member.getTownHallLevel(),
                member.getExpLevel(),
                member.getLeagueTier(),
                member.getTrophies(),
                member.getBuilderBaseTrophies(),
                member.getClanRank(),
                member.getPreviousClanRank(),
                member.getDonations(),
                member.getDonationsReceived(),
                member.getBuilderBaseLeague());
        copy.setInClan(Boolean.TRUE.equals(member.getInClan()));
        return copy;
    }

}
