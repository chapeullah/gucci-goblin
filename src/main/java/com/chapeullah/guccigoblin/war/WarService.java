package com.chapeullah.guccigoblin.war;

import com.chapeullah.guccigoblin.client.Client;
import com.chapeullah.guccigoblin.war.dto.AttackResponse;
import com.chapeullah.guccigoblin.war.dto.ClanResponse;
import com.chapeullah.guccigoblin.war.dto.MemberResponse;
import com.chapeullah.guccigoblin.war.dto.WarResponse;
import com.chapeullah.guccigoblin.war.model.War;
import com.chapeullah.guccigoblin.war.model.WarAttack;
import com.chapeullah.guccigoblin.war.model.WarParticipant;
import com.chapeullah.guccigoblin.war.repository.WarAttackRepository;
import com.chapeullah.guccigoblin.war.repository.WarParticipantRepository;
import com.chapeullah.guccigoblin.war.repository.WarRepository;
import jakarta.transaction.Transactional;
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

    private final Client client;

    private final WarRepository warRepository;
    private final WarParticipantRepository warParticipantRepository;
    private final WarAttackRepository warAttackRepository;
    private final Clock clock;


    @Transactional
    public Optional<War> syncWar() {
        WarResponse response = client.getCurrentWar();

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
        var wars = warRepository.findAllByEndsAtLessThanEqualAndStateNot(
                clock.instant(),
                "warEnded");
        for (War war : wars) {
            war.setState("warEnded");
        }
        warRepository.saveAll(wars);
    }

    private War saveOrUpdateWar(WarResponse response) {
        ClanResponse clanResponse = response.clan();
        ClanResponse opponent = response.opponent();

        Instant startsAt = Instant.from(
                WAR_TIME_FORMAT.parse(response.startTime()));
        Instant endsAt = Instant.from(
                WAR_TIME_FORMAT.parse(response.endTime()));

        War war = warRepository
                .findByClanTagAndOpponentTagAndStartsAt(
                        clanResponse.tag(),
                        opponent.tag(),
                        startsAt)
                .orElseGet(War::new);

        war.setClanTag(clanResponse.tag());
        war.setClanName(clanResponse.name());
        war.setClanAttacks(clanResponse.attacks());
        war.setClanStars(clanResponse.stars());
        war.setClanDestructionPercentage(clanResponse.destructionPercentage());
        war.setClanLevel(clanResponse.clanLevel());

        war.setOpponentTag(opponent.tag());
        war.setOpponentName(opponent.name());
        war.setOpponentAttacks(opponent.attacks());
        war.setOpponentStars(opponent.stars());
        war.setOpponentDestructionPercentage(opponent.destructionPercentage());
        war.setOpponentLevel(opponent.clanLevel());

        war.setStartsAt(startsAt);
        war.setEndsAt(endsAt);
        war.setState(war.isEnded(clock.instant()) ? "warEnded" : response.state());
        war.setTeamSize(response.teamSize());
        war.setAttacksPerMember(response.attacksPerMember());

        return warRepository.save(war);
    }

    private void saveParticipants(War war, ClanResponse clanResponse) {
        for (MemberResponse memberResponse : clanResponse.members()) {
            if (warParticipantRepository.existsByWarAndPlayerTag(
                    war, memberResponse.tag())) {
                continue;
            }
            WarParticipant participant = new WarParticipant(
                    war,
                    memberResponse.tag(),
                    memberResponse.name(),
                    clanResponse.tag(),
                    memberResponse.townhallLevel(),
                    memberResponse.mapPosition());
            warParticipantRepository.save(participant);
        }
    }

    private void saveAttacks(War war, WarResponse response) {
        Map<String, MemberResponse> membersByTag = new HashMap<>();
        for (MemberResponse memberResponse : response.clan().members())
            membersByTag.put(memberResponse.tag(), memberResponse);
        for (MemberResponse memberResponse : response.opponent().members())
            membersByTag.put(memberResponse.tag(), memberResponse);
        for (MemberResponse memberResponse : membersByTag.values()) {
            for (AttackResponse attackResponse : memberResponse.attacks()) {
                if (warAttackRepository
                        .existsByWarAndAttackOrder(war, attackResponse.order())) {
                    continue;
                }

                MemberResponse attacker =
                        membersByTag.get(attackResponse.attackerTag());
                MemberResponse defender =
                        membersByTag.get(attackResponse.defenderTag());

                if (attacker == null || defender == null) {
                    throw new IllegalStateException(
                            "Participant not found for attack #" + attackResponse.order());
                }

                WarAttack warAttack = new WarAttack(
                        war,
                        attackResponse.attackerTag(),
                        attacker.name(),
                        attackResponse.defenderTag(),
                        defender.name(),
                        attacker.townhallLevel(),
                        defender.townhallLevel(),
                        attackResponse.stars(),
                        attackResponse.destructionPercentage(),
                        attackResponse.order(),
                        attackResponse.duration());
                warAttackRepository.save(warAttack);
            }
        }
    }

}
