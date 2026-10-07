package com.chapeullah.guccigoblin.player.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity @Table(
        name = "achievements",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_achievements_player_tag_name_village",
                columnNames = {"player_tag", "name"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlayerAchievement {

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

    @Column(name = "stars",
            nullable = true)
    private Integer stars;

    @Column(name = "value",
            nullable = true)
    private Integer value;

    @Column(name = "target",
            nullable = true)
    private Integer target;

    @Column(name = "info",
            nullable = true)
    private String info;

    @Column(name = "completion_info",
            nullable = true)
    private String completionInfo;

    @Column(name = "village",
            nullable = true)
    private String village;

    public PlayerAchievement(
            @NonNull Player player,
            @NonNull String name,
            Integer stars,
            Integer value,
            Integer target,
            String info,
            String completionInfo,
            String village) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("Achievement name must not be blank");
        }
        if (stars != null && (stars < 0 || stars > 3)) {
            throw new IllegalArgumentException("Achievement stars must be between 0 and 3");
        }
        if (info != null && info.isBlank()) {
            throw new IllegalArgumentException("Achievement info must not be blank");
        }
        if (completionInfo != null && completionInfo.isBlank()) {
            throw new IllegalArgumentException("Achievement completion info must not be blank");
        }
        if (village != null && village.isBlank()) {
            throw new IllegalArgumentException("Achievement village must not be blank");
        }

        this.player = player;
        this.name = name;
        this.stars = stars;
        this.value = value;
        this.target = target;
        this.info = info;
        this.completionInfo = completionInfo;
        this.village = village;
    }

    public void updateFrom(PlayerAchievement playerAchievement) {
        this.stars =            playerAchievement.stars;
        this.value =            playerAchievement.value;
        this.target =           playerAchievement.target;
        this.info =             playerAchievement.info;
        this.completionInfo =   playerAchievement.completionInfo;
        this.village =          playerAchievement.village;
    }

}
