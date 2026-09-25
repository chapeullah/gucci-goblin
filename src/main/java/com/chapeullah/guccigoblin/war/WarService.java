package com.chapeullah.guccigoblin.war;

import com.chapeullah.guccigoblin.war.dto.Attack;
import com.chapeullah.guccigoblin.war.dto.Clan;
import com.chapeullah.guccigoblin.war.dto.Member;
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

    private final WarRepository warRepository;
    private final WarParticipantRepository warParticipantRepository;
    private final WarAttackRepository warAttackRepository;


    @Transactional
    public Optional<War> syncWar(WarResponse response) {
        if ("notInWar".equals(response.state()))
            return Optional.empty();
        War war = saveOrUpdateWar(response);
        saveParticipants(war, response.clan());
        saveParticipants(war, response.opponent());
        saveAttacks(war, response);
        return Optional.of(war);
    }

    private War saveOrUpdateWar(WarResponse response) {
        Clan clan = response.clan();
        Clan opponent = response.opponent();

        Instant startsAt = Instant.from(
                WAR_TIME_FORMAT.parse(response.startTime()));
        Instant endsAt = Instant.from(
                WAR_TIME_FORMAT.parse(response.endTime()));

        War war = warRepository
                .findByClanTagAndOpponentTagAndStartsAt(
                        clan.tag(),
                        opponent.tag(),
                        startsAt)
                .orElseGet(War::new);

        war.setClanTag(clan.tag());
        war.setClanName(clan.name());
        war.setClanAttacks(clan.attacks());
        war.setClanStars(clan.stars());
        war.setClanDestructionPercentage(clan.destructionPercentage());
        war.setClanLevel(clan.clanLevel());

        war.setOpponentTag(opponent.tag());
        war.setOpponentName(opponent.name());
        war.setOpponentAttacks(opponent.attacks());
        war.setOpponentStars(opponent.stars());
        war.setOpponentDestructionPercentage(opponent.destructionPercentage());
        war.setOpponentLevel(opponent.clanLevel());

        war.setState(response.state());
        war.setStartsAt(startsAt);
        war.setEndsAt(endsAt);
        war.setTeamSize(response.teamSize());
        war.setAttacksPerMember(response.attacksPerMember());

        return warRepository.save(war);
    }

    private void saveParticipants(War war, Clan clan) {
        for (Member member : clan.members()) {
            if (warParticipantRepository.existsByWarAndPlayerTag(
                    war, member.tag()))
                continue;
            WarParticipant participant = new WarParticipant(
                    war,
                    member.tag(),
                    member.name(),
                    clan.tag(),
                    member.townhallLevel(),
                    member.mapPosition());
            warParticipantRepository.save(participant);
        }
    }

    private void saveAttacks(War war, WarResponse response) {
        Map<String, Member> membersByTag = new HashMap<>();
        for (Member member : response.clan().members())
            membersByTag.put(member.tag(), member);
        for (Member member : response.opponent().members())
            membersByTag.put(member.tag(), member);
        for (Member member : membersByTag.values()) {
            for (Attack attack : member.attacks()) {
                if (warAttackRepository.existsByWarAndAttackOrder(
                        war, attack.order()))
                    continue;

                Member attacker =
                        membersByTag.get(attack.attackerTag());
                Member defender =
                        membersByTag.get(attack.defenderTag());

                if (attacker == null || defender == null)
                    throw new IllegalStateException(
                            "Participant not found for attack #" + attack.order());

                WarAttack warAttack = new WarAttack(
                        war,
                        attack.attackerTag(),
                        attack.defenderTag(),
                        attacker.townhallLevel(),
                        defender.townhallLevel(),
                        attack.stars(),
                        attack.destructionPercentage(),
                        attack.order(),
                        attack.duration());
                warAttackRepository.save(warAttack);
            }
        }
    }

}
