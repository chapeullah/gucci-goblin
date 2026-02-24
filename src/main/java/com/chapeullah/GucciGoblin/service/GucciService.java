package com.chapeullah.GucciGoblin.service;

import com.chapeullah.GucciGoblin.client.GucciClient;
import com.chapeullah.GucciGoblin.dto.ClanMembersResponse;
import com.chapeullah.GucciGoblin.entity.MemberEntity;
import com.chapeullah.GucciGoblin.entity.PlayerEntity;
import com.chapeullah.GucciGoblin.infrastructure.GucciLogger;
import com.chapeullah.GucciGoblin.model.Member;
import com.chapeullah.GucciGoblin.model.Player;
import com.chapeullah.GucciGoblin.repository.MemberRepository;
import com.chapeullah.GucciGoblin.repository.PlayerRepository;
import jdk.jfr.Experimental;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.hibernate.internal.build.AllowNonPortable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GucciService {

    private final GucciLogger gucciLogger = GucciLogger.of(GucciService.class);

    private final GucciClient gucciClient;
    private final MemberRepository memberRepository;
    private final PlayerRepository playerRepository;
    private final MemberDeltaService memberDeltaService;

    public void init() {
        if (memberRepository.count() != 0) {
            gucciLogger.info("DB already has data. Skipping initial load.");
            return;
        }

        gucciLogger.info("DB is empty. Initializing MEMBERS.");

        LinkedHashMap<String, Member> newMembers = mapFrom(gucciClient.getMembers());

        LinkedHashMap<String, Member> merged = new LinkedHashMap<>();
        for (Member member : newMembers.values()) {
            merged.put(member.getTag(), Member.initFrom(member));
        }

        List<MemberEntity> entities = new ArrayList<>();
        for (Member m : merged.values()) entities.add(MemberEntity.from(m));

        memberRepository.saveAll(entities);
        gucciLogger.info("Initializing MEMBERS SUCCESS. Members: " + entities.size());
    }

    public void sync() {
        LinkedHashMap<String, Member> currentMembers = mapFrom(gucciClient.getMembers());
        updateMembers(currentMembers);
        updatePlayers(currentMembers);
    }

    private void updateMembers(@NonNull LinkedHashMap<String, Member> currentMembers) {
        gucciLogger.info("Starting MEMBERS synchronization.");

        LinkedHashMap<String, Member> oldMembers = mapMembersFrom(memberRepository.findAll());

        memberDeltaService.memberDeltaOutput(oldMembers, currentMembers);

        LinkedHashMap<String, Member> members = new LinkedHashMap<>();

        for (Member newMember : currentMembers.values()) {
            String tag = newMember.getTag();
            upsertMember(members, tag, oldMembers.get(tag), newMember);
        }

        memberRepository.deleteAllInBatch();

        List<MemberEntity> entities = members.values().stream()
                .map(MemberEntity::from)
                .toList();

        memberRepository.saveAll(entities);

        gucciLogger.info("MEMBERS synchronization SUCCESS. Members: " + entities.size());
    }

    private static void upsertMember(
            @NonNull LinkedHashMap<String, Member> members,
            @NonNull String tag,
            Member oldMember,
            @NonNull Member newMember
    ) {
        if (oldMember == null) {
            members.put(tag, Member.initFrom(newMember));
            return;
        }
        members.put(tag, Member.merge(oldMember, newMember));
    }

    private void updatePlayers(@NonNull LinkedHashMap<String, Member> currentMembers) {
        gucciLogger.info("Starting PLAYERS synchronization.");

        LinkedHashMap<String, Player> players = mapPlayersFrom(playerRepository.findAll());

        for (Member member : currentMembers.values()) {
            String tag = member.getTag();
            Player player = players.get(tag);

            if (player == null) {
                players.put(tag, Player.joined(member));
            } else if (player.getLeft() != null) {
                player.rejoin();
            }
        }

        for (Player player : players.values()) {
            String tag = player.getTag();
            if (!currentMembers.containsKey(tag) && player.getLeft() == null) {
                player.left();
            }
        }

        List<PlayerEntity> entities = players.values().stream()
                .map(PlayerEntity::from)
                .toList();

        playerRepository.saveAll(entities);

        gucciLogger.info("PLAYERS synchronization SUCCESS.");
    }

    private static LinkedHashMap<String, Member> mapFrom(@NonNull ClanMembersResponse res) {
        LinkedHashMap<String, Member> members = new LinkedHashMap<>();

        for (ClanMembersResponse.Member resM : res.items()) {
            if (resM == null) continue;
            Member m = Member.from(resM);
            members.put(m.getTag(), m);
        }

        return members;
    }

    private static LinkedHashMap<String, Member> mapMembersFrom(@NonNull List<MemberEntity> entities) {
        LinkedHashMap<String, Member> members = new LinkedHashMap<>();

        for (MemberEntity e : entities) {
            if (e == null) continue;
            Member m = Member.from(e);
            members.put(m.getTag(), m);
        }

        return members;
    }

    private static LinkedHashMap<String, Player> mapPlayersFrom(@NonNull List<PlayerEntity> entities) {
        LinkedHashMap<String, Player> players = new LinkedHashMap<>();

        for (PlayerEntity e : entities) {
            if (e == null) continue;
            Player p = Player.from(e);
            players.put(p.getTag(), p);
        }

        return players;
    }

}
