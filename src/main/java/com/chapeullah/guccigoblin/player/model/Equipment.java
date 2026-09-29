package com.chapeullah.guccigoblin.player.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "player_hero_equipments",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_player_hero_equipments_hero_name",
                columnNames = {"hero_id", "name"}))
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hero_id", nullable = false)
    private Hero hero;

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

    public Equipment(
            @NonNull Hero hero,
            @NonNull String name,
            @NonNull Integer level,
            @NonNull Integer maxLevel,
            @NonNull String village) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("Players hero equipment must not be blank");
        }
        if (village.isBlank()) {
            throw new IllegalArgumentException("Players hero equipment must not be blank");
        }
        this.hero = hero;
        this.name = name;
        this.level = level;
        this.maxLevel = maxLevel;
        this.village = village;
    }

}
