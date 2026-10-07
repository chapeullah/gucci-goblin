package com.chapeullah.guccigoblin.player.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "player_heroes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_player_heroes_player_tag_name",
                columnNames = {"player_tag", "name"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlayerHero {

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

    @SuppressWarnings("FieldMayBeFinal")
    @OneToMany(
            mappedBy = "playerHero",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<PlayerHeroEquipmentLink> playerHeroEquipmentLinks = new ArrayList<>();

    @Column(name = "village",
            nullable = false)
    private String village;

    public PlayerHero(
            @NonNull Player player,
            @NonNull String name,
            @NonNull Integer level,
            @NonNull Integer maxLevel,
            List<PlayerHeroEquipmentLink> playerHeroEquipmentLinks,
            @NonNull String village) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("Player hero name must not be blank");
        }
        if (village.isBlank()) {
            throw new IllegalArgumentException("Player hero village must not be blank");
        }
        this.player = player;
        this.name = name;
        this.level = level;
        this.maxLevel = maxLevel;
        this.playerHeroEquipmentLinks = playerHeroEquipmentLinks;
        this.village = village;
    }

    public void updateFrom(PlayerHero source) {
        this.level = source.level;
        this.maxLevel = source.maxLevel;
    }

}
