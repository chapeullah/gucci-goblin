package com.chapeullah.guccigoblin.member;

import com.chapeullah.guccigoblin.builderbaseleague.BuilderBaseLeague;
import com.chapeullah.guccigoblin.builderbaseleague.BuilderBaseLeagueService;
import com.chapeullah.guccigoblin.clan.dto.ClanMemberResponse;
import com.chapeullah.guccigoblin.clan.model.Clan;
import com.chapeullah.guccigoblin.leaguetier.LeagueTier;
import com.chapeullah.guccigoblin.leaguetier.LeagueTierService;
import com.chapeullah.guccigoblin.member.dto.MemberHouseElementResponse;
import com.chapeullah.guccigoblin.member.model.Member;
import com.chapeullah.guccigoblin.member.model.MemberHouseElement;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
            @NonNull List<ClanMemberResponse> responses) {
        syncLeagueTiersIfMissing(responses);
        syncBuilderBaseLeagueIfMissing(responses);

        Map<String, Member> incomingMembers = responses.stream()
                .collect(Collectors.toMap(
                        ClanMemberResponse::tag,
                        response -> toMember(clan, response)));
        List<Member> existingMembers = memberRepository.findAllByClanTag(clan.getTag());

        Map<String, Member> existingMembersByTag = existingMembers.stream()
                .collect(Collectors.toMap(Member::getTag, Function.identity()));

        memberDeltaService.memberDeltaOutput(existingMembersByTag, incomingMembers);

        List<Member> membersToSave = new ArrayList<>();

        for (Map.Entry<String, Member> entry : incomingMembers.entrySet()) {
            String tag = entry.getKey();
            Member incomingMember = entry.getValue();
            Member existingMember = existingMembersByTag.get(tag);
            if (existingMember == null) {
                membersToSave.add(incomingMember);
                continue;
            }
            boolean rejoining = !existingMember.isInClan();
            existingMember.updateFrom(incomingMember);
            syncMemberHouseElements(existingMember, incomingMember);
            if (rejoining) {
                existingMember.rejoin();
            }
            membersToSave.add(existingMember);
        }
        for (Member existingMember : existingMembers) {
            if (existingMember.isInClan() &&
                    !incomingMembers.containsKey(existingMember.getTag())) {
                existingMember.leave();
                membersToSave.add(existingMember);
            }
        }
        List<Member> savedMembers = memberRepository.saveAll(membersToSave);
        for (Member member : savedMembers) {
            clan.addMember(member);
        }
        return savedMembers.stream()
                .filter(Member::isInClan)
                .toList();
    }

    private void syncLeagueTiersIfMissing(
            @NonNull List<ClanMemberResponse> responses) {
        boolean syncRequired = false;
        for (var response : responses) {
            if (response.leagueTier() == null) {
                continue;
            }
            if (leagueTierService.findById(response.leagueTier().id()).isEmpty()) {
                syncRequired = true;
                break;
            }
        }
        if (syncRequired) {
            leagueTierService.syncAll();
        }
    }

    private void syncBuilderBaseLeagueIfMissing(
            @NonNull List<ClanMemberResponse> responses) {
        boolean syncRequired = false;
        for (var response : responses) {
            if (response.builderBaseLeague() == null) {
                continue;
            }
            if (builderBaseLeagueService.findById(response.builderBaseLeague().id()).isEmpty()) {
                syncRequired = true;
                break;
            }
        }
        if (syncRequired) {
            builderBaseLeagueService.syncAll();
        }
    }

    private void syncMemberHouseElements(
            @NonNull Member target,
            @NonNull Member source) {
        List<MemberHouseElement> elements =
                source.getMemberHouseElements().stream()
                        .map(element -> new MemberHouseElement(
                                element.getElementId(),
                                element.getElementType()))
                        .toList();
        target.getMemberHouseElements().clear();
        target.getMemberHouseElements().addAll(elements);
    }

    private Member toMember(@NonNull Clan clan, @NonNull ClanMemberResponse response) {
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
        if (response.playerHouse() != null && response.playerHouse().elements() != null) {
            for (MemberHouseElementResponse element : response.playerHouse().elements()) {
                member.addHouseElement(element.id(), element.type());
            }
        }
        return member;
    }

}
