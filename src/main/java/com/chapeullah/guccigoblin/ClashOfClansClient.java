package com.chapeullah.guccigoblin;

import com.chapeullah.guccigoblin.builderbaseleague.dto.BuilderBaseLeaguesResponse;
import com.chapeullah.guccigoblin.capitalleague.dto.CapitalLeaguesResponse;
import com.chapeullah.guccigoblin.label.dto.LabelsResponse;
import com.chapeullah.guccigoblin.leaguetier.dto.LeagueTiersResponse;
import com.chapeullah.guccigoblin.location.dto.LocationsResponse;
import com.chapeullah.guccigoblin.player.dto.PlayerResponse;
import com.chapeullah.guccigoblin.raidseason.dto.RaidSeasonResponse;
import com.chapeullah.guccigoblin.war.dto.WarResponse;
import com.chapeullah.guccigoblin.warleague.dto.WarLeaguesResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class ClashOfClansClient {

    private final RestClient rest;

    @Value("${coc.apiToken}")
    private String apiToken;

    @Value("${coc.clanTag}")
    private String clanTag;

    public PlayerResponse getPlayer(@NonNull String playerTag) {
        return rest.get()
                .uri("/players/{playerTag}", playerTag)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
                .retrieve()
                .body(PlayerResponse.class);
    }


    public WarResponse getCurrentWar() {
        return rest.get()
                .uri("/clans/{clanTag}/currentwar", clanTag)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
                .retrieve()
                .body(WarResponse.class);
    }

    public RaidSeasonResponse getCurrentRaid() {
        return rest.get()
                .uri("/clans/{clanTag}/capitalraidseasons?limit=1", clanTag)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
                .retrieve()
                .body(RaidSeasonResponse.class);
    }

    public LeagueTiersResponse getLeagueTiers() {
        return rest.get()
                .uri("/leaguetiers")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
                .retrieve()
                .body(LeagueTiersResponse.class);
    }

    public LocationsResponse getLocations() {
        return rest.get()
                .uri("/locations")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
                .retrieve()
                .body(LocationsResponse.class);
    }

    public CapitalLeaguesResponse getCapitalLeagues() {
        return rest.get()
                .uri("/capitalleagues")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
                .retrieve()
                .body(CapitalLeaguesResponse.class);
    }

    public WarLeaguesResponse getWarLeagues() {
        return rest.get()
                .uri("/warleagues")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
                .retrieve()
                .body(WarLeaguesResponse.class);
    }

    public BuilderBaseLeaguesResponse getBuilderBaseLeagues() {
        return rest.get()
                .uri("/builderbaseleagues")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
                .retrieve()
                .body(BuilderBaseLeaguesResponse.class);
    }

    public LabelsResponse getPlayerLabelsResponse() {
        return rest.get()
                .uri("/labels/players")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
                .retrieve()
                .body(LabelsResponse.class);
    }

    public LabelsResponse getClanLabelsResponse() {
        return rest.get()
                .uri("/labels/clans")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
                .retrieve()
                .body(LabelsResponse.class);
    }

}
