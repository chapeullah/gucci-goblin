package com.chapeullah.guccigoblin.member;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberDeltaService {

    public static List<MemberDelta> memberDeltasFrom(
            @NonNull LinkedHashMap<String, Member> oldMembers,
            @NonNull LinkedHashMap<String, Member> newMembers) {
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
            log.info("Members sync: no changes");
            return;
        }

        log.info("Members sync: changes detected");

        for (MemberDelta memberDelta : memberDeltas) {
            printMemberDelta(memberDelta);
        }
    }

    private void printMemberDelta(@NonNull MemberDelta memberDelta) {
        switch (memberDelta.getMembershipStatus()) {
            case JOINED -> {
                log.info("{}[+] JOINED clan", headerFor(memberDelta, memberDelta.getNameDelta().newValue()));
            }
            case LEFT -> {
                log.info("{}[-] LEFT clan", headerFor(memberDelta, memberDelta.getNameDelta().oldValue()));
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
            log.info("{}{}{}", header, String.format("%-24s ", field), delta);
        }
    }

    private static String consoleName(@NonNull String s) {
        String t = s.replaceAll("[^\\p{L}\\p{N} _\\-\\.'’]", "▫");
        return t.replaceAll("\\s+", " ").trim();
    }

}
