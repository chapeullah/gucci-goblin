package com.chapeullah.guccigoblin.war.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "war_participants",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_war_participants_war_player",
                columnNames = {"war_id", "player_tag"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WarParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "war_id", nullable = false)
    private War war;

    @Column(name = "player_tag", nullable = false)
    private String playerTag;

    @Column(name = "player_name", nullable = false)
    private String playerName;

    @Column(name = "clan_tag", nullable = false)
    private String clanTag;

    @Column(name = "town_hall_level", nullable = false)
    private Integer townHallLevel;

    @Column(name = "map_position", nullable = false)
    private Integer mapPosition;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = true)
    private Instant updatedAt;

    public WarParticipant(
            @NonNull War war,
            @NonNull String playerTag,
            @NonNull String playerName,
            @NonNull String clanTag,
            @NonNull Integer townHallLevel,
            @NonNull Integer mapPosition) {
        this.war = war;
        this.playerTag = playerTag;
        this.playerName = playerName;
        this.clanTag = clanTag;
        this.townHallLevel = townHallLevel;
        this.mapPosition = mapPosition;
    }

    public void update(
            @NonNull String playerName,
            @NonNull Integer townHallLevel) {
        this.playerName = playerName;
        this.townHallLevel = townHallLevel;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof WarParticipant other)) {
            return false;
        }
        return Objects.equals(this.getWar(), other.getWar())
                && Objects.equals(this.getPlayerTag(), other.getPlayerTag());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getWar(), this.getPlayerTag());
    }

}