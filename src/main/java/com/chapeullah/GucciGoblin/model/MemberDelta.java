package com.chapeullah.GucciGoblin.model;

import lombok.Getter;
import lombok.NonNull;

import java.util.Objects;

@Getter
public class MemberDelta {

    public static final class Delta<T> {
        private final T oldValue;
        private final T newValue;

        public Delta(T oldValue, T newValue) {
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        public T oldValue() { return oldValue; }
        public T newValue() { return newValue; }

        public boolean changed() {
            return !Objects.equals(oldValue, newValue);
        }

        @Override
        public String toString() {
            if (changed()) {
                return String.valueOf(oldValue) + " -> " + String.valueOf(newValue);
            }
            return String.valueOf(newValue);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Delta<?> d)) return false;
            return Objects.equals(oldValue, d.oldValue) && Objects.equals(newValue, d.newValue);
        }

        @Override
        public int hashCode() {
            return Objects.hash(oldValue, newValue);
        }
    }


    /**
     * JOINED, NO_CHANGE, LEFT
     */
    public enum MembershipStatus { JOINED, NO_CHANGE, LEFT }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MemberDelta other)) return false;
        return Objects.equals(tag, other.tag)
                && membershipStatus == other.membershipStatus
                && Objects.equals(nameDelta, other.nameDelta)
                && Objects.equals(roleDelta, other.roleDelta)
                && Objects.equals(townHallLevelDelta, other.townHallLevelDelta)
                && Objects.equals(expLevelDelta, other.expLevelDelta)
                && Objects.equals(builderBaseTrophiesDelta, other.builderBaseTrophiesDelta)
                && Objects.equals(donationsDelta, other.donationsDelta)
                && Objects.equals(donationsReceivedDelta, other.donationsReceivedDelta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                tag,
                membershipStatus,
                nameDelta,
                roleDelta,
                townHallLevelDelta,
                expLevelDelta,
                builderBaseTrophiesDelta,
                donationsDelta,
                donationsReceivedDelta
        );
    }


    private final String tag;

    private final MembershipStatus membershipStatus;

    private final Delta<String> nameDelta;
    private final Delta<String> roleDelta;
    private final Delta<Integer> townHallLevelDelta;
    private final Delta<Integer> expLevelDelta;
    private final Delta<Integer> builderBaseTrophiesDelta;
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
            @NonNull Delta<Integer> donationsDelta,
            @NonNull Delta<Integer> donationsReceivedDelta
    ) {
        this.tag = tag;
        this.membershipStatus = membershipStatus;
        this.nameDelta = nameDelta;
        this.roleDelta = roleDelta;
        this.townHallLevelDelta = townHallLevelDelta;
        this.expLevelDelta = expLevelDelta;
        this.builderBaseTrophiesDelta = builderBaseTrophiesDelta;
        this.donationsDelta = donationsDelta;
        this.donationsReceivedDelta = donationsReceivedDelta;
    }

    public static MemberDelta merge(@NonNull Member oldMember, @NonNull Member newMember) {
        if (!oldMember.equals(newMember)) {
            throw new IllegalArgumentException(
                    "Cannot compare different members: oldTag=" + oldMember.getTag() + ", newTag=" + newMember.getTag()
            );
        }
        return new MemberDelta(
                newMember.getTag(),

                MembershipStatus.NO_CHANGE,

                new Delta<>(oldMember.getName(), newMember.getName()),
                new Delta<>(oldMember.getRole(), newMember.getRole()),
                new Delta<>(oldMember.getTownHallLevel(), newMember.getTownHallLevel()),
                new Delta<>(oldMember.getExpLevel(), newMember.getExpLevel()),
                new Delta<>(oldMember.getBuilderBaseTrophies(), newMember.getBuilderBaseTrophies()),
                new Delta<>(oldMember.getDonations(), newMember.getDonations()),
                new Delta<>(oldMember.getDonationsReceived(), newMember.getDonationsReceived())
        );
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
                new Delta<>(null, newMember.getDonations()),
                new Delta<>(null, newMember.getDonationsReceived())
        );
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
                new Delta<>(oldMember.getDonations(), null),
                new Delta<>(oldMember.getDonationsReceived(), null)
        );
    }

    public boolean hasChanges() {
        return membershipStatus != MembershipStatus.NO_CHANGE
                || nameDelta.changed()
                || roleDelta.changed()
                || townHallLevelDelta.changed()
                || expLevelDelta.changed()
                || builderBaseTrophiesDelta.changed()
                || donationsDelta.changed()
                || donationsReceivedDelta.changed();
    }

}
