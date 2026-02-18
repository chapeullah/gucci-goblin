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

    private final GucciLogger gucciLogger = GucciLogger.of(GucciService.class);

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
            gucciLogger.info("No changes");
            return;
        }

        gucciLogger.info("Changes:");

        for (MemberDelta memberDelta : memberDeltas) {
            printMemberDelta(memberDelta);
        }
    }

    private static void printMemberDelta(@NonNull MemberDelta memberDelta) {
        switch (memberDelta.getMembershipStatus()) {
            case JOINED -> {
                System.out.println(headerFor(memberDelta, memberDelta.getNameDelta().newValue()) + "[+] JOINED clan");
            }
            case LEFT -> {
                System.out.println(headerFor(memberDelta, memberDelta.getNameDelta().oldValue()) + "[-] LEFT clan");
            }
            case NO_CHANGE -> {
                String header = headerFor(memberDelta, memberDelta.getNameDelta().newValue());
                printIfChange(header, "Name", memberDelta.getNameDelta());
                printIfChange(header, "Clan role", memberDelta.getRoleDelta());
                printIfChange(header, "TH level", memberDelta.getTownHallLevelDelta());
                printIfChange(header, "Level", memberDelta.getExpLevelDelta());
                printIfChange(header, "Builder base trophies", memberDelta.getBuilderBaseTrophiesDelta());
                printIfChange(header, "Donations", memberDelta.getDonationsDelta());
                printIfChange(header, "Donations received", memberDelta.getDonationsReceivedDelta());
            }
        }
    }

    private static String headerFor(@NonNull MemberDelta memberDelta, String name) {
        return String.format("%-7s%-13s%-18s", " ", memberDelta.getTag(), name);
    }

    private static <T> void printIfChange(
            @NonNull String header,
            @NonNull String field,
            @NonNull MemberDelta.Delta<T> delta
    ) {
        if (delta.changed()) {
            System.out.println(header + String.format("%-24s ", field) + delta);
        }
    }

}
