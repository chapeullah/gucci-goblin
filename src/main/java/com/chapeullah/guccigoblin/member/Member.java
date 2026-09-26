package com.chapeullah.guccigoblin.member;

import com.chapeullah.guccigoblin.member.dto.MemberResponse;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tag",
            nullable = false,
            unique = true)
    private String tag;

    @Column(name = "in_clan", nullable = false)
    @ColumnDefault("true")
    private boolean inClan = true;

    @Column(name = "name",
            nullable = false)
    private String name;

    @Column(name = "role",
            nullable = false)
    private String role;

    @Column(name = "town_hall_level",
            nullable = false)
    private Integer townHallLevel;

    @Column(name = "exp_level",
            nullable = false)
    private Integer expLevel;

    @Column(name = "builder_base_trophies",
            nullable = false)
    private Integer builderBaseTrophies;

    @Column(name = "builder_base_league_id",
            nullable = false)
    private Integer builderBaseLeagueId;

    @Column(name = "builder_base_league_name",
            nullable = false)
    private String builderBaseLeagueName;

    @Column(name = "league_tier_id",
            nullable = false)
    private Integer leagueTierId;

    @Column(name = "league_tier_name",
            nullable = false)
    private String leagueTierName;

    @Column(name = "clan_rank",
            nullable = false)
    private Integer clanRank;

    @Column(name = "donations",
            nullable = false)
    private Integer donations;

    @Column(name = "donations_received",
            nullable = false)
    private Integer donationsReceived;

    @Column(name = "total_donations",
            nullable = false)
    private Integer totalDonations;

    @Column(name = "total_donations_received",
            nullable = false)
    private Integer totalDonationsReceived;

    @Column(name = "last_activity",
            nullable = false)
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
            @NonNull Integer builderBaseLeagueId,
            @NonNull String builderBaseLeagueName,
            @NonNull Integer leagueTierId,
            @NonNull String leagueTierName,
            @NonNull Integer clanRank,
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
        this.builderBaseLeagueId = builderBaseLeagueId;
        this.builderBaseLeagueName = builderBaseLeagueName;
        this.leagueTierId = leagueTierId;
        this.leagueTierName = leagueTierName;
        this.clanRank = clanRank;

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
            @NonNull Integer builderBaseLeagueId,
            @NonNull String builderBaseLeagueName,
            @NonNull Integer leagueTierId,
            @NonNull String leagueTierName,
            @NonNull Integer clanRank,
            @NonNull Integer donations,
            @NonNull Integer donationsReceived) {

        this.tag = tag;
        this.name = name;
        this.role = role;
        this.townHallLevel = townHallLevel;
        this.expLevel = expLevel;

        this.builderBaseTrophies = builderBaseTrophies;
        this.builderBaseLeagueId = builderBaseLeagueId;
        this.builderBaseLeagueName = builderBaseLeagueName;

        this.leagueTierId = leagueTierId;
        this.leagueTierName = leagueTierName;

        this.clanRank = clanRank;

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
                member.builderBaseLeagueId,
                member.builderBaseLeagueName,
                member.leagueTierId,
                member.leagueTierName,
                member.clanRank,
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
                member.builderBaseLeague().id(),
                member.builderBaseLeague().name(),
                member.leagueTier().id(),
                member.leagueTier().name(),
                member.clanRank(),
                member.donations(),
                member.donationsReceived());
    }

    public void leave() {
        inClan = false;
    }

    /**
     * Restores membership without counting counter changes while the player was absent.
     */
    public static Member rejoin(
            @NonNull Member oldMember,
            @NonNull Member newMember) {
        Member member = copyWithHistory(
                oldMember, newMember,
                oldMember.totalDonations, oldMember.totalDonationsReceived);
        Instant now = Instant.now();
        member.joined = now;
        member.lastActivity = now;
        return member;
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

        Member member = copyWithHistory(
                oldMember, newMember,
                oldMember.totalDonations + addedDonations,
                oldMember.totalDonationsReceived + addedReceived);

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

    private static Member copyWithHistory(
            Member oldMember,
            Member newMember,
            int totalDonations,
            int totalDonationsReceived) {
        Member member = new Member(
                newMember.tag,
                newMember.name,
                newMember.role,
                newMember.townHallLevel,
                newMember.expLevel,
                newMember.builderBaseTrophies,
                newMember.builderBaseLeagueId,
                newMember.builderBaseLeagueName,
                newMember.leagueTierId,
                newMember.leagueTierName,
                newMember.clanRank,
                newMember.donations,
                newMember.donationsReceived,
                totalDonations,
                totalDonationsReceived);

        member.id = oldMember.id;
        member.joined = oldMember.joined;
        member.lastActivity = oldMember.lastActivity;
        member.lastDonation = oldMember.lastDonation;
        member.lastDonationsReceived = oldMember.lastDonationsReceived;
        member.lastBuilderBaseTrophiesChanged =
                oldMember.lastBuilderBaseTrophiesChanged;
        member.lastTownHallUpgrade = oldMember.lastTownHallUpgrade;
        return member;
    }

}
