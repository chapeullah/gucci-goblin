package com.chapeullah.guccigoblin.war.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "war_attack",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_war_attack_war_order",
                columnNames = {"war_id", "attack_order"}))
@Getter @Setter
@NoArgsConstructor
public class WarAttack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "war_id", nullable = false)
    private War war;

    @Column(name = "attacker_tag",
            nullable = false)
    private String attackerTag;

    @Column(name = "defender_tag",
            nullable = false)
    private String defenderTag;

    @Column(name = "attacker_th",
            nullable = false)
    private Integer attackerTH;

    @Column(name = "defender_th",
            nullable = false)
    private Integer defenderTH;

    @Column(name = "stars",
            nullable = false)
    private Integer stars;

    @Column(name = "destruction_percentage",
            nullable = false)
    private Double destructionPercentage;

    @Column(name = "attack_order",
            nullable = false)
    private Integer attackOrder;

    @Column(name = "duration_seconds",
            nullable = false)
    private Integer durationSeconds;

    public WarAttack(
            War war,
            String attackerTag,
            String defenderTag,
            Integer attackerTH,
            Integer defenderTH,
            Integer stars,
            Double destructionPercentage,
            Integer attackOrder,
            Integer durationSeconds) {
        this.war = war;
        this.attackerTag = attackerTag;
        this.defenderTag = defenderTag;
        this.attackerTH = attackerTH;
        this.defenderTH = defenderTH;
        this.stars = stars;
        this.destructionPercentage = destructionPercentage;
        this.attackOrder = attackOrder;
        this.durationSeconds = durationSeconds;
    }

}
