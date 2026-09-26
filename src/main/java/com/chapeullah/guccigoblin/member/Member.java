package com.chapeullah.guccigoblin.member;

import com.chapeullah.guccigoblin.member.dto.MemberResponse;
import com.chapeullah.guccigoblin.member.dto.MembersResponse;
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

    @Column(name = "builder_base_trophies", nullable = false)
    private Integer builderBaseTrophies;

    @Column(name = "donations", nullable = false)
    private Integer donations;

    @Column(name = "donations_received", nullable = false)
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
            @NonNull Integer totalDonationsReceived) {
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

    /**
     * Initialize a member from Member.
     */
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
                member.donationsReceived);

        createdMember.lastActivity = Instant.now();
        createdMember.joined = Instant.now();

        return createdMember;
    }

    /**
     * Creates a member from ClanMembersResponse.Member.
     */
    public static Member from(@NonNull MemberResponse member) {
        return new Member(
                member.tag(),
                member.name(),
                member.role(),
                member.townHallLevel(),
                member.expLevel(),
                member.builderBaseTrophies(),
                member.donations(),
                member.donationsReceived());
    }

    /**
     * Merges oldMember with newMember.
     *
     * @return merged member
     */
    public static Member merge(
            @NonNull Member oldMember,
            @NonNull Member newMember) {
        Instant now = Instant.now();

        int oldDonations = oldMember.donations;
        int newDonations = newMember.donations;
        int oldReceived = oldMember.donationsReceived;
        int newReceived = newMember.donationsReceived;

        int addedDonations = newDonations >= oldDonations
                ? newDonations - oldDonations
                : newDonations;

        int addedReceived = newReceived >= oldReceived
                ? newReceived - oldReceived
                : newReceived;

        Member member = new Member(
                newMember.tag,
                newMember.name,
                newMember.role,
                newMember.townHallLevel,
                newMember.expLevel,
                newMember.builderBaseTrophies,
                newDonations,
                newReceived,
                oldMember.totalDonations + addedDonations,
                oldMember.totalDonationsReceived + addedReceived);

        member.joined = oldMember.joined;
        member.lastActivity = oldMember.lastActivity;
        member.lastDonation = oldMember.lastDonation;
        member.lastDonationsReceived = oldMember.lastDonationsReceived;
        member.lastBuilderBaseTrophiesChanged =
                oldMember.lastBuilderBaseTrophiesChanged;
        member.lastTownHallUpgrade = oldMember.lastTownHallUpgrade;

        boolean isActive = false;

        if (addedDonations > 0) {
            member.lastDonation = now;
            isActive = true;
        }

        if (addedReceived > 0) {
            member.lastDonationsReceived = now;
            isActive = true;
        }

        if (!Objects.equals(
                oldMember.builderBaseTrophies,
                newMember.builderBaseTrophies)) {
            member.lastBuilderBaseTrophiesChanged = now;
            isActive = true;
        }

        if (!Objects.equals(
                oldMember.townHallLevel,
                newMember.townHallLevel)) {
            member.lastTownHallUpgrade = now;
        }

        if (isActive) {
            member.lastActivity = now;
        }

        return member;
    }


}
