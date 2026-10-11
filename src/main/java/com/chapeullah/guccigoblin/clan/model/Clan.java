package com.chapeullah.guccigoblin.clan.model;

import com.chapeullah.guccigoblin.capitalleague.CapitalLeague;
import com.chapeullah.guccigoblin.leaguetier.LeagueTier;
import com.chapeullah.guccigoblin.location.Location;
import com.chapeullah.guccigoblin.member.model.Member;
import com.chapeullah.guccigoblin.warleague.WarLeague;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity @Table(name = "clans")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Clan {

    @Id
    @Column(name = "tag", nullable = false)
    private String tag;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "type_changed_at", nullable = true)
    private Instant typeChangedAt;

    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "description_changed_at", nullable = true)
    private Instant descriptionChangedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "location_id", nullable = true)
    private Location location;

    @Column(name = "location_changed_at", nullable = true)
    private Instant locationChangedAt;

    @Column(name = "is_family_friendly", nullable = true)
    private Boolean isFamilyFriendly;

    @Embedded
    private ClanBadgeUrls clanBadgeUrls;

    @Column(name = "clan_badge_urls_changed_at", nullable = true)
    private Instant clanBadgeUrlsChangedAt;

    @Column(name = "clan_level", nullable = true)
    private Integer clanLevel;

    @Column(name = "clan_level_changed_at", nullable = true)
    private Instant clanLevelChangedAt;

    @Column(name = "clan_points", nullable = true)
    private Integer clanPoints;

    @Column(name = "clan_points_changed_at", nullable = true)
    private Instant clanPointsChangedAt;

    @Column(name = "clan_builder_base_points", nullable = true)
    private Integer clanBuilderBasePoints;

    @Column(name = "clan_builder_base_points_changed_at", nullable = true)
    private Instant clanBuilderBasePointsChangedAt;

    @Column(name = "clan_capital_points", nullable = true)
    private Integer clanCapitalPoints;

    @Column(name = "clan_capital_points_changed_at", nullable = true)
    private Instant clanCapitalPointsChangedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "capital_league_id", nullable = true)
    private CapitalLeague capitalLeague;

    @Column(name = "capital_league_changed_at", nullable = true)
    private Instant capitalLeagueChangedAt;

    @Column(name = "required_trophies", nullable = true)
    private Integer requiredTrophies;

    @Column(name = "required_trophies_changed_at", nullable = true)
    private Instant requiredTrophiesChangedAt;

    @Column(name = "war_frequency", nullable = true)
    private String warFrequency;

    @Column(name = "war_frequency_changed_at", nullable = true)
    private Instant warFrequencyChangedAt;

    @Column(name = "war_win_streak", nullable = true)
    private Integer warWinStreak;

    @Column(name = "war_win_streak_changed_at", nullable = true)
    private Instant warWinStreakChangedAt;

    @Column(name = "war_wins", nullable = true)
    private Integer warWins;

    @Column(name = "war_wins_changed_at", nullable = true)
    private Instant warWinsChangedAt;

    @Column(name = "war_ties", nullable = true)
    private Integer warTies;

    @Column(name = "war_ties_changed_at", nullable = true)
    private Instant warTiesChangedAt;

    @Column(name = "war_losses", nullable = true)
    private Integer warLosses;

    @Column(name = "war_losses_changed_at", nullable = true)
    private Instant warLossesChangedAt;

    @Column(name = "is_war_log_public", nullable = true)
    private Boolean isWarLogPublic;

    @Column(name = "is_war_log_public_changed_at", nullable = true)
    private Instant isWarLogPublicChangedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "war_league_id", nullable = true)
    private WarLeague warLeague;

    @Column(name = "war_league_changed_at", nullable = true)
    private Instant warLeagueChangedAt;

    @Column(name = "members", nullable = true)
    private Integer members;

    @Column(name = "members_changed_at", nullable = true)
    private Instant membersChangedAt;

    @SuppressWarnings("FieldMayBeFinal")
    @OneToMany(
            mappedBy = "clan",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Member> memberList = new ArrayList<>();

    @Column(name = "member_list_changed_at", nullable = true)
    private Instant memberListChangedAt;

    @SuppressWarnings("FieldMayBeFinal")
    @OneToMany(mappedBy = "clan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClanLabelLink> labelLinks = new ArrayList<>();

    @Column(name = "label_links_changed_at", nullable = true)
    private Instant labelLinksChangedAt;

    @Column(name = "required_builder_base_trophies", nullable = true)
    private Integer requiredBuilderBaseTrophies;

    @Column(name = "required_builder_base_trophies_changed_at", nullable = true)
    private Instant requiredBuilderBaseTrophiesChangedAt;

    @Column(name = "required_townhall_level", nullable = true)
    private Integer requiredTownhallLevel;

    @Column(name = "required_townhall_level_changed_at", nullable = true)
    private Instant requiredTownhallLevelChangedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "required_league_tier_id", nullable = true)
    private LeagueTier requiredLeagueTier;

    @Column(name = "required_league_tier_changed_at", nullable = true)
    private Instant requiredLeagueTierChangedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "chat_language_id", nullable = true)
    private ClanChatLanguage clanChatLanguage;

    @Column(name = "clan_chat_language_changed_at", nullable = true)
    private Instant clanChatLanguageChangedAt;

    public Clan(
            @NonNull String tag,
            @NonNull String name,
            @NonNull String type,
            String description,
            Location location,
            Boolean isFamilyFriendly,
            ClanBadgeUrls clanBadgeUrls,
            Integer clanLevel,
            Integer clanPoints,
            Integer clanBuilderBasePoints,
            Integer clanCapitalPoints,
            CapitalLeague capitalLeague,
            Integer requiredTrophies,
            String warFrequency,
            Integer warWinStreak,
            Integer warWins,
            Integer warTies,
            Integer warLosses,
            Boolean isWarLogPublic,
            WarLeague warLeague,
            Integer members,
            Integer requiredBuilderBaseTrophies,
            Integer requiredTownhallLevel,
            LeagueTier requiredLeagueTier,
            ClanChatLanguage clanChatLanguage) {
        this.tag = tag;
        this.name = name;
        this.type = type;
        this.description = description;
        this.location = location;
        this.isFamilyFriendly = isFamilyFriendly;
        this.clanBadgeUrls = clanBadgeUrls;
        this.clanLevel = clanLevel;
        this.clanPoints = clanPoints;
        this.clanBuilderBasePoints = clanBuilderBasePoints;
        this.clanCapitalPoints = clanCapitalPoints;
        this.capitalLeague = capitalLeague;
        this.requiredTrophies = requiredTrophies;
        this.warFrequency = warFrequency;
        this.warWinStreak = warWinStreak;
        this.warWins = warWins;
        this.warTies = warTies;
        this.warLosses = warLosses;
        this.isWarLogPublic = isWarLogPublic;
        this.warLeague = warLeague;
        this.members = members;
        this.requiredBuilderBaseTrophies = requiredBuilderBaseTrophies;
        this.requiredTownhallLevel = requiredTownhallLevel;
        this.requiredLeagueTier = requiredLeagueTier;
        this.clanChatLanguage = clanChatLanguage;
    }

    public void updateFrom(@NonNull Clan source) {
        if (!this.tag.equals(source.tag)) {
            throw new IllegalArgumentException("Clan tag mismatch");
        }

        Instant now = Instant.now();

        if (!Objects.equals(this.type, source.type)) {
            this.type = source.type;
            this.typeChangedAt = now;
        }

        if (!Objects.equals(this.description, source.description)) {
            this.description = source.description;
            this.descriptionChangedAt = now;
        }

        if (!Objects.equals(this.location, source.location)) {
            this.location = source.location;
            this.locationChangedAt = now;
        }

        if (!Objects.equals(this.clanBadgeUrls, source.clanBadgeUrls)) {
            this.clanBadgeUrls = source.clanBadgeUrls;
            this.clanBadgeUrlsChangedAt = now;
        }

        if (!Objects.equals(this.clanLevel, source.clanLevel)) {
            this.clanLevel = source.clanLevel;
            this.clanLevelChangedAt = now;
        }

        if (!Objects.equals(this.clanPoints, source.clanPoints)) {
            this.clanPoints = source.clanPoints;
            this.clanPointsChangedAt = now;
        }

        if (!Objects.equals(this.clanBuilderBasePoints, source.clanBuilderBasePoints)) {
            this.clanBuilderBasePoints = source.clanBuilderBasePoints;
            this.clanBuilderBasePointsChangedAt = now;
        }

        if (!Objects.equals(this.clanCapitalPoints, source.clanCapitalPoints)) {
            this.clanCapitalPoints = source.clanCapitalPoints;
            this.clanCapitalPointsChangedAt = now;
        }

        if (!Objects.equals(this.capitalLeague, source.capitalLeague)) {
            this.capitalLeague = source.capitalLeague;
            this.capitalLeagueChangedAt = now;
        }

        if (!Objects.equals(this.requiredTrophies, source.requiredTrophies)) {
            this.requiredTrophies = source.requiredTrophies;
            this.requiredTrophiesChangedAt = now;
        }

        if (!Objects.equals(this.warFrequency, source.warFrequency)) {
            this.warFrequency = source.warFrequency;
            this.warFrequencyChangedAt = now;
        }

        if (!Objects.equals(this.warWinStreak, source.warWinStreak)) {
            this.warWinStreak = source.warWinStreak;
            this.warWinStreakChangedAt = now;
        }

        if (!Objects.equals(this.warWins, source.warWins)) {
            this.warWins = source.warWins;
            this.warWinsChangedAt = now;
        }

        if (!Objects.equals(this.warTies, source.warTies)) {
            this.warTies = source.warTies;
            this.warTiesChangedAt = now;
        }

        if (!Objects.equals(this.warLosses, source.warLosses)) {
            this.warLosses = source.warLosses;
            this.warLossesChangedAt = now;
        }

        if (!Objects.equals(this.isWarLogPublic, source.isWarLogPublic)) {
            this.isWarLogPublic = source.isWarLogPublic;
            this.isWarLogPublicChangedAt = now;
        }

        if (!Objects.equals(this.warLeague, source.warLeague)) {
            this.warLeague = source.warLeague;
            this.warLeagueChangedAt = now;
        }

        if (!Objects.equals(this.members, source.members)) {
            this.members = source.members;
            this.membersChangedAt = now;
        }

        if (!Objects.equals(this.requiredBuilderBaseTrophies, source.requiredBuilderBaseTrophies)) {
            this.requiredBuilderBaseTrophies = source.requiredBuilderBaseTrophies;
            this.requiredBuilderBaseTrophiesChangedAt = now;
        }

        if (!Objects.equals(this.requiredTownhallLevel, source.requiredTownhallLevel)) {
            this.requiredTownhallLevel = source.requiredTownhallLevel;
            this.requiredTownhallLevelChangedAt = now;
        }

        if (!Objects.equals(this.requiredLeagueTier, source.requiredLeagueTier)) {
            this.requiredLeagueTier = source.requiredLeagueTier;
            this.requiredLeagueTierChangedAt = now;
        }

        if (!Objects.equals(this.clanChatLanguage, source.clanChatLanguage)) {
            this.clanChatLanguage = source.clanChatLanguage;
            this.clanChatLanguageChangedAt = now;
        }
    }

}
