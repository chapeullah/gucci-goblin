package com.chapeullah.guccigoblin.war.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Objects;

@Entity @Table(name = "war_clan_badge_urls")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WarClanBadgeUrls {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "war_id", nullable = false, unique = true)
    private War war;

    @Column(name = "badge_small_url", length = 512, nullable = true)
    private String small;

    @Column(name = "badge_large_url", length = 512, nullable = true)
    private String large;

    @Column(name = "badge_medium_url", length = 512, nullable = true)
    private String medium;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = true)
    private Instant updatedAt;

    public WarClanBadgeUrls(
            @NonNull War war,
            String small,
            String large,
            String medium) {
        this.war = war;
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
        if (!(object instanceof WarClanBadgeUrls other)) {
            return false;
        }
        return Objects.equals(this.getWar(), other.getWar())
                && Objects.equals(this.getSmall(), other.getSmall())
                && Objects.equals(this.getLarge(), other.getLarge())
                && Objects.equals(this.getMedium(), other.getMedium());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                this.getWar(),
                this.getSmall(),
                this.getLarge(),
                this.getMedium());
    }

}
