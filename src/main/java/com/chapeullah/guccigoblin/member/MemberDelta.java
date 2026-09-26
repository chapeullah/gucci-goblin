package com.chapeullah.guccigoblin.member;

import lombok.Getter;
import lombok.NonNull;

import java.util.Objects;

@Getter
public class MemberDelta {

    public record Delta<T>(T oldValue, T newValue) {

        public boolean changed() {
            return !Objects.equals(oldValue, newValue);
        }

    }

    /**
     * JOINED, UNCHANGED, LEFT
     */
    public enum MembershipStatus { JOINED, UNCHANGED, LEFT }

    private final String tag;

    private final MembershipStatus membershipStatus;

    private final Delta<String> nameDelta;
    private final Delta<String> roleDelta;
    private final Delta<Integer> townHallLevelDelta;
    private final Delta<Integer> expLevelDelta;
    private final Delta<Integer> builderBaseTrophiesDelta;
    private final Delta<Integer> builderBaseLeagueIdDelta;
    private final Delta<String> builderBaseLeagueNameDelta;
    private final Delta<Integer> leagueTierIdDelta;
    private final Delta<String> leagueTierNameDelta;
    private final Delta<Integer> clanRankDelta;
    private final Delta<Integer> donationsDelta;
    private final Delta<Integer> donationsReceivedDelta;

    private MemberDelta(
            @NonNull String tag,
            @NonNull MembershipStatus membershipStatus,
            @NonNull Delta<String> nameDelta,
            @NonNull Delta<String> roleDelta,
            @NonNull Delta<Integer> townHallLevelDelta,
            @NonNull Delta<Integer> expLevelDelta,
            @NonNull Delta<Integer> builderBaseTrophiesDelta,
            @NonNull Delta<Integer> builderBaseLeagueIdDelta,
            @NonNull Delta<String> builderBaseLeagueNameDelta,
            @NonNull Delta<Integer> leagueTierIdDelta,
            @NonNull Delta<String> leagueTierNameDelta,
            @NonNull Delta<Integer> clanRankDelta,
            @NonNull Delta<Integer> donationsDelta,
            @NonNull Delta<Integer> donationsReceivedDelta) {
        this.tag = tag;
        this.membershipStatus = membershipStatus;
        this.nameDelta = nameDelta;
        this.roleDelta = roleDelta;
        this.townHallLevelDelta = townHallLevelDelta;
        this.expLevelDelta = expLevelDelta;
        this.builderBaseTrophiesDelta = builderBaseTrophiesDelta;
        this.builderBaseLeagueIdDelta = builderBaseLeagueIdDelta;
        this.builderBaseLeagueNameDelta = builderBaseLeagueNameDelta;
        this.leagueTierIdDelta = leagueTierIdDelta;
        this.leagueTierNameDelta = leagueTierNameDelta;
        this.clanRankDelta = clanRankDelta;
        this.donationsDelta = donationsDelta;
        this.donationsReceivedDelta = donationsReceivedDelta;
    }

    public static MemberDelta merge(@NonNull Member oldMember, @NonNull Member newMember) {
        if (!Objects.equals(oldMember.getTag(), newMember.getTag())) {
            throw new IllegalArgumentException(
                    "Cannot compare different members: oldTag=" +
                            oldMember.getTag() + ", newTag=" + newMember.getTag());
        }
        return new MemberDelta(
                newMember.getTag(),
                MembershipStatus.UNCHANGED,
                new Delta<>(oldMember.getName(), newMember.getName()),
                new Delta<>(oldMember.getRole(), newMember.getRole()),
                new Delta<>(oldMember.getTownHallLevel(), newMember.getTownHallLevel()),
                new Delta<>(oldMember.getExpLevel(), newMember.getExpLevel()),
                new Delta<>(oldMember.getBuilderBaseTrophies(), newMember.getBuilderBaseTrophies()),
                new Delta<>(oldMember.getBuilderBaseLeagueId(), newMember.getBuilderBaseLeagueId()),
                new Delta<>(oldMember.getBuilderBaseLeagueName(), newMember.getBuilderBaseLeagueName()),
                new Delta<>(oldMember.getLeagueTierId(), newMember.getLeagueTierId()),
                new Delta<>(oldMember.getLeagueTierName(), newMember.getLeagueTierName()),
                new Delta<>(oldMember.getClanRank(), newMember.getClanRank()),
                new Delta<>(oldMember.getDonations(), newMember.getDonations()),
                new Delta<>(oldMember.getDonationsReceived(), newMember.getDonationsReceived()));
    }

    public static MemberDelta joined(@NonNull Member newMember) {
        return new MemberDelta(
                newMember.getTag(),
                MembershipStatus.JOINED,
                new Delta<>(null, newMember.getName()),
                new Delta<>(null, newMember.getRole()),
                new Delta<>(null, newMember.getTownHallLevel()),
                new Delta<>(null, newMember.getExpLevel()),
                new Delta<>(null, newMember.getBuilderBaseTrophies()),
                new Delta<>(null, newMember.getBuilderBaseLeagueId()),
                new Delta<>(null, newMember.getBuilderBaseLeagueName()),
                new Delta<>(null, newMember.getLeagueTierId()),
                new Delta<>(null, newMember.getLeagueTierName()),
                new Delta<>(null, newMember.getClanRank()),
                new Delta<>(null, newMember.getDonations()),
                new Delta<>(null, newMember.getDonationsReceived()));
    }

    public static MemberDelta left(@NonNull Member oldMember) {
        return new MemberDelta(
                oldMember.getTag(),
                MembershipStatus.LEFT,
                new Delta<>(oldMember.getName(), null),
                new Delta<>(oldMember.getRole(), null),
                new Delta<>(oldMember.getTownHallLevel(), null),
                new Delta<>(oldMember.getExpLevel(), null),
                new Delta<>(oldMember.getBuilderBaseTrophies(), null),
                new Delta<>(oldMember.getBuilderBaseLeagueId(), null),
                new Delta<>(oldMember.getBuilderBaseLeagueName(), null),
                new Delta<>(oldMember.getLeagueTierId(), null),
                new Delta<>(oldMember.getLeagueTierName(), null),
                new Delta<>(oldMember.getClanRank(), null),
                new Delta<>(oldMember.getDonations(), null),
                new Delta<>(oldMember.getDonationsReceived(), null));
    }

    public boolean hasChanges() {
        return membershipStatus != MembershipStatus.UNCHANGED
                || nameDelta.changed()
                || roleDelta.changed()
                || townHallLevelDelta.changed()
                || expLevelDelta.changed()
                || builderBaseTrophiesDelta.changed()
                || builderBaseLeagueIdDelta.changed()
                || builderBaseLeagueNameDelta.changed()
                || leagueTierIdDelta.changed()
                || leagueTierNameDelta.changed()
                || clanRankDelta.changed()
                || donationsDelta.changed()
                || donationsReceivedDelta.changed();
    }

}
