package com.chapeullah.guccigoblin.leaguetier;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity @Table(name = "league_tiers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LeagueTier {

    @Id
    @Column(name = "id",
            nullable = false)
    private Integer id;

    @Column(name = "name",
            nullable = false)
    private String name;

    @Column(name = "small_icon_url",
            nullable = true,
            length = 512)
    private String smallIconUrl;

    @Column(name = "large_icon_url",
            nullable = true,
            length = 512)
    private String largeIconUrl;

    public LeagueTier(
            @NonNull Integer id,
            @NonNull String name,
            String smallIconUrl,
            String largeIconUrl) {
        if (name.isBlank()) {
            throw new IllegalArgumentException();
        }
        this.id = id;
        this.name = name;
        this.smallIconUrl = smallIconUrl;
        this.largeIconUrl = largeIconUrl;
    }

}
