package com.chapeullah.guccigoblin.leaguetier;

import com.chapeullah.guccigoblin.ClashOfClansClient;
import com.chapeullah.guccigoblin.leaguetier.dto.LeagueTierResponse;
import com.chapeullah.guccigoblin.leaguetier.dto.LeagueTiersResponse;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeagueTierService {

    private final ClashOfClansClient client;

    private final LeagueTierRepository leagueTierRepository;

    public LeagueTier findById(@NonNull Integer id) {
        return leagueTierRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("League tier not found: id=" + id));
    }

    public LeagueTier syncAndGetById(@NonNull Integer id) {
        return syncLeagueTiers().stream()
                .filter(tier -> tier.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("League tier not found: leagueTierId=" + id));
    }

    @Transactional
    public List<LeagueTier> syncLeagueTiers() {
        LeagueTiersResponse response = client.getLeagueTiers();
        List<LeagueTier> leagueTiers = response
                .items()
                .stream()
                .map(this::toLeagueTier)
                .toList();
        return leagueTierRepository.saveAll(leagueTiers);
    }

    private LeagueTier toLeagueTier(LeagueTierResponse response) {
        return new LeagueTier(
                response.id(),
                response.name(),
                response.iconUrls().small(),
                response.iconUrls().large());
    }

}
