package com.chapeullah.GucciGoblin.service;

import com.chapeullah.GucciGoblin.infrastructure.GucciLogger;
import com.chapeullah.GucciGoblin.model.Member;
import com.chapeullah.GucciGoblin.model.MemberDelta;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MemberDeltaService {

    private final GucciLogger gucciLogger = GucciLogger.of(MemberDeltaService.class);

    public static List<MemberDelta> memberDeltasFrom(
            @NonNull LinkedHashMap<String, Member> oldMembers,
            @NonNull LinkedHashMap<String, Member> newMembers
    ) {
        LinkedHashMap<String, Member> oldCopy = new LinkedHashMap<>(oldMembers);

        List<MemberDelta> memberDeltas = new ArrayList<>();

        for (Map.Entry<String, Member> memberEntry : newMembers.entrySet()) {
            String tag = memberEntry.getKey();
            MemberDelta memberDelta;

            Member newMember = memberEntry.getValue();
            Member oldMember = oldMembers.get(tag);

            if (oldMember == null) {
                memberDelta = MemberDelta.joined(newMember);
                memberDeltas.add(memberDelta);
                continue;
            }

            memberDelta = MemberDelta.merge(oldMember, newMember);
            if (memberDelta.hasChanges()) {
                memberDeltas.add(memberDelta);
            }
            oldCopy.remove(tag);
        }

        for (Map.Entry<String, Member> memberEntry : oldCopy.entrySet()) {
            Member oldMember = memberEntry.getValue();
            MemberDelta memberDelta = MemberDelta.left(oldMember);
            memberDeltas.add(memberDelta);
        }

        return memberDeltas;
    }

    public void memberDeltaOutput(
            @NonNull LinkedHashMap<String, Member> oldMembers,
            @NonNull LinkedHashMap<String, Member> newMembers
    ) {
        List<MemberDelta> memberDeltas = MemberDeltaService.memberDeltasFrom(oldMembers, newMembers);

        if (memberDeltas.isEmpty()) {
            gucciLogger.info("Members sync: no changes");
            return;
        }

        gucciLogger.info("Members sync: changes detected");

        for (MemberDelta memberDelta : memberDeltas) {
            printMemberDelta(memberDelta);
        }
    }

    private void printMemberDelta(@NonNull MemberDelta memberDelta) {
        switch (memberDelta.getMembershipStatus()) {
            case JOINED -> {
                gucciLogger.info(headerFor(memberDelta, memberDelta.getNameDelta().newValue()) + "[+] JOINED clan");
            }
            case LEFT -> {
                gucciLogger.info(headerFor(memberDelta, memberDelta.getNameDelta().oldValue()) + "[-] LEFT clan");
            }
            case NO_CHANGE -> {
                String header = headerFor(memberDelta, memberDelta.getNameDelta().newValue());
                logIfChange(header, "Name", memberDelta.getNameDelta());
                logIfChange(header, "Clan role", memberDelta.getRoleDelta());
                logIfChange(header, "TH level", memberDelta.getTownHallLevelDelta());
                logIfChange(header, "Level", memberDelta.getExpLevelDelta());
                logIfChange(header, "Builder base trophies", memberDelta.getBuilderBaseTrophiesDelta());
                logIfChange(header, "Donations", memberDelta.getDonationsDelta());
                logIfChange(header, "Donations received", memberDelta.getDonationsReceivedDelta());
            }
        }
    }

    private static String headerFor(@NonNull MemberDelta memberDelta, String name) {
        return String.format("%-7s%-13s%-18s", " ", memberDelta.getTag(), consoleName(name));
    }

    private <T> void logIfChange(
            @NonNull String header,
            @NonNull String field,
            @NonNull MemberDelta.Delta<T> delta
    ) {
        if (delta.changed()) {
            gucciLogger.info(header + String.format("%-24s ", field) + delta);
        }
    }

    private static String consoleName(@NonNull String s) {
        String t = s.replaceAll("[^\\p{L}\\p{N} _\\-\\.'’]", "▫");
        return t.replaceAll("\\s+", " ").trim();
    }

}
