package com.chapeullah.guccigoblin.label.clan;

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
public class ClanLabelService {

    private final ClashOfClansClient client;

    private final ClanLabelRepository clanLabelRepository;

    public void syncClanLabels() {
        LabelsResponse response = client.getClanLabelsResponse();
        List<ClanLabel> clanLabels = response
                .items()
                .stream()
                .map(this::toClanLabel)
                .toList();
        clanLabelRepository.saveAll(clanLabels);
    }

    private ClanLabel toClanLabel(LabelResponse response) {
        return new ClanLabel(
                response.id(),
                response.name(),
                this.toIconUrls(response.iconUrls()));
    }

    private IconUrls toIconUrls(LabelIconUrlsResponse response) {
        return new IconUrls(response.small(), response.medium());
    }

}
