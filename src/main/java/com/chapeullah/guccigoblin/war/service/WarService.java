package com.chapeullah.guccigoblin.war.service;

import com.chapeullah.guccigoblin.ClashOfClansClient;
import com.chapeullah.guccigoblin.war.dto.WarClanResponse;
import com.chapeullah.guccigoblin.war.dto.WarResponse;
import com.chapeullah.guccigoblin.war.model.War;
import com.chapeullah.guccigoblin.war.repository.WarRepository;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WarService {

    private static final DateTimeFormatter WAR_TIME_FORMAT =
            DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss.SSSX");

    private final ClashOfClansClient client;

    private final WarRepository warRepository;
    private final Clock clock;


    @Transactional
    public Optional<War> syncCurrentWar(@NonNull String clanTag) {
        WarResponse response = client.getCurrentWar(clanTag);
        if (response == null) {
            throw new IllegalStateException("Empty current war response: clanTag=" + clanTag);
        }
        if (response.state().equals("notInWar")) {
            // TODO здесь стоит реализовать закрытие предыдущих войн. Бот может быть выключен во время войны, а потом включен, когда война уже закончилась и в таком случае проверить состояние последней войны что она isEnded или вообще стоит сделать защиту от таких случаев. Надо думать
            return Optional.empty();
        }

        War war = warRepository
                .findByClanTagAndOpponentTagAndStartTime(
                        response.clan().tag(),
                        response.opponent().tag(),
                        Instant.from(WAR_TIME_FORMAT.parse(response.startTime())))
                .orElse(null);
        if (war == null) {
            war = createWar(response);
        } else {
            updateWar(war, response);
        }

        return Optional.of(war);
    }

    @Transactional
    public void finishEndedWars() {
        var wars = warRepository.findAllByEndTimeLessThanEqualAndStateNot(clock.instant(), "warEnded");
        // TODO
        warRepository.saveAll(wars);
    }

    private War createWar(@NonNull WarResponse response) {

        return null;
    }

    private void updateWar(@NonNull War war, @NonNull WarResponse response) {

    }

    private War saveOrUpdateWar(WarResponse response) {
        WarClanResponse clanResponse = response.clan();
        WarClanResponse opponentClanResponse = response.opponent();

        Instant startTime = Instant.from(WAR_TIME_FORMAT.parse(response.startTime()));
        Instant endTime = Instant.from(WAR_TIME_FORMAT.parse(response.endTime()));

        // TODO

        return null;
    }

}
