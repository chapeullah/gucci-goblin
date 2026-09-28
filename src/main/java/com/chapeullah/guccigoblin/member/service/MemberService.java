package com.chapeullah.guccigoblin.member.service;

import com.chapeullah.guccigoblin.Client;
import com.chapeullah.guccigoblin.member.Member;
import com.chapeullah.guccigoblin.member.MemberRepository;
import com.chapeullah.guccigoblin.member.dto.MemberResponse;
import com.chapeullah.guccigoblin.member.dto.MemberSnapshot;
import com.chapeullah.guccigoblin.member.dto.MemberSyncResult;
import com.chapeullah.guccigoblin.member.dto.MembersResponse;
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
    public MemberSyncResult syncMembers() {
        LinkedHashMap<String, Member> currentMembers =
                mapFrom(client.getMembers());

        LinkedHashMap<String, Member> oldMembers =
                mapMembersFrom(memberRepository.findAllByInClanTrue());

        memberDeltaService.memberDeltaOutput(oldMembers, currentMembers);

        List<MemberSnapshot> joined = currentMembers.values().stream()
                .filter(member -> !oldMembers.containsKey(member.getTag()))
                .map(MemberSnapshot::from)
                .toList();

        List<MemberSnapshot> left = oldMembers.values().stream()
                .filter(member -> !currentMembers.containsKey(member.getTag()))
                .map(MemberSnapshot::from)
                .toList();

        LinkedHashMap<String, Member> knownMembers = new LinkedHashMap<>(oldMembers);
        if (!joined.isEmpty()) {
            List<String> joinedTags = joined.stream()
                    .map(MemberSnapshot::tag)
                    .toList();
            knownMembers.putAll(mapMembersFrom(memberRepository.findAllByTagIn(joinedTags)));
        }

        LinkedHashMap<String, Member> members = new LinkedHashMap<>();

        for (Member newMember : currentMembers.values()) {
            String tag = newMember.getTag();
            upsertMember(members, tag, knownMembers.get(tag), newMember);
        }

        for (MemberSnapshot snapshot : left) {
            Member member = oldMembers.get(snapshot.tag());
            member.leave();
            members.put(member.getTag(), member);
        }

        memberRepository.saveAll(members.values());

        log.info(
                "Members synchronization success. Members: {}, joined: {}, left: {}",
                currentMembers.size(),
                joined.size(),
                left.size());

        return new MemberSyncResult(joined, left);
    }

    private static void upsertMember(
            @NonNull LinkedHashMap<String, Member> members,
            @NonNull String tag,
            Member oldMember,
            @NonNull Member newMember) {
        if (oldMember == null) {
            members.put(tag, Member.initFrom(newMember));
            return;
        }
        if (!oldMember.isInClan()) {
            members.put(tag, Member.rejoin(oldMember, newMember));
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
