package com.chapeullah.guccigoblin.member;

import com.chapeullah.guccigoblin.ClashOfClansClient;
import com.chapeullah.guccigoblin.builderbaseleague.BuilderBaseLeague;
import com.chapeullah.guccigoblin.builderbaseleague.BuilderBaseLeagueService;
import com.chapeullah.guccigoblin.clan.dto.ClanMemberResponse;
import com.chapeullah.guccigoblin.clan.model.Clan;
import com.chapeullah.guccigoblin.leaguetier.LeagueTier;
import com.chapeullah.guccigoblin.leaguetier.LeagueTierService;
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

    private final ClashOfClansClient client;

    private final MemberRepository memberRepository;

    private final LeagueTierService leagueTierService;
    private final BuilderBaseLeagueService builderBaseLeagueService;

    public List<Member> syncMembers(@NonNull String clanTag) {
        return List.of();
    }

    public List<Member> syncMembers(
            @NonNull Clan clan,
            @NonNull List<ClanMemberResponse> responses) {
        leagueTierService.syncLeagueTiers();
        builderBaseLeagueService.syncBuilderBaseLeagues();

        Map<String, Member> incomingMembers = responses.stream()
                .collect(Collectors.toMap(
                        ClanMemberResponse::tag,
                        response -> toMember(clan, response)));

        List<Member> existingMembers = memberRepository.findAllByClanTag(clan.getTag());

        Map<String, Member> existingMembersByTag = existingMembers.stream()
                .collect(Collectors.toMap(
                        Member::getTag,
                        Function.identity()));

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
            if (rejoining) {
                existingMember.rejoin();
            }
            membersToSave.add(existingMember);
        }
        List<Member> savedMembers = memberRepository.saveAll(membersToSave);
        return savedMembers.stream()
                .filter(Member::isInClan)
                .toList();
    }

    private Member toMember(@NonNull Clan clan, @NonNull ClanMemberResponse response) {
        LeagueTier leagueTier = response.leagueTier() == null ? null
                : leagueTierService.findById(response.leagueTier().id());

        BuilderBaseLeague builderBaseLeague = response.builderBaseLeague() == null ? null
                : builderBaseLeagueService.findById(response.builderBaseLeague().id());
        return new Member(
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
    }

}
