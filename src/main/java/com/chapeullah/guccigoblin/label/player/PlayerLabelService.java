package com.chapeullah.guccigoblin.label.player;

import com.chapeullah.guccigoblin.ClashOfClansClient;
import com.chapeullah.guccigoblin.label.IconUrls;
import com.chapeullah.guccigoblin.label.dto.LabelIconUrlsResponse;
import com.chapeullah.guccigoblin.label.dto.LabelResponse;
import com.chapeullah.guccigoblin.label.dto.LabelsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerLabelService {

    private final ClashOfClansClient client;

    private final PlayerLabelRepository playerLabelRepository;

    public void syncPlayerLabels() {
        LabelsResponse response = client.getPlayerLabelsResponse();
        List<PlayerLabel> playerLabels = response
                .items()
                .stream()
                .map(this::toPlayerLabel)
                .toList();
        playerLabelRepository.saveAll(playerLabels);
    }

    private PlayerLabel toPlayerLabel(LabelResponse response) {
        return new PlayerLabel(
                response.id(),
                response.name(),
                this.toIconUrls(response.iconUrls()));
    }

    private IconUrls toIconUrls(LabelIconUrlsResponse response) {
        return new IconUrls(response.small(), response.medium());
    }

}
