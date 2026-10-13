package com.chapeullah.guccigoblin.member.model;

import com.chapeullah.guccigoblin.builderbaseleague.BuilderBaseLeague;
import com.chapeullah.guccigoblin.clan.model.Clan;
import com.chapeullah.guccigoblin.leaguetier.LeagueTier;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity @Table(
        name = "members",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_members_tag_clan_tag",
                columnNames = {"tag", "clan_tag"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tag", nullable = false)
    private String tag;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clan_tag", nullable = false)
    private Clan clan;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "name_changed_at", nullable = true)
    private Instant nameChangedAt;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "role_changed_at", nullable = true)
    private Instant roleChangedAt;

    @Column(name = "town_hall_level", nullable = false)
    private Integer townHallLevel;

    @Column(name = "town_hall_level_changed_at", nullable = true)
    private Instant townHallLevelChangedAt;

    @Column(name = "exp_level", nullable = false)
    private Integer expLevel;

    @Column(name = "exp_level_changed_at", nullable = true)
    private Instant expLevelChangedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "league_tier_id", nullable = true)
    private LeagueTier leagueTier;

    @Column(name = "league_tier_id_changed_at", nullable = true)
    private Instant leagueTierChangedAt;

    @Column(name = "trophies", nullable = true)
    private Integer trophies;

    @Column(name = "trophies_changed_at", nullable = true)
    private Instant trophiesChangedAt;

    @Column(name = "builder_base_trophies", nullable = true)
    private Integer builderBaseTrophies;

    @Column(name = "builder_base_trophies_changed_at", nullable = true)
    private Instant builderBaseTrophiesChangedAt;

    @Column(name = "clan_rank", nullable = false)
    private Integer clanRank;

    @Column(name = "clan_rank_changed_at", nullable = true)
    private Instant clanRankChangedAt;

    @Column(name = "previous_clan_rank", nullable = true)
    private Integer previousClanRank;

    @Column(name = "previous_clan_rank_changed_at", nullable = true)
    private Instant previousClanRankChangedAt;

    @Column(name = "donations", nullable = false)
    private Integer donations;

    @Column(name = "donations_changed_at", nullable = true)
    private Instant donationsChangedAt;

    @Column(name = "total_donations", nullable = false)
    private Integer totalDonations;

    @Column(name = "total_donations_changed_at", nullable = true)
    private Instant totalDonationsChangedAt;

    @Column(name = "donations_received", nullable = false)
    private Integer donationsReceived;

    @Column(name = "dontations_received_changed_at", nullable = true)
    private Instant donationsReceivedChangedAt;

    @Column(name = "total_donations_received", nullable = false)
    private Integer totalDonationsReceived;

    @Column(name = "total_donations_received_changed_at", nullable = true)
    private Instant totalDonationsReceivedChangedAt;

    @SuppressWarnings("FieldMayBeFinal")
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "member_house_elements",
            joinColumns = @JoinColumn(
                    name = "member_id",
                    nullable = false),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_member_house_elements_member_element",
                    columnNames = {"member_id", "element_id"}))
    private List<MemberHouseElement> memberHouseElements = new ArrayList<>();

    @Column(name = "member_house_elements_changed_at", nullable = true)
    private Instant memberHouseElementsChangedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "builder_base_league_id", nullable = true)
    private BuilderBaseLeague builderBaseLeague;

    @Column(name = "builder_base_league_id_changed_at")
    private Instant builderBaseLeagueChangedAt;

    @Column(name = "in_clan", nullable = false)
    private Boolean inClan;

    @Column(name = "in_clan_changed_at", nullable = true)
    private Instant inClanChangedAt;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    public Member(
            @NonNull String tag,
            @NonNull Clan clan,
            @NonNull String name,
            @NonNull String role,
            @NonNull Integer townHallLevel,
            @NonNull Integer expLevel,
            LeagueTier leagueTier,
            Integer trophies,
            Integer builderBaseTrophies,
            @NonNull Integer clanRank,
            Integer previousClanRank,
            @NonNull Integer donations,
            @NonNull Integer donationsReceived,
            BuilderBaseLeague builderBaseLeague) {
        this.tag = tag;
        this.clan = clan;
        this.name = name;
        this.role = role;
        this.townHallLevel = townHallLevel;
        this.expLevel = expLevel;
        this.leagueTier = leagueTier;
        this.trophies = trophies;
        this.builderBaseTrophies = builderBaseTrophies;
        this.clanRank = clanRank;
        this.previousClanRank = previousClanRank;
        this.donations = donations;
        this.donationsReceived = donationsReceived;
        this.builderBaseLeague = builderBaseLeague;
        this.inClan = true;
    }

    public void addHouseElement(
            @NonNull Integer elementId,
            @NonNull String elementType) {
        memberHouseElements.add(
                new MemberHouseElement(elementId, elementType));
        this.memberHouseElementsChangedAt = Instant.now();
    }

    public void rejoin() {
        if (this.inClan) {
            throw new IllegalStateException("Member is already in clan: memberTag=\"" + tag + "\"");
        }
        this.inClan = true;
        this.inClanChangedAt = Instant.now();
    }

    public void leave() {
        if (!this.inClan) {
            throw new IllegalStateException("Member has already left clan: memberTag=\"" + tag + "\"");
        }
        this.inClan = false;
        this.inClanChangedAt = Instant.now();
    }

    public void setClan(Clan clan) {
        this.clan = clan;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNameChangedAt(Instant nameChangedAt) {
        this.nameChangedAt = nameChangedAt;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setRoleChangedAt(Instant roleChangedAt) {
        this.roleChangedAt = roleChangedAt;
    }

    public void setTownHallLevel(Integer townHallLevel) {
        this.townHallLevel = townHallLevel;
    }

    public void setTownHallLevelChangedAt(Instant townHallLevelChangedAt) {
        this.townHallLevelChangedAt = townHallLevelChangedAt;
    }

    public void setExpLevel(Integer expLevel) {
        this.expLevel = expLevel;
    }

    public void setExpLevelChangedAt(Instant expLevelChangedAt) {
        this.expLevelChangedAt = expLevelChangedAt;
    }

    public void setLeagueTier(LeagueTier leagueTier) {
        this.leagueTier = leagueTier;
    }

    public void setLeagueTierChangedAt(Instant leagueTierChangedAt) {
        this.leagueTierChangedAt = leagueTierChangedAt;
    }

    public void setTrophies(Integer trophies) {
        this.trophies = trophies;
    }

    public void setTrophiesChangedAt(Instant trophiesChangedAt) {
        this.trophiesChangedAt = trophiesChangedAt;
    }

    public void setBuilderBaseTrophies(Integer builderBaseTrophies) {
        this.builderBaseTrophies = builderBaseTrophies;
    }

    public void setBuilderBaseTrophiesChangedAt(Instant builderBaseTrophiesChangedAt) {
        this.builderBaseTrophiesChangedAt = builderBaseTrophiesChangedAt;
    }

    public void setClanRank(Integer clanRank) {
        this.clanRank = clanRank;
    }

    public void setClanRankChangedAt(Instant clanRankChangedAt) {
        this.clanRankChangedAt = clanRankChangedAt;
    }

    public void setPreviousClanRank(Integer previousClanRank) {
        this.previousClanRank = previousClanRank;
    }

    public void setPreviousClanRankChangedAt(Instant previousClanRankChangedAt) {
        this.previousClanRankChangedAt = previousClanRankChangedAt;
    }

    public void setDonations(Integer donations) {
        this.donations = donations;
    }

    public void setDonationsChangedAt(Instant donationsChangedAt) {
        this.donationsChangedAt = donationsChangedAt;
    }

    public void setTotalDonations(Integer totalDonations) {
        this.totalDonations = totalDonations;
    }

    public void setTotalDonationsChangedAt(Instant totalDonationsChangedAt) {
        this.totalDonationsChangedAt = totalDonationsChangedAt;
    }

    public void setDonationsReceived(Integer donationsReceived) {
        this.donationsReceived = donationsReceived;
    }

    public void setDonationsReceivedChangedAt(Instant donationsReceivedChangedAt) {
        this.donationsReceivedChangedAt = donationsReceivedChangedAt;
    }

    public void setTotalDonationsReceived(Integer totalDonationsReceived) {
        this.totalDonationsReceived = totalDonationsReceived;
    }

    public void setTotalDonationsReceivedChangedAt(Instant totalDonationsReceivedChangedAt) {
        this.totalDonationsReceivedChangedAt = totalDonationsReceivedChangedAt;
    }

    public void setMemberHouseElements(List<MemberHouseElement> memberHouseElements) {
        this.memberHouseElements = memberHouseElements;
    }

    public void setMemberHouseElementsChangedAt(Instant memberHouseElementsChangedAt) {
        this.memberHouseElementsChangedAt = memberHouseElementsChangedAt;
    }

    public void setBuilderBaseLeague(BuilderBaseLeague builderBaseLeague) {
        this.builderBaseLeague = builderBaseLeague;
    }

    public void setBuilderBaseLeagueChangedAt(Instant builderBaseLeagueChangedAt) {
        this.builderBaseLeagueChangedAt = builderBaseLeagueChangedAt;
    }

    public void setInClan(boolean inClan) {
        this.inClan = inClan;
    }

    public void setInClanChangedAt(Instant inClanChangedAt) {
        this.inClanChangedAt = inClanChangedAt;
    }

}
