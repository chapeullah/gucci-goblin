package com.chapeullah.guccigoblin.warleague;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "war_leagues")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WarLeague {

    @Id
    @Column(name = "id",
            nullable = false)
    private Integer id;

    @Column(name = "name",
            nullable = false)
    private String name;

    public WarLeague(
            @NonNull Integer id,
            @NonNull String name) {
        this.id = id;
        this.name = name;
    }

}
