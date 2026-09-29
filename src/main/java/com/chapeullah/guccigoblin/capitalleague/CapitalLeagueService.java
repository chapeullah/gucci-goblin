package com.chapeullah.guccigoblin.capitalleague;

import com.chapeullah.guccigoblin.ClashOfClansClient;
import com.chapeullah.guccigoblin.capitalleague.dto.CapitalLeagueResponse;
import com.chapeullah.guccigoblin.capitalleague.dto.CapitalLeaguesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CapitalLeagueService {

    private final ClashOfClansClient client;

    private final CapitalLeagueRepository capitalLeagueRepository;

    public void syncCapitalLeagues() {
        CapitalLeaguesResponse response = client.getCapitalLeagues();
        List<CapitalLeague> capitalLeagues = response
                .items()
                .stream()
                .map(this::toCapitalLeague)
                .toList();
        capitalLeagueRepository.saveAll(capitalLeagues);
    }

    private CapitalLeague toCapitalLeague(CapitalLeagueResponse response) {
        return new CapitalLeague(response.id(), response.name());
    }

}
