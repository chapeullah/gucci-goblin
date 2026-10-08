package com.chapeullah.guccigoblin.member.model;

import com.chapeullah.guccigoblin.builderbaseleague.BuilderBaseLeague;
import com.chapeullah.guccigoblin.clan.model.Clan;
import com.chapeullah.guccigoblin.leaguetier.LeagueTier;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

@Entity @Table(
        name = "members",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_members_tag_clan_tag",
                columnNames = {"clan_tag", "tag"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tag",
            nullable = false)
    private String tag;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clan_tag", nullable = false)
    private Clan clan;

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

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = true)
    @JoinColumn(
            name = "league_tier_id",
            nullable = true)
    private LeagueTier leagueTier;

    @Column(name = "trophies", nullable = true)
    private Integer trophies;

    @Column(name = "builder_base_trophies", nullable = true)
    private Integer builderBaseTrophies;

    @Column(name = "clan_rank", nullable = false)
    private Integer clanRank;

    @Column(name = "previous_clan_rank", nullable = true)
    private Integer previousClanRank;

    @Column(name = "donations",
            nullable = false)
    private Integer donations;

    @Column(name = "donations_received",
            nullable = false)
    private Integer donationsReceived;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "builder_base_league_id", nullable = true)
    private BuilderBaseLeague builderBaseLeague;

    @Column(name = "in_clan", nullable = false)
    private boolean inClan;

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
        if (tag.isBlank()) {
            throw new IllegalArgumentException("Member tag must not be blank");
        }
        if (name.isBlank()) {
            throw new IllegalArgumentException("Member name must not be blank");
        }
        if (role.isBlank()) {
            throw new IllegalArgumentException("Member role must not be blank");
        }
        if (townHallLevel < 1) {
            throw new IllegalArgumentException("Member town hall level must be positive");
        }
        if (expLevel < 1) {
            throw new IllegalArgumentException("Member experience level must be positive");
        }
        if (trophies != null && trophies < 0) {
            throw new IllegalArgumentException("Member trophies must not be negative");
        }
        if (builderBaseTrophies != null && builderBaseTrophies < 0) {
            throw new IllegalArgumentException("Member builder base trophies must not be negative");
        }
        if (clanRank < 1 || clanRank > 50) {
            throw new IllegalArgumentException("Member clan rank must be between 1 and 50");
        }
        if (previousClanRank != null && (previousClanRank < 1 || previousClanRank > 50)) {
            throw new IllegalArgumentException("Previous member clan rank must be between 1 and 50");
        }
        if (donations < 0) {
            throw new IllegalArgumentException("Member donations must not be negative");
        }
        if (donationsReceived < 0) {
            throw new IllegalArgumentException("Member received donations must not be negative");
        }
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

    public void updateFrom(@NonNull Member source) {
        if (!tag.equals(source.tag)) {
            throw new IllegalArgumentException("Member tags mismatch: currentMemberTag=" + tag + ", sourceMemberTag=" + source.tag);
        }
        if (!clan.getTag().equals(source.clan.getTag())) {
            throw new IllegalArgumentException(
                    "Member clan tags mismatch: currentClanTag="
                            + clan.getTag()
                            + ", sourceClanTag="
                            + source.clan.getTag());
        }
        this.name = source.name;
        this.role = source.role;
        this.townHallLevel = source.townHallLevel;
        this.expLevel = source.expLevel;
        this.leagueTier = source.leagueTier;
        this.trophies = source.trophies;
        this.builderBaseTrophies = source.builderBaseTrophies;
        this.clanRank = source.clanRank;
        this.previousClanRank = source.previousClanRank;
        this.donations = source.donations;
        this.donationsReceived = source.donationsReceived;
        this.builderBaseLeague = source.builderBaseLeague;
    }

    public void addHouseElement(
            @NonNull Integer elementId,
            @NonNull String elementType) {
        memberHouseElements.add(
                new MemberHouseElement(elementId, elementType));
    }

    public void rejoin() {
        if (this.inClan) {
            throw new IllegalStateException("Member is already in clan: memberTag=" + tag);
        }
        this.inClan = true;
    }

    public void leave() {
        if (!this.inClan) {
            throw new IllegalStateException("Member has already left clan: memberTag=" + tag);
        }
        this.inClan = false;
    }

}
