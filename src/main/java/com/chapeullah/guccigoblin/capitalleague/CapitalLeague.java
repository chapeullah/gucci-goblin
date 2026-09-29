package com.chapeullah.guccigoblin.capitalleague;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity @Table(name = "capital_leagues")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CapitalLeague {

    @Id
    @Column(name = "id",
            nullable = false)
    private Integer id;

    @Column(name = "name",
            nullable = false)
    private String name;

    public CapitalLeague(@NonNull Integer id, @NonNull String name) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("Clan capital league name must not be blank");
        }
        this.id = id;
        this.name = name;
    }

}
