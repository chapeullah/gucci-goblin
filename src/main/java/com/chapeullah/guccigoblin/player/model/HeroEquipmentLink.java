package com.chapeullah.guccigoblin.player.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity @Table(
        name = "hero_equipment_links",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_hero_equipment_links_hero_equipment",
                columnNames = {"hero_id", "hero_equipment_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HeroEquipmentLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hero_id", nullable = false)
    private Hero hero;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hero_equipment_id", nullable = false)
    private HeroEquipment heroEquipment;

    public HeroEquipmentLink(
            @NonNull Hero hero,
            @NonNull HeroEquipment heroEquipment) {
        this.hero = hero;
        this.heroEquipment = heroEquipment;
    }

}
