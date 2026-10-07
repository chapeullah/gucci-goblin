package com.chapeullah.guccigoblin.player.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(
        name = "hero_equipments",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_hero_equipments_player_name",
                columnNames = {"player_tag", "name"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlayerHeroEquipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_tag", nullable = false)
    private Player player;

    @Column(name = "name",
            nullable = false)
    private String name;

    @Column(name = "level",
            nullable = false)
    private Integer level;

    @Column(name = "max_level",
            nullable = false)
    private Integer maxLevel;

    @Column(name = "village",
            nullable = false)
    private String village;

    public PlayerHeroEquipment(
            @NonNull Player player,
            @NonNull String name,
            @NonNull Integer level,
            @NonNull Integer maxLevel,
            @NonNull String village) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("Hero equipment name must not be blank");
        }
        this.player = player;
        this.name = name;
        this.level = level;
        this.maxLevel = maxLevel;
        this.village = village;
    }

    public void updateFrom(PlayerHeroEquipment source) {
        this.level = source.level;
        this.maxLevel = source.maxLevel;
        this.village = source.village;
    }

}
