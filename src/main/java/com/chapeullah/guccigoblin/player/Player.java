package com.chapeullah.guccigoblin.player;

import com.chapeullah.guccigoblin.member.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "players")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Player {

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Player other)) return false;
        return getTag() != null && Objects.equals(getTag(), other.getTag());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTag());
    }

    @Id
    @Column(name = "tag", nullable = false)
    private String tag;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    @Column(name = "left_at")
    private Instant leftAt;

    private Player(@NonNull String tag, @NonNull String name) {
        this.tag = tag;
        this.name = name;
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
