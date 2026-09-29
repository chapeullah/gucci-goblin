package com.chapeullah.guccigoblin.warleague;

import com.chapeullah.guccigoblin.ClashOfClansClient;
import com.chapeullah.guccigoblin.warleague.dto.WarLeagueResponse;
import com.chapeullah.guccigoblin.warleague.dto.WarLeaguesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarLeagueService {

    private final ClashOfClansClient client;

    private final WarLeagueRepository warLeagueRepository;

    public void syncWarLeagues() {
        WarLeaguesResponse response = client.getWarLeagues();
        List<WarLeague> warLeagues = response
                .items()
                .stream()
                .map(this::toWarLeague)
                .toList();
        warLeagueRepository.saveAll(warLeagues);
    }

    private WarLeague toWarLeague(WarLeagueResponse response) {
        return new WarLeague(response.id(), response.name());
    }

}
