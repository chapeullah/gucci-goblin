package com.chapeullah.guccigoblin.service;

import com.chapeullah.guccigoblin.client.Client;
import com.chapeullah.guccigoblin.member.MembersResponse;
import com.chapeullah.guccigoblin.member.Member;
import com.chapeullah.guccigoblin.player.Player;
import com.chapeullah.guccigoblin.member.MemberRepository;
import com.chapeullah.guccigoblin.repository.PlayerRepository;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GucciService {

    private final Client client;
    private final MemberRepository memberRepository;
    private final PlayerRepository playerRepository;
    private final MemberDeltaService memberDeltaService;

    /**
     * Initialize data if not exists
     */
    @Deprecated
    @Transactional
    public void init() {
        System.out.println("═".repeat(47) + " INITIALIZATION " + "═".repeat(46) + "╗");
        initMembers();
        initPlayers();
    }

    /**
     * Method of members initialization
     */
    @Deprecated
    private void initMembers() {
        if (memberRepository.count() > 0) {
            log.info("Members table is already has data, skipping members initialization.");
            return;
        }
        log.info("Members table is empty, initializing members.");

        LinkedHashMap<String, Member> currentMembers = mapFrom(client.getMembers());
        LinkedHashMap<String, Member> mergedMembers = new LinkedHashMap<>();
        for (Member member : currentMembers.values()) {
            mergedMembers.put(member.getTag(), Member.initFrom(member));
        }
        memberRepository.saveAll(mergedMembers.values());

        log.info("Initializing members success. Members: {}", mergedMembers.size());
    }

    /**
     * Method of players initialization
     */
    @Deprecated
    private void initPlayers() {
        if (playerRepository.count() > 0) {
            log.info("Players table is already has data. Skipping players initialization.");
            return;
        }
        log.info("Players table is empty, initializing players.");

        LinkedHashMap<String, Player> players = mapPlayersFrom(playerRepository.findAll());

        // ДОДЕЛАТЬ

        log.info("Initializing players success.");
    }

    /**
     * Synchronize API with DB
     */
    @Transactional
    public void synchronize() {
        updateMembers();
        updatePlayers();
    }

    private void updateMembers() {
        LinkedHashMap<String, Member> currentMembers = mapFrom(client.getMembers());
        LinkedHashMap<String, Member> oldMembers = mapMembersFrom(memberRepository.findAll());
        memberDeltaService.memberDeltaOutput(oldMembers, currentMembers);
        LinkedHashMap<String, Member> members = new LinkedHashMap<>();
        for (Member newMember : currentMembers.values()) {
            String tag = newMember.getTag();
            upsertMember(members, tag, oldMembers.get(tag), newMember);
        }
        List<String> leftTags = oldMembers.keySet().stream()
                .filter(tag -> !currentMembers.containsKey(tag))
                .toList();
        memberRepository.deleteAllByIdInBatch(leftTags);
        memberRepository.saveAll(members.values());

        log.info("Members synchronization success. Members: {}", members.size());
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

    private void updatePlayers() {
        LinkedHashMap<String, Member> currentMembers = mapMembersFrom(memberRepository.findAll());
        LinkedHashMap<String, Player> players = mapPlayersFrom(playerRepository.findAll());
        for (Member member : currentMembers.values()) {
            String tag = member.getTag();
            Player player = players.get(tag);
            if (player == null) {
                players.put(tag, Player.joined(member));
            } else if (player.getLeftAt() != null) {
                player.rejoin();
            }
        }
        for (Player player : players.values()) {
            String tag = player.getTag();
            if (!currentMembers.containsKey(tag) && player.getLeftAt() == null) {
                player.left();
            }
        }
        playerRepository.saveAll(players.values());

        log.info("Players synchronization success.");
    }


    public Instant showMembersLastActivity(@NonNull String tag) {
        if (!tag.startsWith("#")) tag = "#" + tag;
        return memberRepository.findLastActivity(tag);
    }

    private static LinkedHashMap<String, Member> mapFrom(@NonNull MembersResponse res) {
        LinkedHashMap<String, Member> members = new LinkedHashMap<>();
        for (MembersResponse.Member resM : res.items()) {
            if (resM == null) continue;
            Member m = Member.from(resM);
            members.put(m.getTag(), m);
        }
        return members;
    }

    private static LinkedHashMap<String, Member> mapMembersFrom(@NonNull List<Member> entities) {
        LinkedHashMap<String, Member> members = new LinkedHashMap<>();
        for (Member member : entities) {
            if (member == null) continue;
            members.put(member.getTag(), member);
        }
        return members;
    }

    private static LinkedHashMap<String, Player> mapPlayersFrom(@NonNull List<Player> entities) {
        LinkedHashMap<String, Player> players = new LinkedHashMap<>();
        for (Player player : entities) {
            if (player == null) continue;
            players.put(player.getTag(), player);
        }
        return players;
    }

}
