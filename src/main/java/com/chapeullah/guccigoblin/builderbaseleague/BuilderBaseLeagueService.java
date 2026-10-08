package com.chapeullah.guccigoblin.builderbaseleague;

import com.chapeullah.guccigoblin.ClashOfClansClient;
import com.chapeullah.guccigoblin.builderbaseleague.dto.BuilderBaseLeagueResponse;
import com.chapeullah.guccigoblin.builderbaseleague.dto.BuilderBaseLeaguesResponse;
import com.chapeullah.guccigoblin.leaguetier.LeagueTier;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BuilderBaseLeagueService {

    private final ClashOfClansClient client;

    private final BuilderBaseLeagueRepository builderBaseLeagueRepository;

    public BuilderBaseLeague findById(@NonNull Integer id) {
        return builderBaseLeagueRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Builder base league not found: id=" + id));
    }

    public List<BuilderBaseLeague> syncBuilderBaseLeagues() {
        BuilderBaseLeaguesResponse response = client.getBuilderBaseLeagues();
        List<BuilderBaseLeague> builderBaseLeagues = response
                .items()
                .stream()
                .map(this::toBuilderBaseLeague)
                .toList();
        return builderBaseLeagueRepository.saveAll(builderBaseLeagues);
    }

    private BuilderBaseLeague toBuilderBaseLeague(BuilderBaseLeagueResponse response) {
        return new BuilderBaseLeague(response.id(), response.name());
    }

}
