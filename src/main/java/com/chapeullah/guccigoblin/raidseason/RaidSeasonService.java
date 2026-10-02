package com.chapeullah.guccigoblin.raidseason;

import com.chapeullah.guccigoblin.ClashOfClansClient;
import com.chapeullah.guccigoblin.raidseason.dto.RaidSeasonResponse;
import com.chapeullah.guccigoblin.raidseason.model.RaidSeason;
import com.chapeullah.guccigoblin.raidseason.model.RaidSeasonAttack;
import com.chapeullah.guccigoblin.raidseason.model.RaidSeasonParticipant;
import com.chapeullah.guccigoblin.raidseason.repository.RaidSeasonAttackRepository;
import com.chapeullah.guccigoblin.raidseason.repository.RaidSeasonParticipantRepository;
import com.chapeullah.guccigoblin.raidseason.repository.RaidSeasonRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@DependsOn("environmentVariablesValidator")
public class RaidSeasonService {

    private static final DateTimeFormatter RAID_TIME_FORMAT =
            DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss.SSSX");

    private final ClashOfClansClient client;
    private final RaidSeasonRepository raidSeasonRepository;
    private final RaidSeasonParticipantRepository raidSeasonParticipantRepository;
    private final RaidSeasonAttackRepository raidSeasonAttackRepository;

    @Value("${coc.clan-tag}")
    private String clanTag;

    @Transactional
    public Optional<RaidSeason> syncRaidSeason() {
        RaidSeasonResponse response = client.getCurrentRaid();

        if (response == null || response.items() == null) {
            throw new IllegalStateException("Некорректный ответ API рейдов");
        }

        if (response.items().isEmpty()) {
            return Optional.empty();
        }

        RaidSeasonResponse.Season dto =
                required(response.items().getFirst(), "items[0]");

        RaidSeason season = saveOrUpdateSeason(dto);

        if (dto.members() == null) {
            log.warn("У сезона {} отсутствует members. Сохранённые участники и атаки не изменены",
                    season.getStartTime());

            return Optional.of(season);
        }

        Map<String, RaidSeasonParticipant> participantsByTag =
                saveOrUpdateParticipants(season, dto.members());

        syncAttacksIfComplete(season, dto, participantsByTag);

        return Optional.of(season);
    }

    private RaidSeason saveOrUpdateSeason(RaidSeasonResponse.Season dto) {
        Instant startTime = Instant.from(
                RAID_TIME_FORMAT.parse(required(dto.startTime(), "startTime")));

        Instant endTime = Instant.from(
                RAID_TIME_FORMAT.parse(required(dto.endTime(), "endTime")));

        if (endTime.isBefore(startTime)) {
            throw new IllegalStateException(
                    "Окончание рейдового сезона раньше его начала");
        }

        RaidSeason season = raidSeasonRepository
                .findByClanTagAndStartTime(clanTag, startTime)
                .orElseGet(RaidSeason::new);

        season.setClanTag(clanTag);
        season.setState(required(dto.state(), "state"));
        season.setStartTime(startTime);
        season.setEndTime(endTime);

        season.setCapitalTotalLoot(
                required(dto.capitalTotalLoot(), "capitalTotalLoot"));

        season.setRaidsCompleted(
                required(dto.raidsCompleted(), "raidsCompleted"));

        season.setTotalAttacks(
                required(dto.totalAttacks(), "totalAttacks"));

        season.setEnemyDistrictsDestroyed(
                required(dto.enemyDistrictsDestroyed(),
                        "enemyDistrictsDestroyed"));

        season.setOffensiveReward(
                required(dto.offensiveReward(), "offensiveReward"));

        season.setDefensiveReward(
                required(dto.defensiveReward(), "defensiveReward"));

        return raidSeasonRepository.save(season);
    }

