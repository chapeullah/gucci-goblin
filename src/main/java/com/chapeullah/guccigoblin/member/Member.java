package com.chapeullah.guccigoblin.member;

import com.chapeullah.guccigoblin.clan.Clan;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity @Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clan_tag", nullable = false)
    private Clan clan;

    @Id
    @Column(name = "tag",
            nullable = false)
    private String tag;

    @Column(name = "name",
            nullable = false)
    private String name;

    @Column(name = "role",
            nullable = false)
    private String role;

    @Column(name = "clan_rank",
            nullable = false)
    private Integer clanRank;

    @Column(name = "previous_clan_rank",
            nullable = false)
    private Integer previousClanRank;

    @Column(name = "donations",
            nullable = false)
    private Integer donations;

    @Column(name = "donations_received",
            nullable = false)
    private Integer donationsReceived;

    public Member(
            @NonNull Clan clan,
            @NonNull String tag,
            @NonNull String name,
            @NonNull String role,
            @NonNull Integer clanRank,
            @NonNull Integer previousClanRank,
            @NonNull Integer donations,
            @NonNull Integer donationsReceived) {
        if (tag.isBlank()) {
            throw new IllegalArgumentException("Clan members tag must not be blank");
        }
        if (name.isBlank()) {
            throw new IllegalArgumentException("Clan members name must not be blank");
        }
        if (role.isBlank()) {
            throw new IllegalArgumentException("Clan members role must not be blank");
        }
        if (clanRank < 1 || clanRank > 50) {
            throw new IllegalArgumentException("Member clan rank must be between 1 and 50");
        }
        if (previousClanRank < 1 || previousClanRank > 50) {
            throw new IllegalArgumentException("Previous member clan rank must be between 1 and 50");
        }
        if (donations < 0) {
            throw new IllegalArgumentException("Member donations must not be negative");
        }
        if (donationsReceived < 0) {
            throw new IllegalArgumentException("Member received donations must not be negative");
        }

        this.clan = clan;
        this.tag = tag;
        this.name = name;
        this.role = role;
        this.clanRank = clanRank;
        this.previousClanRank = previousClanRank;
        this.donations = donations;
        this.donationsReceived = donationsReceived;
    }

}
