package com.chapeullah.guccigoblin.location;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity @Table(name = "locations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Location {

    @Id
    @Column(name = "id",
            nullable = false)
    private Integer id;

    @Column(name = "name",
            nullable = false)
    private String name;

    @Column(name = "is_country",
            nullable = false)
    private Boolean isCountry;

    @Column(name = "country_code",
            nullable = true)
    private String countryCode;

    public Location(
            @NonNull Integer id,
            @NonNull String name,
            @NonNull Boolean isCountry,
            String countryCode) {
        this.id = id;
        this.name = name;
        this.isCountry = isCountry;
        this.countryCode = countryCode;
    }

}
