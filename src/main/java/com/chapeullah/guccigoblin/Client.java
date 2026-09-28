package com.chapeullah.guccigoblin;

import com.chapeullah.guccigoblin.member.dto.MembersResponse;
import com.chapeullah.guccigoblin.raidseason.dto.RaidSeasonResponse;
import com.chapeullah.guccigoblin.war.dto.WarResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class Client {

    private final RestClient rest;

    @Value("${coc.apiToken}")
    private String apiToken;

    @Value("${coc.clanTag}")
    private String clanTag;

    public MembersResponse getMembers() {
        return rest.get()
                .uri("/clans/{clanTag}/members", clanTag)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
                .retrieve()
                .body(MembersResponse.class);
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

}
