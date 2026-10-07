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
    public Optional<War> syncWar(@NonNull String clanTag) {
        WarResponse response = client.getCurrentWar(clanTag);

        if (response == null) {
            throw new IllegalStateException("Empty current war response");
        }

        if ("notInWar".equals(response.state())) {
            return Optional.empty();
        }
        War war = saveOrUpdateWar(response);
        saveParticipants(war, response.clan());
        saveParticipants(war, response.opponent());
        saveAttacks(war, response);
        return Optional.of(war);
    }

    @Transactional
    public void finishEndedWars() {
        var wars = warRepository.findAllByEndTimeLessThanEqualAndStateNot(
                clock.instant(),
                "warEnded");
        for (War war : wars) {
            war.setState("warEnded");
        }
        warRepository.saveAll(wars);
    }

    private War saveOrUpdateWar(WarResponse response) {
        WarClanResponse warClanResponse = response.clan();
        WarClanResponse opponent = response.opponent();

        Instant startTime = Instant.from(
                WAR_TIME_FORMAT.parse(response.startTime()));
        Instant endTime = Instant.from(
                WAR_TIME_FORMAT.parse(response.endTime()));

        War war = warRepository
                .findByClanTagAndOpponentTagAndStartTime(
                        warClanResponse.tag(),
                        opponent.tag(),
                        startTime)
                .orElseGet(War::new);

        war.setClanTag(warClanResponse.tag());
        war.setClanName(warClanResponse.name());
        war.setClanAttacks(warClanResponse.attacks());
        war.setClanStars(warClanResponse.stars());
        war.setClanDestructionPercentage(warClanResponse.destructionPercentage());
        war.setClanLevel(warClanResponse.clanLevel());

        war.setOpponentTag(opponent.tag());
        war.setOpponentName(opponent.name());
        war.setOpponentAttacks(opponent.attacks());
        war.setOpponentStars(opponent.stars());
        war.setOpponentDestructionPercentage(opponent.destructionPercentage());
        war.setOpponentLevel(opponent.clanLevel());

        war.setStartTime(startTime);
        war.setEndTime(endTime);
        war.setState(war.isEnded(clock.instant()) ? "warEnded" : response.state());
        war.setTeamSize(response.teamSize());
        war.setAttacksPerMember(response.attacksPerMember());

        return war;
    }

    private void saveParticipants(War war, WarClanResponse warClanResponse) {
        for (WarMemberResponse warMemberResponse : warClanResponse.members()) {
            if (warParticipantRepository.existsByWarAndPlayerTag(
                    war, warMemberResponse.tag())) {
                continue;
            }
            WarParticipant participant = new WarParticipant(
                    war,
                    warMemberResponse.tag(),
                    warMemberResponse.name(),
                    warClanResponse.tag(),
                    warMemberResponse.townhallLevel(),
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
                        attacker.townhallLevel(),
                        defender.townhallLevel(),
                        warAttackResponse.stars(),
                        warAttackResponse.destructionPercentage(),
                        warAttackResponse.order(),
                        warAttackResponse.duration());
                warAttackRepository.save(warAttack);
            }
        }
    }

}
