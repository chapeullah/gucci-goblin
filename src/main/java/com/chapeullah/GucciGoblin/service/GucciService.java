package com.chapeullah.GucciGoblin.service;

import com.chapeullah.GucciGoblin.client.GucciClient;
import com.chapeullah.GucciGoblin.dto.ClanMembersResponse;
import com.chapeullah.GucciGoblin.entity.MemberEntity;
import com.chapeullah.GucciGoblin.infrastructure.GucciLogger;
import com.chapeullah.GucciGoblin.model.Member;
import com.chapeullah.GucciGoblin.repository.MemberRepository;
import jdk.jfr.Experimental;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GucciService {

    private final GucciLogger gucciLogger = GucciLogger.of(GucciService.class);

    private final GucciClient gucciClient;
    private final MemberRepository memberRepository;
    private final MemberDeltaService memberDeltaService;

    public void init() {
        if (memberRepository.count() != 0) {
            gucciLogger.info("DB already has data. Skipping initial load.");
            return;
        }

        gucciLogger.info("DB is empty. Initializing members.");

        LinkedHashMap<String, Member> newMembers = mapFrom(gucciClient.getMembers());

        LinkedHashMap<String, Member> merged = new LinkedHashMap<>();
        for (Member member : newMembers.values()) {
            merged.put(member.getTag(), Member.initFrom(member));
        }

        List<MemberEntity> entities = new ArrayList<>();
        for (Member m : merged.values()) entities.add(MemberEntity.from(m));

        memberRepository.saveAll(entities);
        gucciLogger.info("Initializing members SUCCESS. Members: " + entities.size());
    }

    public void updateMembers() {
        gucciLogger.info("Starting members synchronization.");

        LinkedHashMap<String, Member> newMembers = mapFrom(gucciClient.getMembers());

        LinkedHashMap<String, Member> oldMembers = mapFrom(memberRepository.findAll());

        memberDeltaService.memberDeltaOutput(oldMembers, newMembers);

        LinkedHashMap<String, Member> members = new LinkedHashMap<>();

        for (Member newMember : newMembers.values()) {
            String tag = newMember.getTag();
            upsertMember(members, tag, oldMembers.get(tag), newMember);
        }

        memberRepository.deleteAllInBatch();

        List<MemberEntity> entities = members.values().stream()
                .map(MemberEntity::from)
                .toList();

        memberRepository.saveAll(entities);

        gucciLogger.info("Members synchronization SUCCESS. Members: " + entities.size());
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

    private static LinkedHashMap<String, Member> mapFrom(@NonNull ClanMembersResponse res) {
        LinkedHashMap<String, Member> members = new LinkedHashMap<>();

        for (ClanMembersResponse.Member resM : res.items()) {
            if (resM == null) continue;
            Member m = Member.from(resM);
            members.put(m.getTag(), m);
        }

        return members;
    }

    private static LinkedHashMap<String, Member> mapFrom(@NonNull List<MemberEntity> entities) {
        LinkedHashMap<String, Member> members = new LinkedHashMap<>();

        for (MemberEntity e : entities) {
            if (e == null) continue;
            Member m = Member.from(e);
            members.put(m.getTag(), m);
        }

        return members;
    }

    @Deprecated
    private static String consoleName(@NonNull String s) {
        String t = s.replaceAll("[^a-zA-Z0-9А-Яа-яЁё _\\-\\.]", "?");
        return t.replaceAll("\\s+", " ").trim();
    }

}
