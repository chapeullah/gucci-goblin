package com.chapeullah.guccigoblin.player.model;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "player_house_elements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HouseElement {

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

    public HouseElement(
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

}
