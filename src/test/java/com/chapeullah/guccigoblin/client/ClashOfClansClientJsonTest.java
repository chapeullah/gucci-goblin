package com.chapeullah.guccigoblin.client;

import com.chapeullah.guccigoblin.ClashOfClansClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ClashOfClansClientJsonTest {

    private MockRestServiceServer server;
    private ClashOfClansClient client;

    @BeforeEach
    void configureClient() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.clashofclans.com/v1");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new ClashOfClansClient(builder.build());
        ReflectionTestUtils.setField(client, "apiToken", "test-token");
        ReflectionTestUtils.setField(client, "clanTag", "#HOME");
    }


    @Test
    void readsWarAndNormalizesMissingOrNullAttackLists() {
        server.expect(request -> {
            assertEquals("https://api.clashofclans.com/v1/clans/%23HOME/currentwar", request.getURI().toString());
            assertEquals("Bearer test-token", request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        }).andRespond(withSuccess(new ClassPathResource("fixtures/war-in-war.json"), MediaType.APPLICATION_JSON));

        var response = client.getCurrentWar();

        assertNotNull(response);
        assertEquals("inWar", response.state());
        assertEquals(2, response.teamSize());
        assertEquals("20260925T120000.000Z", response.startTime());
        var attacker = response.clan().members().getFirst();
        assertEquals(15, attacker.townhallLevel());
        assertEquals(60.5, attacker.attacks().getFirst().destructionPercentage());
        assertEquals(123, attacker.attacks().getFirst().duration());
        assertEquals("#X", attacker.bestOpponentAttack().attackerTag());
        assertTrue(response.clan().members().get(1).attacks().isEmpty());
        assertTrue(response.opponent().members().get(1).attacks().isEmpty());
        server.verify();
    }

    @Test
    void acceptsMinimalNotInWarResponse() {
        server.expect(request -> assertEquals(HttpMethod.GET, request.getMethod()))
                .andRespond(withSuccess("{\"state\":\"notInWar\"}", MediaType.APPLICATION_JSON));

        var response = client.getCurrentWar();

        assertNotNull(response);
        assertEquals("notInWar", response.state());
        assertNull(response.clan());
        assertNull(response.opponent());
        server.verify();
    }


    @Test
    void forbiddenWarResponseRemainsAnErrorInsteadOfNotInWar() {
        server.expect(request -> assertEquals(HttpMethod.GET, request.getMethod()))
                .andRespond(withStatus(HttpStatus.FORBIDDEN));

        assertThrows(HttpClientErrorException.Forbidden.class, client::getCurrentWar);

        server.verify();
    }
}
