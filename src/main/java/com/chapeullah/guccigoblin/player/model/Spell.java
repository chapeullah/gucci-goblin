package com.chapeullah.guccigoblin.player.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "player_spells")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Spell {

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
            nullable = false)
    private Integer level;

    @Column(name = "max_level",
            nullable = false)
    private Integer maxLevel;

    @Column(name = "village",
            nullable = false)
    private String village;

    public Spell(
            Player player,
            String name,
            Integer level,
            Integer maxLevel,
            String village) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("Player spell must no be blank");
        }
        this.player = player;
        this.name = name;
        this.level = level;
        this.maxLevel = maxLevel;
        this.village = village;
    }

}
