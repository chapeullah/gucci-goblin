package com.chapeullah.GucciGoblin.entity;

import com.chapeullah.GucciGoblin.model.Player;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "players")
@Getter @Setter
@NoArgsConstructor
public class PlayerEntity {

    @Id
    @Column(name="tag",
            nullable = false)
    private String tag;

    @Column(name="name",
            nullable = false)
    private String name;

    @Column(name="joined_at",
            nullable = false)
    private Instant joinedAt = Instant.now();

    @Column(name="left_at",
            nullable = true)
    private Instant leftAt;

    private PlayerEntity(@NonNull String tag, @NonNull String name, @NonNull Instant joinedAt, Instant leftAt) {
        this.tag = tag;
        this.name = name;
        this.joinedAt = joinedAt;
        this.leftAt = leftAt;
    }

    public static PlayerEntity from(@NonNull Player player) {
        return new PlayerEntity(player.getTag(), player.getName(), player.getJoinedAt(), player.getLeftAt());
    }

}
