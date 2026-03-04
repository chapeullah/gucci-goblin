package com.chapeullah.GucciGoblin.entity;

import com.chapeullah.GucciGoblin.model.enums.TownHallRel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "war_attack")
@Getter @Setter
@NoArgsConstructor
public class WarAttackEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="war_key", nullable = false)
    private String warKey;

    @Column(name="tag", nullable = false)
    private String tag;

    @Column(name="name", nullable = false)
    private String name;

    @Column(name="a1_stars")
    private Integer a1Stars;

    @Enumerated(EnumType.STRING)
    @Column(name="a1_townhall_rel")
    private TownHallRel a1TownHallRel;

    @Column(name="a2_stars")
    private Integer a2Stars;

    @Enumerated(EnumType.STRING)
    @Column(name="a2_townhall_rel")
    private TownHallRel a2TownHallRel;

}
