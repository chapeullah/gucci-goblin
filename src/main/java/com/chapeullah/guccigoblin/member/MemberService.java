package com.chapeullah.guccigoblin.member;

import com.chapeullah.guccigoblin.builderbaseleague.BuilderBaseLeagueService;
import com.chapeullah.guccigoblin.clan.dto.ClanMemberResponse;
import com.chapeullah.guccigoblin.clan.model.Clan;
import com.chapeullah.guccigoblin.leaguetier.LeagueTierService;
import com.chapeullah.guccigoblin.member.model.Member;
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
        syncLeagueTiersAndBuilderBaseLeaguesIfMissing(clanMemberResponses);

        Map<String, Member> currentMembersByTag = memberRepository
                .findAllByClanTag(clan.getTag())
                .stream()
                .collect(Collectors.toMap(Member::getTag, Function.identity()));

        List<Member> membersToSave = new ArrayList<>();

        for (ClanMemberResponse clanMemberResponse : clanMemberResponses) {
            Member member = currentMembersByTag.get(clanMemberResponse.tag());
            if (member != null) {
                membersToSave.add(updateMember(member, clanMemberResponse));
            } else {
                membersToSave.add(createMember(clanMemberResponse));
            }
        }


    }

    public Member updateMember(
            @NonNull Member member,
            @NonNull ClanMemberResponse response) {
        if (!Objects.equals(member.getTag(), response.tag())) {
            throw new IllegalArgumentException(
                    "Member tags mismatch: currentMemberTag=\"" + member.getTag() + "\", sourceMemberTag=\"" + response.tag() + "\"");
        }

        Instant now = Instant.now();

        // TODO clan

        if (!Objects.equals(member.getName(), response.name())) {
            member.setName(response.name());
            member.setNameChangedAt(now);
        }

        if (!Objects.equals(member.getRole(), response.role())) {
            member.setRole(response.role());
            member.setRoleChangedAt(now);
        }

        if (!Objects.equals(member.getTownHallLevel(), response.townHallLevel())) {
            member.setTownHallLevel(response.townHallLevel());
            member.setTownHallLevelChangedAt(now);
        }

        if (!Objects.equals(member.getExpLevel(), response.expLevel())) {
            member.setExpLevel(response.expLevel());
            member.setExpLevelChangedAt(now);
        }

        // TODO league tier

        if (!Objects.equals(member.getTrophies(), response.trophies())) {
            member.setTrophies(response.trophies());
            member.setTrophiesChangedAt(now);
        }

        if (!Objects.equals(member.getBuilderBaseTrophies(), response.builderBaseTrophies())) {
            member.setBuilderBaseTrophies(response.builderBaseTrophies());
            member.setBuilderBaseTrophiesChangedAt(now);
        }

        if (!Objects.equals(member.getClanRank(), response.clanRank())) {
            member.setClanRank(response.clanRank());
            member.setClanRankChangedAt(now);
        }

        if (!Objects.equals(member.getPreviousClanRank(), response.previousClanRank())) {
            member.setPreviousClanRank(response.previousClanRank());
            member.setPreviousClanRankChangedAt(now);
        }

        if (!Objects.equals(member.getDonations(), response.donations())) {
            member.setDonations(response.donations());
            member.setDonationsChangedAt(now);
        }

        // TODO totalDonations

        if (!Objects.equals(member.getDonationsReceived(), response.donationsReceived())) {
            member.setDonationsReceived(response.donationsReceived());
            member.setDonationsReceivedChangedAt(now);
        }

        // TODO totalDonationsReceived

        // TODO memberHouseElements

        // TODO builderBaseLeague

        // TODO inClan

    }

    public Member createMember(@NonNull ClanMemberResponse response) {

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

}
