package com.chapeullah.guccigoblin.member.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.function.Function;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MemberDelta {

    public record Delta<T>(T oldValue, T newValue) {
        public boolean changed() {
            return !Objects.equals(oldValue, newValue);
        }
    }

    public enum MembershipStatus {
        JOINED,
        UNCHANGED,
        LEFT
    }

    private final String tag;
    private final MembershipStatus membershipStatus;
    private final Delta<String> nameDelta;
    private final Delta<String> roleDelta;
    private final Delta<Integer> townHallLevelDelta;
    private final Delta<Integer> expLevelDelta;
    private final Delta<Integer> leagueTierIdDelta;
    private final Delta<Integer> trophiesDelta;
    private final Delta<Integer> builderBaseTrophiesDelta;
    private final Delta<Integer> builderBaseLeagueIdDelta;
    private final Delta<Integer> clanRankDelta;
    private final Delta<Integer> previousClanRankDelta;
    private final Delta<Integer> donationsDelta;
    private final Delta<Integer> donationsReceivedDelta;

    public static MemberDelta merge(
            @NonNull Member oldMember,
            @NonNull Member newMember) {
        if (!oldMember.getTag().equals(newMember.getTag())) {
            throw new IllegalArgumentException(
                    "Member tags mismatch: oldMemberTag="
                            + oldMember.getTag()
                            + ", newMemberTag="
                            + newMember.getTag());
        }
        return between(MembershipStatus.UNCHANGED, oldMember, newMember);
    }

    public static MemberDelta joined(@NonNull Member newMember) {
        return between(MembershipStatus.JOINED, null, newMember);
    }

    public static MemberDelta left(@NonNull Member oldMember) {
        return between(MembershipStatus.LEFT, oldMember, null);
    }

    private static MemberDelta between(
            @NonNull MembershipStatus membershipStatus,
            Member oldMember,
            Member newMember) {
        Member identifiedMember = newMember != null ? newMember : oldMember;
        if (identifiedMember == null) {
            throw new IllegalArgumentException("Members must not be null");
        }
        return new MemberDelta(
                identifiedMember.getTag(),
                membershipStatus,
                delta(oldMember, newMember, Member::getName),
                delta(oldMember, newMember, Member::getRole),
                delta(oldMember, newMember, Member::getTownHallLevel),
                delta(oldMember, newMember, Member::getExpLevel),
                new Delta<>(
                        leagueTierId(oldMember),
                        leagueTierId(newMember)),
                delta(oldMember, newMember, Member::getTrophies),
                delta(
                        oldMember,
                        newMember,
                        Member::getBuilderBaseTrophies),
                new Delta<>(
                        builderBaseLeagueId(oldMember),
                        builderBaseLeagueId(newMember)),
                delta(oldMember, newMember, Member::getClanRank),
                delta(oldMember, newMember, Member::getPreviousClanRank),
                delta(oldMember, newMember, Member::getDonations),
                delta(oldMember, newMember, Member::getDonationsReceived));
    }

    private static <T> Delta<T> delta(
            Member oldMember,
            Member newMember,
            Function<Member, T> extractor) {
        T oldValue = oldMember == null ? null : extractor.apply(oldMember);
        T newValue = newMember == null ? null : extractor.apply(newMember);
        return new Delta<>(oldValue, newValue);
    }

    private static Integer leagueTierId(Member member) {
        if (member == null || member.getLeagueTier() == null) {
            return null;
        }
        return member.getLeagueTier().getId();
    }

    private static Integer builderBaseLeagueId(Member member) {
        if (member == null || member.getBuilderBaseLeague() == null) {
            return null;
        }
        return member.getBuilderBaseLeague().getId();
    }

    public boolean hasChanges() {
        return membershipStatus != MembershipStatus.UNCHANGED
                || nameDelta.changed()
                || roleDelta.changed()
                || townHallLevelDelta.changed()
                || expLevelDelta.changed()
                || leagueTierIdDelta.changed()
                || trophiesDelta.changed()
                || builderBaseTrophiesDelta.changed()
                || builderBaseLeagueIdDelta.changed()
                || clanRankDelta.changed()
                || previousClanRankDelta.changed()
                || donationsDelta.changed()
                || donationsReceivedDelta.changed();
    }
}