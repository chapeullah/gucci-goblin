package com.chapeullah.guccigoblin.player.model;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(
        name = "player_house_elements",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_player_house_elements_player_element",
                columnNames = {"player_tag", "element_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlayerHouseElement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false)
    @JoinColumn(name = "player_tag",
            nullable = false)
    private Player player;

    @Column(name = "element_id",
            nullable = false)
    private Integer elementId;

    @Column(name = "element_type",
            nullable = false)
    private String elementType;

    public PlayerHouseElement(
            @NonNull Player player,
            @NonNull Integer elementId,
            @NonNull String elementType) {
        if (elementType.isBlank()) {
            throw new IllegalArgumentException("Players house element type must not be blank");
        }
        this.player = player;
        this.elementId = elementId;
        this.elementType = elementType;
    }

    public void updateFrom(PlayerHouseElement source) {
        if (!this.elementId.equals(source.elementId)) {
            throw new IllegalArgumentException("Player house element IDs mismatch: " +
                    "currentElementId=" + elementId
                    + ", sourceElementId=" + source.elementId);
        }
        this.elementType = source.elementType;
    }

}
