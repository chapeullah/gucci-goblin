package com.chapeullah.guccigoblin.clan.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClanBadgeUrls {

    @Column(name = "badge_small_url",
            length = 512)
    private String small;

    @Column(name = "badge_large_url",
            length = 512)
    private String large;

    @Column(name = "badge_medium_url",
            length = 512)
    private String medium;

    public ClanBadgeUrls(
            String small, String large, String medium) {
        this.small = small;
        this.large = large;
        this.medium = medium;
    }

}
