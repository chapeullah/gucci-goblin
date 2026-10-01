package com.chapeullah.guccigoblin.war;

import com.chapeullah.guccigoblin.ClashOfClansClient;
import com.chapeullah.guccigoblin.config.Scheduler;
import com.chapeullah.guccigoblin.raidseason.RaidSeasonService;
import com.chapeullah.guccigoblin.raidseason.model.RaidSeason;
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
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tools.jackson.databind.json.JsonMapper;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class WarServicePersistenceTest {
    private static final Instant BATTLE_END = Instant.parse("2026-09-26T12:00:00Z");
    private static final Instant TEST_TIME = BATTLE_END.minusSeconds(3600);

    private static AnnotationConfigApplicationContext context;
    private WarService service;
    private MutableClock clock;
    private StubClashOfClansClient client;
    private WarRepository wars;
    private WarParticipantRepository participants;
    private WarAttackRepository attacks;
    private WarResponse battle;

    @BeforeAll
    static void startPersistence() {
        context = new AnnotationConfigApplicationContext(PersistenceConfiguration.class);
    }

    @AfterAll
    static void closePersistence() {
        if (context != null) context.close();
    }

    @BeforeEach
    void resetData() throws Exception {
        service = context.getBean(WarService.class);
        clock = context.getBean(MutableClock.class);
        clock.setInstant(TEST_TIME);
        client = context.getBean(StubClashOfClansClient.class);
        wars = context.getBean(WarRepository.class);
        participants = context.getBean(WarParticipantRepository.class);
        attacks = context.getBean(WarAttackRepository.class);
        attacks.deleteAllInBatch();
        participants.deleteAllInBatch();
        wars.deleteAllInBatch();
        client.failure = null;
        battle = JsonMapper.builder().build().readValue(
                new ClassPathResource("fixtures/war-in-war.json").getContentAsString(StandardCharsets.UTF_8),
                WarResponse.class);
        client.response = battle;
    }

    @Test
    void persistsWarParticipantsAndAttacksFromBothClans() {
        War saved = service.syncWar().orElseThrow();
        War stored = wars.findById(saved.getId()).orElseThrow();
        assertEquals("#HOME", stored.getClanTag());
        assertEquals("Test Home Clan", stored.getClanName());
        assertEquals(10, stored.getClanLevel());
        assertEquals(1, stored.getClanAttacks());
        assertEquals(2, stored.getClanStars());
        assertEquals(30.25, stored.getClanDestructionPercentage());
        assertEquals("#AWAY", stored.getOpponentTag());
        assertEquals("Test Away Clan", stored.getOpponentName());
        assertEquals(12, stored.getOpponentLevel());
        assertEquals(1, stored.getOpponentAttacks());
        assertEquals(3, stored.getOpponentStars());
        assertEquals(50.0, stored.getOpponentDestructionPercentage());
        assertEquals("inWar", stored.getState());
        assertEquals(2, stored.getTeamSize());
        assertEquals(2, stored.getAttacksPerMember());
        assertEquals(Instant.parse("2026-09-25T12:00:00Z"), stored.getStartTime());
        assertEquals(Instant.parse("2026-09-26T12:00:00Z"), stored.getEndTime());

        Map<String, WarParticipant> roster = participants.findAll().stream()
                .collect(Collectors.toMap(WarParticipant::getPlayerTag, Function.identity()));
        assertEquals(4, roster.size());
        assertParticipant(roster.get("#A"), saved.getId(), "#HOME", 15, 1);
        assertParticipant(roster.get("#B"), saved.getId(), "#HOME", 14, 2);
        assertParticipant(roster.get("#X"), saved.getId(), "#AWAY", 16, 1);
        assertParticipant(roster.get("#Y"), saved.getId(), "#AWAY", 13, 2);

        Map<Integer, WarAttack> storedAttacks = attacksByOrder();
        assertEquals(2, storedAttacks.size());
        WarAttack homeAttack = storedAttacks.get(1);
        assertEquals(saved.getId(), homeAttack.getWar().getId());
        assertEquals("#A", homeAttack.getAttackerTag());
        assertEquals("#X", homeAttack.getDefenderTag());
        assertEquals(15, homeAttack.getAttackerTH());
        assertEquals(16, homeAttack.getDefenderTH());
        assertEquals(2, homeAttack.getStars());
        assertEquals(60.5, homeAttack.getDestructionPercentage());
        assertEquals(123, homeAttack.getDurationSeconds());
        WarAttack awayAttack = storedAttacks.get(2);
        assertEquals("#X", awayAttack.getAttackerTag());
        assertEquals("#A", awayAttack.getDefenderTag());
        assertEquals(16, awayAttack.getAttackerTH());
        assertEquals(15, awayAttack.getDefenderTH());
        assertEquals(3, awayAttack.getStars());
        assertEquals(100.0, awayAttack.getDestructionPercentage());
        assertEquals(170, awayAttack.getDurationSeconds());
    }

    @Test
    void replayingTheSameResponseDoesNotDuplicateRows() {
        Long id = service.syncWar().orElseThrow().getId();
        var attackIds = attacksByOrder().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getId()));
        var participantIds = participants.findAll().stream()
                .collect(Collectors.toMap(WarParticipant::getPlayerTag, WarParticipant::getId));

        assertEquals(id, service.syncWar().orElseThrow().getId());

        assertCounts(1, 4, 2);
        attacksByOrder().forEach((order, attack) -> assertEquals(attackIds.get(order), attack.getId()));
        participants.findAll().forEach(participant ->
                assertEquals(participantIds.get(participant.getPlayerTag()), participant.getId()));
    }

    @Test
    void addsOnlyNewAttacksAndUpdatesWarTotals() {
        Long warId = service.syncWar().orElseThrow().getId();
        Long firstAttackId = attacksByOrder().get(1).getId();
        client.response = withAdditionalHomeAttacks(List.of(
                new AttackResponse("#A", "#Y", 1, 44.5, 3, 77)), false);

        service.syncWar();
        service.syncWar();

        assertCounts(1, 4, 3);
        assertEquals(firstAttackId, attacksByOrder().get(1).getId());
        WarAttack added = attacksByOrder().get(3);
        assertEquals(warId, added.getWar().getId());
        assertEquals(15, added.getAttackerTH());
        assertEquals(13, added.getDefenderTH());
        assertEquals(77, added.getDurationSeconds());
        War updated = wars.findById(warId).orElseThrow();
        assertEquals(2, updated.getClanAttacks());
        assertEquals(3, updated.getClanStars());
        assertEquals(52.5, updated.getClanDestructionPercentage());
    }

    @Test
    void updatesOneWarThroughPreparationBattleAndEnd() {
        client.response = withStateAndClans("preparation",
                withoutAttacks(battle.clan()), withoutAttacks(battle.opponent()));
        Long warId = service.syncWar().orElseThrow().getId();
        assertCounts(1, 4, 0);
        assertEquals("preparation", wars.findById(warId).orElseThrow().getState());

        client.response = battle;
        assertEquals(warId, service.syncWar().orElseThrow().getId());
        assertCounts(1, 4, 2);
        assertEquals("inWar", wars.findById(warId).orElseThrow().getState());

        client.response = withStateAndClans("warEnded", battle.clan(), battle.opponent());
        assertEquals(warId, service.syncWar().orElseThrow().getId());
        assertCounts(1, 4, 2);
        assertEquals("warEnded", wars.findById(warId).orElseThrow().getState());
    }

    @Test
    void createsSeparateWarForANewStartTime() {
        Long firstId = service.syncWar().orElseThrow().getId();
        client.response = new WarResponse(battle.state(), battle.teamSize(), battle.attacksPerMember(),
                battle.battleModifier(), "20260926T120000.000Z", "20260927T120000.000Z",
                "20260928T120000.000Z", battle.clan(), battle.opponent());

        Long secondId = service.syncWar().orElseThrow().getId();

        assertNotEquals(firstId, secondId);
        assertCounts(2, 8, 4);
        assertEquals(2, attacks.findAll().stream()
                .filter(attack -> attack.getAttackOrder() == 1).count());
    }

    @Test
    void notInWarDoesNotCreateOrDeleteHistory() {
        WarResponse notInWar = new WarResponse("notInWar", null, null, null, null, null, null, null, null);
        client.response = notInWar;
        assertTrue(service.syncWar().isEmpty());
        assertCounts(0, 0, 0);

        client.response = battle;
        Long id = service.syncWar().orElseThrow().getId();
        client.response = notInWar;
        assertTrue(service.syncWar().isEmpty());
        assertTrue(wars.existsById(id));
        assertCounts(1, 4, 2);
    }

    @Test
    void emptyResponseFailsWithoutWritingRows() {
        client.response = null;
        IllegalStateException error = assertThrows(IllegalStateException.class, service::syncWar);
        assertEquals("Empty current war response", error.getMessage());
        assertCounts(0, 0, 0);
    }

    @Test
    void unknownParticipantRollsBackTheWholeNewWar() {
        client.response = withAdditionalHomeAttacks(List.of(
                new AttackResponse("#A", "#MISSING", 1, 40.0, 3, 100)), false);
        IllegalStateException error = assertThrows(IllegalStateException.class, service::syncWar);
        assertEquals("Participant not found for attack #3", error.getMessage());
        assertCounts(0, 0, 0);
    }

    @Test
    void failedUpdatePreservesPreviouslySavedWarAndAttacks() {
        Long warId = service.syncWar().orElseThrow().getId();
        client.response = withAdditionalHomeAttacks(List.of(
                new AttackResponse("#A", "#Y", 1, 44.5, 3, 77),
                new AttackResponse("#A", "#MISSING", 1, 40.0, 4, 100)), true);

        assertThrows(IllegalStateException.class, service::syncWar);

        assertCounts(1, 4, 2);
        War stored = wars.findById(warId).orElseThrow();
        assertEquals("Test Home Clan", stored.getClanName());
        assertEquals(1, stored.getClanAttacks());
        assertEquals(2, stored.getClanStars());
        assertEquals(30.25, stored.getClanDestructionPercentage());
        assertFalse(participants.existsByWarAndPlayerTag(stored, "#C"));
        assertFalse(attacks.existsByWarAndAttackOrder(stored, 3));
    }

    @Test
    void invalidTimeFailsWithoutWritingRows() {
        client.response = new WarResponse(battle.state(), battle.teamSize(), battle.attacksPerMember(),
                battle.battleModifier(), battle.preparationStartTime(), "not-a-date", battle.endTime(),
                battle.clan(), battle.opponent());
        assertThrows(DateTimeParseException.class, service::syncWar);
        assertCounts(0, 0, 0);
    }

    @Test
    void apiFailureDoesNotChangeSavedWar() {
        Long warId = service.syncWar().orElseThrow().getId();
        client.failure = new IllegalStateException("Simulated API failure");
        assertSame(client.failure, assertThrows(IllegalStateException.class, service::syncWar));
        assertCounts(1, 4, 2);
        assertEquals("inWar", wars.findById(warId).orElseThrow().getState());
    }

    @ParameterizedTest
    @CsvSource({
            "2026-09-26T11:59:59.999999999Z, inWar",
            "2026-09-26T12:00:00Z, warEnded",
            "2026-09-26T12:00:00.000000001Z, warEnded"
    })
    void determinesWarStateUsingTheInjectedClock(String now, String expectedState) {
        clock.setInstant(Instant.parse(now));

        Long warId = service.syncWar().orElseThrow().getId();

        assertEquals(expectedState, wars.findById(warId).orElseThrow().getState());
        assertCounts(1, 4, 2);
    }

    @Test
    void finishesOnlyExpiredWarsIncludingTheBoundaryAndDoesNotRewriteThem() {
        War expiredBattle = saveWar("inWar", TEST_TIME.minusSeconds(60));
        War expiredPreparation = saveWar("preparation", TEST_TIME.minusSeconds(120));
        War boundary = saveWar("inWar", TEST_TIME);
        War future = saveWar("inWar", TEST_TIME.plusSeconds(60));
        War complete = saveWar("warEnded", TEST_TIME.minusSeconds(180));
        Instant futureUpdatedAt = wars.findById(future.getId()).orElseThrow().getUpdatedAt();
        Instant completeUpdatedAt = wars.findById(complete.getId()).orElseThrow().getUpdatedAt();

        service.finishEndedWars();

        for (War expired : List.of(expiredBattle, expiredPreparation, boundary)) {
            assertEquals("warEnded", wars.findById(expired.getId()).orElseThrow().getState());
        }
        War storedFuture = wars.findById(future.getId()).orElseThrow();
        assertEquals("inWar", storedFuture.getState());
        assertEquals(futureUpdatedAt, storedFuture.getUpdatedAt());
        War storedComplete = wars.findById(complete.getId()).orElseThrow();
        assertEquals("warEnded", storedComplete.getState());
        assertEquals(completeUpdatedAt, storedComplete.getUpdatedAt());
        Map<Long, Instant> updateTimes = wars.findAll().stream()
                .collect(Collectors.toMap(War::getId, War::getUpdatedAt));

        clock.setInstant(TEST_TIME.plusSeconds(30));
        service.finishEndedWars();

        assertCounts(5, 0, 0);
        assertEquals(updateTimes, wars.findAll().stream()
                .collect(Collectors.toMap(War::getId, War::getUpdatedAt)));
    }

    @Test
    void schedulerFinishesSavedWarEvenWhenTheApiFails() {
        Long warId = service.syncWar().orElseThrow().getId();
        clock.setInstant(BATTLE_END.plusSeconds(60));
        client.failure = new IllegalStateException("Simulated API failure");

        assertDoesNotThrow(() -> scheduler().sync());

        assertEquals("warEnded", wars.findById(warId).orElseThrow().getState());
        assertCounts(1, 4, 2);
    }

    @Test
    void schedulerFinishesPreviousWarWhenTheApiAlreadyReturnsANewWar() {
        Long previousWarId = service.syncWar().orElseThrow().getId();
        clock.setInstant(BATTLE_END.plusSeconds(60));
        ClanResponse nextOpponent = withoutAttacks(battle.opponent());
        nextOpponent = new ClanResponse("#NEXT", nextOpponent.name(), nextOpponent.clanLevel(),
                nextOpponent.attacks(), nextOpponent.stars(), nextOpponent.destructionPercentage(),
                nextOpponent.members());
        client.response = new WarResponse("preparation", battle.teamSize(), battle.attacksPerMember(),
                battle.battleModifier(), "20260926T120000.000Z", "20260927T120000.000Z",
                "20260928T120000.000Z", withoutAttacks(battle.clan()), nextOpponent);

        assertDoesNotThrow(() -> scheduler().sync());

        assertEquals("warEnded", wars.findById(previousWarId).orElseThrow().getState());
        War current = wars.findByClanTagAndOpponentTagAndStartTime(
                "#HOME", "#NEXT", Instant.parse("2026-09-27T12:00:00Z")).orElseThrow();
        assertNotEquals(previousWarId, current.getId());
        assertEquals("preparation", current.getState());
        assertCounts(2, 8, 2);
    }

    @Test
    void schedulerFinishesPreviousWarWhenTheApiReturnsNotInWar() {
        Long warId = service.syncWar().orElseThrow().getId();
        clock.setInstant(BATTLE_END.plusSeconds(60));
        client.response = new WarResponse("notInWar", null, null, null, null, null, null, null, null);

        assertDoesNotThrow(() -> scheduler().sync());

        assertEquals("warEnded", wars.findById(warId).orElseThrow().getState());
        assertCounts(1, 4, 2);
    }

    @Test
    void endedWarStillAcceptsLateAttacksWithoutStateRegressionOrDuplicates() {
        Long warId = service.syncWar().orElseThrow().getId();
        Long firstAttackId = attacksByOrder().get(1).getId();
        clock.setInstant(BATTLE_END.plusSeconds(60));
        service.finishEndedWars();
        client.response = withAdditionalHomeAttacks(List.of(
                new AttackResponse("#A", "#Y", 1, 44.5, 3, 77)), false);

        assertEquals(warId, service.syncWar().orElseThrow().getId());
        service.syncWar();

        War stored = wars.findById(warId).orElseThrow();
        assertEquals("warEnded", stored.getState());
        assertEquals(2, stored.getClanAttacks());
        assertEquals(3, stored.getClanStars());
        assertEquals(firstAttackId, attacksByOrder().get(1).getId());
        assertEquals("#Y", attacksByOrder().get(3).getDefenderTag());
        assertCounts(1, 4, 3);
    }

    private War saveWar(String state, Instant endTime) {
        ClanResponse clan = battle.clan();
        ClanResponse opponent = battle.opponent();
        return wars.save(new War(clan.tag(), clan.name(), clan.attacks(), clan.stars(),
                clan.destructionPercentage(), clan.clanLevel(),
                opponent.tag(), opponent.name(), opponent.attacks(), opponent.stars(),
                opponent.destructionPercentage(), opponent.clanLevel(),
                state, endTime.minusSeconds(86400), endTime, battle.teamSize(), battle.attacksPerMember()));
    }

    private Scheduler scheduler() {
        var raids = new RaidSeasonService(null, null, null, null) {
            @Override
            public Optional<RaidSeason> syncRaidSeason() {
                return Optional.empty();
            }
        };
        return new Scheduler(service, raids, null);
    }

    private WarResponse withAdditionalHomeAttacks(List<AttackResponse> added, boolean addParticipant) {
        MemberResponse original = battle.clan().members().getFirst();
        List<AttackResponse> combined = new ArrayList<>(original.attacks());
        combined.addAll(added);
        List<MemberResponse> roster = new ArrayList<>(battle.clan().members());
        roster.set(0, new MemberResponse(original.tag(), original.name(), original.townhallLevel(),
                original.mapPosition(), combined, original.opponentAttacks(), original.bestOpponentAttack()));
        if (addParticipant) roster.add(new MemberResponse("#C", "Test Player C", 12, 3, null, 0, null));
        ClanResponse clan = battle.clan();
        ClanResponse updated = new ClanResponse(clan.tag(), "Updated clan name", clan.clanLevel(),
                clan.attacks() + added.size(), clan.stars() + added.stream().mapToInt(AttackResponse::stars).sum(),
                52.5, roster);
        return withStateAndClans(battle.state(), updated, battle.opponent());
    }

    private WarResponse withStateAndClans(String state, ClanResponse clan, ClanResponse opponent) {
        return new WarResponse(state, battle.teamSize(), battle.attacksPerMember(), battle.battleModifier(),
                battle.preparationStartTime(), battle.startTime(), battle.endTime(), clan, opponent);
    }

    private static ClanResponse withoutAttacks(ClanResponse clan) {
        List<MemberResponse> roster = clan.members().stream()
                .map(member -> new MemberResponse(member.tag(), member.name(), member.townhallLevel(),
                        member.mapPosition(), null, 0, null))
                .toList();
        return new ClanResponse(clan.tag(), clan.name(), clan.clanLevel(), 0, 0, 0.0, roster);
    }

    private Map<Integer, WarAttack> attacksByOrder() {
        return attacks.findAll().stream()
                .collect(Collectors.toMap(WarAttack::getAttackOrder, Function.identity()));
    }

    private void assertCounts(long warCount, long participantCount, long attackCount) {
        assertEquals(warCount, wars.count(), "wars");
        assertEquals(participantCount, participants.count(), "participants");
        assertEquals(attackCount, attacks.count(), "attacks");
    }

    private static void assertParticipant(WarParticipant participant, Long warId, String clanTag, int th, int position) {
        assertNotNull(participant);
        assertEquals(warId, participant.getWar().getId());
        assertEquals(clanTag, participant.getClanTag());
        assertEquals(th, participant.getTownHallLevel());
        assertEquals(position, participant.getMapPosition());
        assertNotNull(participant.getPlayerName());
    }

    static class MutableClock extends Clock {
        private final AtomicReference<Instant> time;
        private final ZoneId zone;

        MutableClock(Instant initialTime) {
            this(new AtomicReference<>(initialTime), ZoneOffset.UTC);
        }

        private MutableClock(AtomicReference<Instant> time, ZoneId zone) {
            this.time = time;
            this.zone = zone;
        }

        void setInstant(Instant instant) {
            time.set(instant);
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableClock(time, zone);
        }

        @Override
        public Instant instant() {
            return time.get();
        }
    }

    static class StubClashOfClansClient extends ClashOfClansClient {
        private WarResponse response;
        private RuntimeException failure;
        StubClashOfClansClient() { super(null); }
        @Override
        public WarResponse getCurrentWar() {
            if (failure != null) throw failure;
            return response;
        }
    }

    @Configuration
    @EnableTransactionManagement
    @EnableJpaRepositories(basePackageClasses = WarRepository.class)
    static class PersistenceConfiguration {
        @Bean
        DataSource dataSource() {
            return new DriverManagerDataSource(
                    "jdbc:h2:mem:war_service;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "");
        }
        @Bean
        LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
            var factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(dataSource);
            factory.setPackagesToScan(War.class.getPackageName());
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "create-drop",
                    "hibernate.jdbc.time_zone", "UTC"));
            return factory;
        }
        @Bean
        PlatformTransactionManager transactionManager(EntityManagerFactory factory) {
            return new JpaTransactionManager(factory);
        }
        @Bean
        StubClashOfClansClient client() { return new StubClashOfClansClient(); }
        @Bean
        MutableClock clock() { return new MutableClock(TEST_TIME); }
        @Bean
        WarService warService(StubClashOfClansClient client, WarRepository wars,
                              WarParticipantRepository participants, WarAttackRepository attacks,
                              Clock clock) {
            return new WarService(client, wars, participants, attacks, clock);
        }
    }
}
