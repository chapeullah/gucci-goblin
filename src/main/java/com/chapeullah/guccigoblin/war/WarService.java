package com.chapeullah.guccigoblin.war;

import com.chapeullah.guccigoblin.ClashOfClansClient;
import com.chapeullah.guccigoblin.war.dto.WarAttackResponse;
import com.chapeullah.guccigoblin.war.dto.WarClanResponse;
import com.chapeullah.guccigoblin.war.dto.WarMemberResponse;
import com.chapeullah.guccigoblin.war.dto.WarResponse;
import com.chapeullah.guccigoblin.war.model.War;
import com.chapeullah.guccigoblin.war.model.WarAttack;
import com.chapeullah.guccigoblin.war.model.WarParticipant;
import com.chapeullah.guccigoblin.war.repository.WarAttackRepository;
import com.chapeullah.guccigoblin.war.repository.WarParticipantRepository;
import com.chapeullah.guccigoblin.war.repository.WarRepository;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WarService {

    private static final DateTimeFormatter WAR_TIME_FORMAT =
            DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss.SSSX");

    private final ClashOfClansClient client;

    private final WarRepository warRepository;
    private final WarParticipantRepository warParticipantRepository;
    private final WarAttackRepository warAttackRepository;
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

    private void saveParticipants(War war, WarClanResponse warClanResponse) {
        for (WarMemberResponse warMemberResponse : warClanResponse.members()) {
            if (warParticipantRepository.existsByWarAndPlayerTag(war, warMemberResponse.tag())) {
                continue;
            }
            WarParticipant participant = new WarParticipant(
                    war,
                    warMemberResponse.tag(),
                    warMemberResponse.name(),
                    warClanResponse.tag(),
                    warMemberResponse.townHallLevel(),
                    warMemberResponse.mapPosition());
            warParticipantRepository.save(participant);
        }
    }

    private void saveAttacks(War war, WarResponse response) {
        Map<String, WarMemberResponse> membersByTag = new HashMap<>();
        for (WarMemberResponse warMemberResponse : response.clan().members())
            membersByTag.put(warMemberResponse.tag(), warMemberResponse);
        for (WarMemberResponse warMemberResponse : response.opponent().members())
            membersByTag.put(warMemberResponse.tag(), warMemberResponse);
        for (WarMemberResponse warMemberResponse : membersByTag.values()) {
            for (WarAttackResponse warAttackResponse : warMemberResponse.attacks()) {
                if (warAttackRepository
                        .existsByWarAndAttackOrder(war, warAttackResponse.order())) {
                    continue;
                }

                WarMemberResponse attacker =
                        membersByTag.get(warAttackResponse.attackerTag());
                WarMemberResponse defender =
                        membersByTag.get(warAttackResponse.defenderTag());

                if (attacker == null || defender == null) {
                    throw new IllegalStateException(
                            "Participant not found for attack #" + warAttackResponse.order());
                }

                WarAttack warAttack = new WarAttack(
                        war,
                        warAttackResponse.attackerTag(),
                        attacker.name(),
                        warAttackResponse.defenderTag(),
                        defender.name(),
                        attacker.townHallLevel(),
                        defender.townHallLevel(),
                        warAttackResponse.stars(),
                        warAttackResponse.destructionPercentage(),
                        warAttackResponse.order(),
                        warAttackResponse.duration());
                warAttackRepository.save(warAttack);
            }
        }
    }

}
