package com.chapeullah.guccigoblin.clan;

import com.chapeullah.guccigoblin.clan.model.Clan;
import com.chapeullah.guccigoblin.clan.model.ClanLabelLink;
import com.chapeullah.guccigoblin.label.dto.LabelResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClanLabelLinkService {

    private final ClanLabelLinkRepository clanLabelLinkRepository;

    @Transactional(readOnly = true)
    public List<ClanLabelLink> getAllByClanTag(@NonNull String clanTag) {
        return clanLabelLinkRepository.findAllByClanTag(clanTag);
    }

    @Transactional
    public List<ClanLabelLink> syncAndGetByClan(
            @NonNull Clan clan,
            @NonNull List<LabelResponse> responses) {
        // TODO
        return List.of();
    }

}
