package com.chapeullah.guccigoblin.war.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "war_participants",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_war_participants_war_player",
                columnNames = {"war_id", "player_tag"}))
@Getter @Setter
@NoArgsConstructor
public class WarParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    public WarParticipant(
            War war,
            String playerTag,
            String playerName,
            String clanTag,
            Integer townHallLevel,
            Integer mapPosition) {
        this.war = war;
        this.playerTag = playerTag;
        this.playerName = playerName;
        this.clanTag = clanTag;
        this.townHallLevel = townHallLevel;
        this.mapPosition = mapPosition;
    }

}