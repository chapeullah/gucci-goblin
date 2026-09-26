package com.chapeullah.guccigoblin.war.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "wars",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_wars_clans_start",
                columnNames = {"clan_tag", "opponent_tag", "starts_at"}))
@Getter @Setter
@NoArgsConstructor
public class War {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * CLAN
     */

    @Column(name = "clan_tag",
            nullable = false)
    private String clanTag;
    @Column(name = "clan_name",
            nullable = false)
    private String clanName;
    @Column(name = "clan_attacks",
            nullable = false)
    private Integer clanAttacks;
    @Column(name = "clan_stars",
            nullable = false)
    private Integer clanStars;
    @Column(name = "clan_destruction_percentage",
            nullable = false)
    private Double clanDestructionPercentage;
    @Column(name = "clan_level",
            nullable = false)
    private Integer clanLevel;

    /**
     * OPPONENT
     */

    @Column(name = "opponent_tag",
            nullable = false)
    private String opponentTag;
    @Column(name = "opponent_name",
            nullable = false)
    private String opponentName;
    @Column(name = "opponent_attacks",
            nullable = false)
    private Integer opponentAttacks;
    @Column(name = "opponent_stars",
            nullable = false)
    private Integer opponentStars;
    @Column(name = "opponent_destruction_percentage",
            nullable = false)
    private Double opponentDestructionPercentage;
    @Column(name = "opponent_level",
            nullable = false)
    private Integer opponentLevel;

    /**
     * GENERAL
     */

    @Column(name = "state",
            nullable = false)
    private String state;

    @Column(name = "starts_at",
            nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at",
            nullable = false)
    private Instant endsAt;

    @Column(name = "team_size",
            nullable = false)
    private Integer teamSize;

    @Column(name = "attacks_per_member",
            nullable = false)
    private Integer attacksPerMember;

    @CreationTimestamp
    @Column(name = "created_at",
            nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at",
            nullable = false)
    private Instant updatedAt;

    public War(
            String clanTag,
            String clanName,
            Integer clanAttacks,
            Integer clanStars,
            Double clanDestructionPercentage,
            Integer clanLevel,

            String opponentTag,
            String opponentName,
            Integer opponentAttacks,
            Integer opponentStars,
            Double opponentDestructionPercentage,
            Integer opponentLevel,

            String state,
            Instant startsAt,
            Instant endsAt,
            Integer teamSize,
            Integer attacksPerMember) {
        this.clanTag = clanTag;
        this.clanName = clanName;
        this.clanAttacks = clanAttacks;
        this.clanStars = clanStars;
        this.clanDestructionPercentage = clanDestructionPercentage;
        this.clanLevel = clanLevel;

        this.opponentTag = opponentTag;
        this.opponentName = opponentName;
        this.opponentAttacks = opponentAttacks;
        this.opponentStars = opponentStars;
        this.opponentDestructionPercentage = opponentDestructionPercentage;
        this.opponentLevel = opponentLevel;

        this.state = state;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.teamSize = teamSize;
        this.attacksPerMember = attacksPerMember;
    }

    public boolean isEnded() {
        return isEnded(Instant.now());
    }

    public boolean isEnded(Instant now) {
        return !now.isBefore(endsAt);
    }

}
