package com.chapeullah.GucciGoblin.entity;

import com.chapeullah.GucciGoblin.model.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "members")
@Getter @Setter
@NoArgsConstructor
public class MemberEntity {

    @Id
    @Column(name="tag",
            nullable = false)
    private String tag;

    @Column(name="name",
            nullable = false)
    private String name;

    @Column(name="role",
            nullable = false)
    private String role;

    @Column(name="town_hall_level",
            nullable = false)
    private Integer townHallLevel;

    @Column(name="exp_level",
            nullable = false)
    private Integer expLevel;

    @Column(name="builder_base_trophies",
            nullable = true)
    private Integer builderBaseTrophies;

    @Column(name="donations",
            nullable = true)
    private Integer donations;

    @Column(name="donations_received",
            nullable = true)
    private Integer donationsReceived;

    @Column(name="last_activity",
            nullable = false)
    private Instant lastActivity;

    @Column(name="last_donation",
            nullable = true)
    private Instant lastDonation;

    @Column(name="last_donations_received",
            nullable = true)
    private Instant lastDonationsReceived;

    @Column(name="last_builder_base_trophies_changed",
            nullable = true)
    private Instant lastBuilderBaseTrophiesChanged;

    @Column(name="last_town_hall_upgrade",
            nullable = true)
    private Instant lastTownHallUpgrade;

    @Column(name="total_donations",
            nullable = false)
    private Integer totalDonations;

    @Column(name="total_donations_received",
            nullable = false)
    private Integer totalDonationsReceived;

    @Column(name="joined",
            nullable = false)
    private Instant joined;

    private MemberEntity(
            @NonNull String tag,
            @NonNull String name,
            @NonNull String role,
            @NonNull Integer townHallLevel,
            @NonNull Integer expLevel,
            @NonNull Integer builderBaseTrophies,
            @NonNull Integer donations,
            @NonNull Integer donationsReceived,
            @NonNull Integer totalDonations,
            @NonNull Integer totalDonationsReceived,
            @NonNull Instant lastActivity,
            Instant lastDonation,
            Instant lastDonationsReceived,
            Instant lastBuilderBaseTrophiesChanged,
            Instant lastTownHallUpgrade,
            @NonNull Instant joined
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
        this.lastActivity = lastActivity;
        this.lastDonation = lastDonation;
        this.lastDonationsReceived = lastDonationsReceived;
        this.lastBuilderBaseTrophiesChanged = lastBuilderBaseTrophiesChanged;
        this.lastTownHallUpgrade = lastTownHallUpgrade;
        this.joined = joined;
    }

    public static MemberEntity from(@NonNull Member member) {
        return new MemberEntity(
                member.getTag(),
                member.getName(),
                member.getRole(),
                member.getTownHallLevel(),
                member.getExpLevel(),
                member.getBuilderBaseTrophies(),
                member.getDonations(),
                member.getDonationsReceived(),
                member.getTotalDonations(),
                member.getTotalDonationsReceived(),
                member.getLastActivity(),
                member.getLastDonation(),
                member.getLastDonationsReceived(),
                member.getLastBuilderBaseTrophiesChanged(),
                member.getLastTownHallUpgrade(),
                member.getJoined()
        );
    }

}
