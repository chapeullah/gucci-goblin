package com.chapeullah.guccigoblin.player.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "player_heroes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Hero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id",
            nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_tag", nullable = false)
    private Player player;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "level", nullable = false)
    private Integer level;

    @Column(name = "max_level", nullable = false)
    private Integer maxLevel;

    @SuppressWarnings("FieldMayBeFinal")
    @OneToMany(
            mappedBy = "hero",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Equipment> equipments = new ArrayList<>();

    public Hero(
            @NonNull Player player,
            @NonNull String name,
            @NonNull Integer level,
            @NonNull Integer maxLevel,
            @NonNull List<Equipment> equipments) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("Player hero name must not be blank");
        }
        this.player = player;
        this.name = name;
        this.level = level;
        this.maxLevel = maxLevel;
        this.equipments = equipments;
    }
}
