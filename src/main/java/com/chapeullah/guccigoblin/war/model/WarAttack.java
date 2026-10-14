package com.chapeullah.guccigoblin.war.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "war_attacks",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_war_attack_war_order",
                columnNames = {"war_id", "attack_order"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WarAttack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "war_id", nullable = false)
    private War war;

    @Column(name = "attacker_tag", nullable = false)
    private String attackerTag;

    @Column(name = "attacker_name", nullable = false)
    private String attackerName;

    @Column(name = "attacker_town_hall_level", nullable = false)
    private Integer attackerTownHallLevel;

    @Column(name = "defender_tag", nullable = false)
    private String defenderTag;

    @Column(name = "defender_name", nullable = false)
    private String defenderName;

    @Column(name = "defender_town_hall_level", nullable = false)
    private Integer defenderTownHallLevel;

    @Column(name = "stars", nullable = false)
    private Integer stars;

    @Column(name = "destruction_percentage", nullable = false)
    private Double destructionPercentage;

    @Column(name = "attack_order", nullable = false)
    private Integer attackOrder;

    @Column(name = "duration_seconds", nullable = false)
    private Integer durationSeconds;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = true)
    private Instant updatedAt;

    public WarAttack(
            @NonNull War war,
            @NonNull String attackerTag,
            @NonNull String attackerName,
            @NonNull String defenderTag,
            @NonNull String defenderName,
            @NonNull Integer attackerTownHallLevel,
            @NonNull Integer defenderTownHallLevel,
            @NonNull Integer stars,
            @NonNull Double destructionPercentage,
            @NonNull Integer attackOrder,
            @NonNull Integer durationSeconds) {
        this.war = war;
        this.attackerTag = attackerTag;
        this.attackerName = attackerName;
        this.defenderTag = defenderTag;
        this.defenderName = defenderName;
        this.attackerTownHallLevel = attackerTownHallLevel;
        this.defenderTownHallLevel = defenderTownHallLevel;
        this.stars = stars;
        this.destructionPercentage = destructionPercentage;
        this.attackOrder = attackOrder;
        this.durationSeconds = durationSeconds;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof WarAttack other)) {
            return false;
        }
        return Objects.equals(this.getWar(), other.getWar())
                && Objects.equals(this.getAttackOrder(), other.getAttackOrder());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getWar(), this.getAttackOrder());
    }

}