    private Map<String, RaidSeasonParticipant> saveOrUpdateParticipants(
            RaidSeason season,
            List<RaidSeasonResponse.Member> members) {
        Map<String, RaidSeasonParticipant> existingByTag = new HashMap<>();

        for (RaidSeasonParticipant participant :
                raidSeasonParticipantRepository.findAllByRaidSeason(season)) {
            existingByTag.put(participant.getTag(), participant);
        }

        Map<String, RaidSeasonParticipant> participantsByTag = new HashMap<>();

        for (RaidSeasonResponse.Member member : members) {
            required(member, "members[]");

            String tag = required(member.tag(), "members[].tag");

            if (participantsByTag.containsKey(tag)) {
                throw new IllegalStateException("Повторяющийся участник рейдового сезона: " + tag);
            }

            RaidSeasonParticipant participant = existingByTag.get(tag);

            if (participant == null) {
                participant = new RaidSeasonParticipant();
            }

            participant.setRaidSeason(season);
            participant.setTag(tag);
            participant.setName(required(member.name(), "members[].name"));
            participant.setAttacks(required(member.attacks(), "members[].attacks"));
            participant.setAttackLimit(required(member.attackLimit(), "members[].attackLimit"));
            participant.setBonusAttackLimit(required(member.bonusAttackLimit(), "members[].bonusAttackLimit"));
            participant.setCapitalResourcesLooted(required(member.capitalResourcesLooted(), "members[].capitalResourcesLooted"));

            participantsByTag.put(tag, participant);
        }

        raidSeasonParticipantRepository.saveAll(participantsByTag.values());

        return participantsByTag;
    }

    private void syncAttacksIfComplete(
            RaidSeason season,
            RaidSeasonResponse.Season dto,
            Map<String, RaidSeasonParticipant> participantsByTag) {
        if (dto.attackLog() == null) {
            log.warn("У сезона {} отсутствует attackLog. Старые атаки сохранены", season.getStartTime());
            return;
        }

        List<RaidSeasonAttack> attacks = new ArrayList<>();
        Map<String, Integer> attackCountsByTag = new HashMap<>();

        for (RaidSeasonResponse.AttackLogEntry entry : dto.attackLog()) {
            if (entry == null
                    || entry.defender() == null
                    || entry.districts() == null) {

                log.warn("У сезона {} неполный attackLog. Старые атаки сохранены",
                        season.getStartTime());
                return;
            }

            RaidSeasonResponse.Defender defender = entry.defender();

            for (RaidSeasonResponse.District district : entry.districts()) {
                required(district, "attackLog[].districts[]");

                if (district.attacks() == null) {
                    continue;
                }

                for (RaidSeasonResponse.Attack attackDto : district.attacks()) {
                    required(attackDto, "districts[].attacks[]");

                    RaidSeasonResponse.Attacker attacker =
                            required(attackDto.attacker(), "attacks[].attacker");

                    String attackerTag =
                            required(attacker.tag(), "attacks[].attacker.tag");

                    RaidSeasonParticipant participant =
                            participantsByTag.get(attackerTag);

                    if (participant == null) {
                        log.warn(
                                "У сезона {} нет участника {} из журнала атак. "
                                        + "Старые атаки сохранены",
                                season.getStartTime(),
                                attackerTag);
                        return;
                    }

                    RaidSeasonAttack attack = new RaidSeasonAttack(
                            participant,
                            required(defender.tag(), "defender.tag"),
                            required(defender.name(), "defender.name"),
                            required(district.id(), "district.id"),
                            required(district.name(), "district.name"),
                            required(district.districtHallLevel(),
                                    "district.districtHallLevel"),
                            required(attackDto.stars(), "attack.stars"),
                            required(attackDto.destructionPercent(),
                                    "attack.destructionPercent"));

                    attacks.add(attack);

                    attackCountsByTag.merge(
                            attackerTag,
                            1,
                            Integer::sum);
                }
            }
        }

        if (attacks.size() != season.getTotalAttacks()) {
            log.warn(
                    "У сезона {} получено {} атак, ожидалось {}. "
                            + "Старые атаки сохранены",
                    season.getStartTime(),
                    attacks.size(),
                    season.getTotalAttacks());
            return;
        }

        for (RaidSeasonParticipant participant : participantsByTag.values()) {
            int actualCount = attackCountsByTag.getOrDefault(
                    participant.getTag(), 0);

            if (actualCount != participant.getAttacks()) {
                log.warn(
                        "У сезона {} для игрока {} получено {} атак, "
                                + "ожидалось {}. Старые атаки сохранены",
                        season.getStartTime(),
                        participant.getTag(),
                        actualCount,
                        participant.getAttacks());
                return;
            }
        }

        raidSeasonAttackRepository.deleteAllByParticipant_RaidSeason(season);
        raidSeasonAttackRepository.flush();
        raidSeasonAttackRepository.saveAll(attacks);

        log.info(
                "Сезон {} синхронизирован: участников {}, атак {}",
                season.getStartTime(),
                participantsByTag.size(),
                attacks.size());
    }

    private static <T> T required(T value, String field) {
        if (value == null) {
            throw new IllegalStateException("В ответе API рейдов отсутствует обязательное поле: " + field);
        }

        return value;
    }
}