package com.chapeullah.guccigoblin.clan.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Objects;

@Entity @Table(name = "clan_badge_urls")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClanBadgeUrls {

    @Id
    @Column(name = "clan_tag", nullable = false)
    private String clanTag;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clan_tag", nullable = false)
    private Clan clan;

    @Column(name = "badge_small_url", length = 512, nullable = true)
    private String small;

    @Column(name = "badge_large_url", length = 512, nullable = true)
    private String large;

    @Column(name = "badge_medium_url", length = 512, nullable = true)
    private String medium;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    public ClanBadgeUrls(
            @NonNull Clan clan,
            String small,
            String large,
            String medium) {
        this.clanTag = clan.getTag();
        this.clan = clan;
        this.small = small;
        this.large = large;
        this.medium = medium;
    }

    public void update(String small, String large, String medium) {
        this.small = small;
        this.large = large;
        this.medium = medium;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ClanBadgeUrls other)) {
            return false;
        }
        return Objects.equals(this.getClanTag(), other.getClanTag())
                && Objects.equals(this.getSmall(), other.getSmall())
                && Objects.equals(this.getLarge(), other.getLarge())
                && Objects.equals(this.getMedium(), other.getMedium());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                this.getClanTag(),
                this.getSmall(),
                this.getLarge(),
                this.getMedium());
    }

}
