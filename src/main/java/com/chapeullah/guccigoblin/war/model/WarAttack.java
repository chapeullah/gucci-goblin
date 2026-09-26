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

    @Column(name = "attacker_name",
            nullable = false)
    private String attackerName;

    @Column(name = "defender_tag",
            nullable = false)
    private String defenderTag;

    @Column(name = "defender_name",
            nullable = false)
    private String defenderName;

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
            String attackerName,
            String defenderTag,
            String defenderName,
            Integer attackerTH,
            Integer defenderTH,
            Integer stars,
            Double destructionPercentage,
            Integer attackOrder,
            Integer durationSeconds) {
        this.war = war;
        this.attackerTag = attackerTag;
        this.attackerName = attackerName;
        this.defenderTag = defenderTag;
        this.defenderName = defenderName;
        this.attackerTH = attackerTH;
        this.defenderTH = defenderTH;
        this.stars = stars;
        this.destructionPercentage = destructionPercentage;
        this.attackOrder = attackOrder;
        this.durationSeconds = durationSeconds;
    }

}
