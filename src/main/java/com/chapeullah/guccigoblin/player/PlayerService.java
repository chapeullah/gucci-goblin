package com.chapeullah.guccigoblin.player;

import com.chapeullah.guccigoblin.member.Member;
import com.chapeullah.guccigoblin.member.MemberRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final MemberRepository memberRepository;

    public void syncPlayers() {
        LinkedHashMap<String, Member> currentMembers = mapMembersFrom(memberRepository.findAll());
        LinkedHashMap<String, Player> players = mapPlayersFrom(playerRepository.findAll());
        for (Member member : currentMembers.values()) {
            String tag = member.getTag();
            Player player = players.get(tag);
            if (player == null) {
                players.put(tag, Player.joined(member));
            } else {
                player.updateName(member.getName());
                if (player.getLeftAt() != null) {
                    player.rejoin();
                }
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
