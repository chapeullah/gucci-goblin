package com.chapeullah.guccigoblin.war;

import com.chapeullah.guccigoblin.war.dto.WarClanResponse;
import com.chapeullah.guccigoblin.war.model.War;
import com.chapeullah.guccigoblin.war.model.WarParticipant;
import com.chapeullah.guccigoblin.war.repository.WarParticipantRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class WarParticipantService {

    private final WarParticipantRepository warParticipantRepository;

    @Transactional
    public void syncWarParticipants(
            @NonNull War war,
            @NonNull WarClanResponse clanResponse,
            @NonNull WarClanResponse opponentResponse) {
        if (clanResponse.members() == null || clanResponse.members().isEmpty()) {
            throw new IllegalArgumentException(
                    "Clan members cannot be null or empty: clanTag=" + clanResponse.tag());
        }
        if (opponentResponse.members() == null || opponentResponse.members().isEmpty()) {
            throw new IllegalArgumentException(
                    "Opponent clan members cannot be null or empty: clanTag=" + opponentResponse.tag());
        }
        if (!Objects.equals(war.getClanTag(), clanResponse.tag())) {
            throw new IllegalArgumentException(
                    "Clan tag mismatch: expected clanTag=\""
                            + war.getClanTag()
                            + "\", actual clanTag=\""
                            + clanResponse.tag() + "\"");
        }
        if (!Objects.equals(war.getOpponentClanTag(), opponentResponse.tag())) {
            throw new IllegalArgumentException(
                    "Opponent clan tag mismatch: expected clanTag=\""
                            + war.getOpponentClanTag()
                            + "\", actual clanTag=\""
                            + opponentResponse.tag() + "\"");
        }
        if (clanResponse.members().size() != war.getTeamSize()
                || opponentResponse.members().size() != war.getTeamSize()) {
            throw new IllegalArgumentException(
                    "War participants count mismatch: expected teamSize=" + war.getTeamSize()
                            + ", clanTeamSize=" + clanResponse.members().size()
                            + ", opponentTeamSize=" + opponentResponse.members().size()
            );
        }

        if (!warParticipantRepository.existsByWar(war)) {
            createParticipants(war, clanResponse, opponentResponse);
        } else {
            updateParticipants();
        }
    }

    private void createParticipants(
            War war,
            WarClanResponse clanResponse,
            WarClanResponse opponentResponse) {
        List<WarParticipant> warParticipants = new ArrayList<>(war.getTeamSize() * 2);

        clanResponse.members().forEach(member -> warParticipants.add(
                new WarParticipant(
                        war,
                        member.tag(),
                        member.name(),
                        clanResponse.tag(),
                        member.townHallLevel(),
                        member.mapPosition())));
        opponentResponse.members().forEach(member -> warParticipants.add(
                new WarParticipant(war,
                        member.tag(),
                        member.name(),
                        opponentResponse.tag(),
                        member.townHallLevel(),
                        member.mapPosition())));

        warParticipantRepository.saveAll(warParticipants);
    }

    private void updateParticipants() {

    }

}
