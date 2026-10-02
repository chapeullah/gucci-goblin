package com.chapeullah.guccigoblin.player.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "troops")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Troop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id",
            nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_tag", nullable = false)
    private Player player;

    @Column(name = "name",
            nullable = false)
    private String name;

    @Column(name = "level",
            nullable = false )
    private Integer level;

    @Column(name = "max_level",
            nullable = false)
    private Integer maxLevel;

    @Column(name = "village",
            nullable = false)
    private String village;

    public Troop(
            @NonNull Player player,
            @NonNull String name,
            @NonNull Integer level,
            @NonNull Integer maxLevel,
            @NonNull String village) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("Player troop name must not be blank");
        }
        this.player = player;
        this.name = name;
        this.level = level;
        this.maxLevel = maxLevel;
        this.village = village;
    }

    public void updateFrom(Troop source) {
        this.level = source.level;
        this.maxLevel = source.maxLevel;
    }

}
