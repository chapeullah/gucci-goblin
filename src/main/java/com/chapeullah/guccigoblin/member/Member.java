package com.chapeullah.guccigoblin.member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Member other)) return false;
        return getTag() != null && Objects.equals(getTag(), other.getTag());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTag());
    }

    @Id
    @Column(name = "tag", nullable = false)
    private String tag;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "town_hall_level", nullable = false)
    private Integer townHallLevel;

    @Column(name = "exp_level", nullable = false)
    private Integer expLevel;

    @Column(name = "builder_base_trophies")
    private Integer builderBaseTrophies;

    @Column(name = "donations")
    private Integer donations;

    @Column(name = "donations_received")
    private Integer donationsReceived;

    @Column(name = "total_donations", nullable = false)
    private Integer totalDonations;

    @Column(name = "total_donations_received", nullable = false)
    private Integer totalDonationsReceived;

    @Column(name = "last_activity", nullable = false)
    private Instant lastActivity;

    @Column(name = "last_donation")
    private Instant lastDonation;

    @Column(name = "last_donations_received")
    private Instant lastDonationsReceived;

    @Column(name = "last_builder_base_trophies_changed")
    private Instant lastBuilderBaseTrophiesChanged;

    @Column(name = "last_town_hall_upgrade")
    private Instant lastTownHallUpgrade;

    @Column(name = "joined", nullable = false)
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
        Member createdMember = new Member(
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

        createdMember.lastActivity = Instant.now();
        createdMember.joined = Instant.now();

        return createdMember;
    }

    /**
     * Creates a member from ClanMembersResponse.Member.
     */
    public static Member from(@NonNull MembersResponse.Member member) {
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
