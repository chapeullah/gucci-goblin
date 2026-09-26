package com.chapeullah.guccigoblin.player;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "players")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlayerEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tag",
            nullable = false,
            updatable = false)
    private String tag;

    @Column(name = "name",
            nullable = false,
            updatable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type",
            nullable = false,
            updatable = false)
    private PlayerEventType type;

    @CreationTimestamp
    @Column(name = "detected_at",
            nullable = false,
            updatable = false)
    private Instant detectedAt;

    private PlayerEvent(@NonNull String tag, @NonNull String name, @NonNull PlayerEventType type) {
        this.tag = tag;
        this.name = name;
        this.type = type;
    }

    public static PlayerEvent joined(
            @NonNull String tag,
            @NonNull String name) {
        return new PlayerEvent(tag, name, PlayerEventType.JOINED);
    }

    public static PlayerEvent left(
            @NonNull String tag,
            @NonNull String name) {
        return new PlayerEvent(tag, name, PlayerEventType.LEFT);
    }

}
