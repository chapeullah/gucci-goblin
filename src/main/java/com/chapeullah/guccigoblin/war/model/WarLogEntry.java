package com.chapeullah.guccigoblin.war.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.Instant;
import java.util.Objects;

@Entity @Table(
        name = "war_log",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_war_log_clan_tag_end_time",
                columnNames = {"clan_tag", "end_time"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WarLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * GENERAL
     */

    @Column(name = "result", nullable = true)
    private String result;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Column(name = "team_size", nullable = true)
    private Integer teamSize;

    @Column(name = "attacks_per_member", nullable = true)
    private Integer attacksPerMember;

    @Column(name = "battle_modifier", nullable = true)
    private String battleModifier;

    /**
     * CLAN
     */

    @Column(name = "clan_tag", nullable = false)
    private String clanTag;

    @Column(name = "clan_name", nullable = true)
    private String clanName;

    @Column(name = "clan_level", nullable = true)
    private Integer clanLevel;

    @Column(name = "clan_attacks", nullable = true)
    private Integer clanAttacks;

    @Column(name = "clan_stars", nullable = true)
    private Integer clanStars;

    @Column(name = "clan_destruction_percentage", nullable = true)
    private Double clanDestructionPercentage;

    @Column(name = "clan_exp_earned")
    private Integer clanExpEarned;

    @Embedded
    private WarLogEntryClanBadgeUrls clanBadgeUrls;

    /**
     * OPPONENT
     */

    @Column(name = "opponent_clan_tag", nullable = true)
    private String opponentClanTag;

    @Column(name = "opponent_clan_name", nullable = true)
    private String opponentClanName;

    @Column(name = "opponent_clan_level", nullable = true)
    private Integer opponentClanLevel;

    @Column(name = "opponent_clan_stars", nullable = true)
    private Integer opponentClanStars;

    @Column(name = "opponent_clan_destruction_percentage", nullable = true)
    private Double opponentClanDestructionPercentage;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "small", column = @Column(name = "opponent_clan_badge_small_url", length = 512)),
            @AttributeOverride(name = "large", column = @Column(name = "opponent_clan_badge_large_url", length = 512)),
            @AttributeOverride(name = "medium", column = @Column(name = "opponent_clan_badge_medium_url", length = 512))
    })
    private WarLogEntryClanBadgeUrls opponentClanBadgeUrls;

    public WarLogEntry(
            String result,
            @NonNull Instant endTime,
            Integer teamSize,
            Integer attacksPerMember,
            String battleModifier,

            @NonNull String clanTag,
            String clanName,
            Integer clanLevel,
            Integer clanAttacks,
            Integer clanStars,
            Double clanDestructionPercentage,
            Integer clanExpEarned,
            WarLogEntryClanBadgeUrls clanBadgeUrls,

            String opponentClanTag,
            String opponentClanName,
            Integer opponentClanLevel,
            Integer opponentClanStars,
            Double opponentClanDestructionPercentage,
            WarLogEntryClanBadgeUrls opponentClanBadgeUrls) {
        this.result = result;
        this.endTime = endTime;
        this.teamSize = teamSize;
        this.attacksPerMember = attacksPerMember;
        this.battleModifier = battleModifier;

        this.clanTag = clanTag;
        this.clanName = clanName;
        this.clanLevel = clanLevel;
        this.clanAttacks = clanAttacks;
        this.clanStars = clanStars;
        this.clanDestructionPercentage = clanDestructionPercentage;
        this.clanExpEarned = clanExpEarned;
        this.clanBadgeUrls = clanBadgeUrls;

        this.opponentClanTag = opponentClanTag;
        this.opponentClanName = opponentClanName;
        this.opponentClanLevel = opponentClanLevel;
        this.opponentClanStars = opponentClanStars;
        this.opponentClanDestructionPercentage = opponentClanDestructionPercentage;
        this.opponentClanBadgeUrls = opponentClanBadgeUrls;
    }

    public boolean updateFrom(@NonNull WarLogEntry incoming) {
        if (!Objects.equals(this.clanTag, incoming.getClanTag())
                || !Objects.equals(this.endTime, incoming.getEndTime())) {
            throw new IllegalArgumentException("Cannot update a different war log entry");
        }

        boolean unchanged = Objects.equals(this.result, incoming.getResult())
                && Objects.equals(this.teamSize, incoming.getTeamSize())
                && Objects.equals(this.attacksPerMember, incoming.getAttacksPerMember())
                && Objects.equals(this.battleModifier, incoming.getBattleModifier())

                && Objects.equals(this.clanName, incoming.getClanName())
                && Objects.equals(this.clanLevel, incoming.getClanLevel())
                && Objects.equals(this.clanAttacks, incoming.getClanAttacks())
                && Objects.equals(this.clanStars, incoming.getClanStars())
                && Objects.equals(this.clanDestructionPercentage, incoming.getClanDestructionPercentage())
                && Objects.equals(this.clanExpEarned, incoming.getClanExpEarned())
                && Objects.equals(this.clanBadgeUrls, incoming.getClanBadgeUrls())

                && Objects.equals(this.opponentClanTag, incoming.getOpponentClanTag())
                && Objects.equals(this.opponentClanName, incoming.getOpponentClanName())
                && Objects.equals(this.opponentClanLevel, incoming.getOpponentClanLevel())
                && Objects.equals(this.opponentClanStars, incoming.getOpponentClanStars())
                && Objects.equals(this.opponentClanDestructionPercentage, incoming.getOpponentClanDestructionPercentage())
                && Objects.equals(this.opponentClanBadgeUrls, incoming.getOpponentClanBadgeUrls());

        if (unchanged) {
            return false;
        }

        this.result = incoming.getResult();
        this.teamSize = incoming.getTeamSize();
        this.attacksPerMember = incoming.getAttacksPerMember();
        this.battleModifier = incoming.getBattleModifier();

        this.clanName = incoming.getClanName();
        this.clanLevel = incoming.getClanLevel();
        this.clanAttacks = incoming.getClanAttacks();
        this.clanStars = incoming.getClanStars();
        this.clanDestructionPercentage = incoming.getClanDestructionPercentage();
        this.clanExpEarned = incoming.getClanExpEarned();
        this.clanBadgeUrls = incoming.getClanBadgeUrls();

        this.opponentClanTag = incoming.getOpponentClanTag();
        this.opponentClanName = incoming.getOpponentClanName();
        this.opponentClanLevel = incoming.getOpponentClanLevel();
        this.opponentClanStars = incoming.getOpponentClanStars();
        this.opponentClanDestructionPercentage =
                incoming.getOpponentClanDestructionPercentage();
        this.opponentClanBadgeUrls = incoming.getOpponentClanBadgeUrls();

        return true;
    }

}
