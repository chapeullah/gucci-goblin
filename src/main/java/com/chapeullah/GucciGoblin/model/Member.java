package com.chapeullah.GucciGoblin.model;

import com.chapeullah.GucciGoblin.dto.ClanMembersResponse;
import com.chapeullah.GucciGoblin.entity.MemberEntity;
import lombok.Getter;
import lombok.NonNull;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain model for a clan member.
 */
@Getter
public class Member {

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Member other)) return false;
        return Objects.equals(this.tag, other.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag);
    }

    private final String tag;
    private final String name;
    private final String role;
    private final Integer townHallLevel;
    private final Integer expLevel;
    private final Integer builderBaseTrophies;
    private final Integer donations;
    private final Integer donationsReceived;

    private final Integer totalDonations;
    private final Integer totalDonationsReceived;

    private Instant lastActivity;
    private Instant lastDonation;
    private Instant lastDonationsReceived;
    private Instant lastBuilderBaseTrophiesChanged;
    private Instant lastTownHallUpgrade;

    private Instant joined;

    /**
     * Constructs a member from snapshots.
     */
    private Member(
            @NonNull String tag,
            @NonNull String name,
            @NonNull String role,
            @NonNull Integer townHallLevel,
            @NonNull Integer expLevel,
            @NonNull Integer builderBaseTrophies,
            @NonNull Integer donations,
            @NonNull Integer donationsReceived,
            @NonNull Integer totalDonations,
            @NonNull Integer totalDonationsReceived
    ) {
        this.tag = tag;
        this.name = name;
        this.role = role;
        this.townHallLevel = townHallLevel;
        this.expLevel = expLevel;
        this.builderBaseTrophies = builderBaseTrophies;

        this.donations = donations;
        this.donationsReceived = donationsReceived;
        this.totalDonations = totalDonations;
        this.totalDonationsReceived = totalDonationsReceived;
    }

    /**
     * Constructs a member from factory.
     */
    private Member(
            @NonNull String tag,
            @NonNull String name,
            @NonNull String role,
            @NonNull Integer townHallLevel,
            @NonNull Integer expLevel,
            @NonNull Integer builderBaseTrophies,
            @NonNull Integer donations,
            @NonNull Integer donationsReceived
    ) {
        this.tag = tag;
        this.name = name;
        this.role = role;
        this.townHallLevel = townHallLevel;
        this.expLevel = expLevel;
        this.builderBaseTrophies = builderBaseTrophies;

        this.donations = donations;
        this.donationsReceived = donationsReceived;

        this.totalDonations = 0;
        this.totalDonationsReceived = 0;
    }

    public static Member initFrom(@NonNull Member member) {
        Member created = new Member(
                member.tag,
                member.name,
                member.role,
                member.townHallLevel,
                member.expLevel,
                member.builderBaseTrophies,
                member.donations,
                member.donationsReceived,
                member.donations,
                member.donationsReceived
        );

        created.lastActivity = Instant.now();
        created.joined = Instant.now();

        return created;
    }

    /**
     * Creates a member from ClanMembersResponse.Member.
     */
    public static Member from(@NonNull ClanMembersResponse.Member member) {
        return new Member(
                member.tag(),
                member.name(),
                member.role(),
                member.townHallLevel(),
                member.expLevel(),
                member.builderBaseTrophies(),
                member.donations(),
                member.donationsReceived()
        );
    }

    /**
     * Creates a member from MemberEntity.
     */
    public static Member from(@NonNull MemberEntity entity) {
        Member member = new Member(
                entity.getTag(),
                entity.getName(),
                entity.getRole(),
                entity.getTownHallLevel(),
                entity.getExpLevel(),
                entity.getBuilderBaseTrophies(),
                entity.getDonations(),
                entity.getDonationsReceived(),
                entity.getTotalDonations(),
                entity.getTotalDonationsReceived()
        );

        member.lastActivity = entity.getLastActivity();
        member.lastDonation = entity.getLastDonation();
        member.lastDonationsReceived = entity.getLastDonationsReceived();
        member.lastBuilderBaseTrophiesChanged = entity.getLastBuilderBaseTrophiesChanged();
        member.lastTownHallUpgrade = entity.getLastTownHallUpgrade();

        member.joined = entity.getJoined();

        return member;
    }

    /**
     * Merges oldMember with newMember.
     *
     * @return merged member
     */
    public static Member merge(@NonNull Member oldMember, @NonNull Member newMember) {
        Instant now = Instant.now();
        boolean isActive = false;

        String tag = newMember.tag;
        String name = newMember.name;
        String role = newMember.role;
        Integer townHallLevel = newMember.townHallLevel;
        Integer expLevel = newMember.expLevel;

        Integer builderBaseTrophies = newMember.builderBaseTrophies;
        Integer donations = newMember.donations;
        Integer donationsReceived = newMember.donationsReceived;

        int oldDon = oldMember.getDonations();
        int newDon = newMember.getDonations();
        int oldRec = oldMember.getDonationsReceived();
        int newRec = newMember.getDonationsReceived();

        int totalDon = oldMember.getTotalDonations();
        int totalRec = oldMember.getTotalDonationsReceived();

        int deltaDon = newDon - oldDon;
        int deltaRec = newRec - oldRec;

        totalDon += (deltaDon >= 0) ? deltaDon : newDon;
        totalRec += (deltaRec >= 0) ? deltaRec : newRec;

        Integer totalDonations = totalDon;
        Integer totalDonationsReceived = totalRec;

        Instant lastActivity = oldMember.lastActivity;
        Instant lastDonation = oldMember.lastDonation;
        Instant lastDonationsReceived = oldMember.lastDonationsReceived;
        Instant lastBuilderBaseTrophiesChanged = oldMember.lastBuilderBaseTrophiesChanged;
        Instant lastTownHallUpgrade = oldMember.lastTownHallUpgrade;

        if (!Objects.equals(oldMember.donations, newMember.donations)) {
            lastDonation = now;
            isActive = true;
        }
        if (!Objects.equals(oldMember.donationsReceived, newMember.donationsReceived)) {
            lastDonationsReceived = now;
            isActive = true;
        }
        if (!Objects.equals(oldMember.builderBaseTrophies, newMember.builderBaseTrophies)) {
            lastBuilderBaseTrophiesChanged = now;
            isActive = true;
        }
        if (isActive) lastActivity = now;
        if (!Objects.equals(oldMember.townHallLevel, newMember.townHallLevel)) {
            lastTownHallUpgrade = now;
        }

        Member member = new Member(
                tag,
                name,
                role,
                townHallLevel,
                expLevel,
                builderBaseTrophies,
                donations,
                donationsReceived,
                totalDonations,
                totalDonationsReceived
        );

        member.lastActivity = lastActivity;
        member.lastDonation = lastDonation;
        member.lastDonationsReceived = lastDonationsReceived;
        member.lastBuilderBaseTrophiesChanged = lastBuilderBaseTrophiesChanged;
        member.lastTownHallUpgrade = lastTownHallUpgrade;

        member.joined = oldMember.joined;

        return member;
    }


}
