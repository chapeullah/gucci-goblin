package com.chapeullah.GucciGoblin.model;

import com.chapeullah.GucciGoblin.entity.PlayerEntity;
import lombok.Getter;
import lombok.NonNull;

import java.time.Instant;
import java.util.Objects;

@Getter
public class Player {

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Player other)) return false;
        return Objects.equals(this.tag, other.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag);
    }

    private final String tag;
    private final String name;
    private Instant joinedAt;
    private Instant leftAt;

    private Player(@NonNull String tag, @NonNull String name) {
        this.tag = tag;
        this.name = name;
    }

    private Player(@NonNull String tag, @NonNull String name, @NonNull Instant joinedAt, Instant leftAt) {
        this.tag = tag;
        this.name = name;
        this.joinedAt = joinedAt;
        this.leftAt = leftAt;
    }

    public static Player from(PlayerEntity playerEntity) {
        return new Player(
                playerEntity.getTag(),
                playerEntity.getName(),
                playerEntity.getJoinedAt(),
                playerEntity.getLeftAt()
        );
    }

    public static Player joined(Member member) {
        Player player = new Player(member.getTag(), member.getName());
        player.joinedAt = Instant.now();
        return player;
    }

    public void left() {
        this.leftAt = Instant.now();
    }

    public void rejoin() {
        this.joinedAt = Instant.now();
        this.leftAt = null;
    }

}
