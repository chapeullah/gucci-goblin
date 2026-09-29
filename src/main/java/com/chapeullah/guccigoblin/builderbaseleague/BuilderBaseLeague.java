package com.chapeullah.guccigoblin.builderbaseleague;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity @Table(name = "builder_base_leagues")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BuilderBaseLeague {

    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    public BuilderBaseLeague(
            @NonNull Integer id,
            @NonNull String name) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("Player builder base league name must not be blank");
        }
        this.id = id;
        this.name = name;
    }

}
