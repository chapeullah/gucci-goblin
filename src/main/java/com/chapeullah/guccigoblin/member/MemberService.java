package com.chapeullah.guccigoblin.member;

import com.chapeullah.guccigoblin.client.Client;
import com.chapeullah.guccigoblin.member.dto.MemberResponse;
import com.chapeullah.guccigoblin.member.dto.MembersResponse;
import com.chapeullah.guccigoblin.player.Player;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final Client client;

    private final MemberRepository memberRepository;
    private final MemberDeltaService memberDeltaService;

    @Transactional
    public void syncMembers() {
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

    private static LinkedHashMap<String, Member> mapFrom(
            @NonNull MembersResponse response) {
        LinkedHashMap<String, Member> members = new LinkedHashMap<>();
        for (MemberResponse memberResponse : response.items()) {
            if (memberResponse == null) continue;
            Member member = Member.from(memberResponse);
            members.put(member.getTag(), member);
        }
        return members;
    }

    private static LinkedHashMap<String, Member> mapMembersFrom(
            @NonNull List<Member> members) {
        LinkedHashMap<String, Member> membersMap = new LinkedHashMap<>();
        for (Member member : members) {
            if (member == null) continue;
            membersMap.put(member.getTag(), member);
        }
        return membersMap;
    }

}
