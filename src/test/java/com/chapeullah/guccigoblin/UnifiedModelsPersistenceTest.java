package com.chapeullah.guccigoblin;

import com.chapeullah.guccigoblin.config.Scheduler;
import com.chapeullah.guccigoblin.member.dto.BuilderBaseLeagueResponse;
import com.chapeullah.guccigoblin.member.dto.LeagueTierResponse;
import com.chapeullah.guccigoblin.member.dto.MemberResponse;
import com.chapeullah.guccigoblin.member.dto.MembersResponse;
import com.chapeullah.guccigoblin.member.Member;
import com.chapeullah.guccigoblin.member.MemberDelta;
import com.chapeullah.guccigoblin.player.PlayerEvent;
import com.chapeullah.guccigoblin.player.PlayerEventType;
import com.chapeullah.guccigoblin.member.MemberRepository;
import com.chapeullah.guccigoblin.player.PlayerEventRepository;
import com.chapeullah.guccigoblin.member.service.MemberDeltaService;
import com.chapeullah.guccigoblin.member.service.MemberService;
import com.chapeullah.guccigoblin.member.service.MemberSyncService;
import com.chapeullah.guccigoblin.player.PlayerEventService;
import com.chapeullah.guccigoblin.raidseason.RaidSeasonService;
import com.chapeullah.guccigoblin.raidseason.model.RaidSeason;
import com.chapeullah.guccigoblin.war.WarService;
import com.chapeullah.guccigoblin.war.model.War;
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
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UnifiedModelsPersistenceTest {

    private static final BuilderBaseLeagueResponse BUILDER_BASE_LEAGUE =
            new BuilderBaseLeagueResponse(1, "Wood League V");
    private static final LeagueTierResponse LEAGUE_TIER =
            new LeagueTierResponse(2, "Gold League I");
    private static final int CLAN_RANK = 1;

    private static AnnotationConfigApplicationContext context;
    private MemberRepository members;
    private PlayerEventRepository players;
    private StubClient client;
    private MemberSyncService memberSyncService;
    private Scheduler scheduler;
    private StubWarService wars;

    @BeforeAll
    static void startPersistence() {
        context = new AnnotationConfigApplicationContext(PersistenceConfiguration.class);
    }

    @AfterAll
    static void closePersistence() {
        if (context != null) context.close();
    }

    @BeforeEach
    void resetData() {
        members = context.getBean(MemberRepository.class);
        players = context.getBean(PlayerEventRepository.class);
        client = context.getBean(StubClient.class);
        memberSyncService = context.getBean(MemberSyncService.class);
        scheduler = context.getBean(Scheduler.class);
        wars = context.getBean(StubWarService.class);
        players.deleteAllInBatch();
        members.deleteAllInBatch();
        client.response = new MembersResponse(List.of());
        wars.reset();
    }

    @Test
    void synchronizationPreservesCountersAndRecordsMembershipHistory() {
        synchronize(snapshot("#A", 100, 40), snapshot("#B", 20, 10));
        assertEquals(1, wars.getFinishCalls());
        Long memberId = member("#A").getId();
        Long returningMemberId = member("#B").getId();
        Instant memberJoined = member("#A").getJoinedAt();
        assertEventTypes("#A", PlayerEventType.JOINED);
        assertEventTypes("#B", PlayerEventType.JOINED);

        synchronize(new MemberResponse(
                "#A", "Renamed", "elder", 16, 201, 125, 47, 3100,
                BUILDER_BASE_LEAGUE, LEAGUE_TIER, CLAN_RANK));

        Member updated = member("#A");
        assertEquals(memberId, updated.getId());
        assertEquals("Renamed", updated.getName());
        assertEquals("elder", updated.getRole());
        assertEquals(16, updated.getTownHallLevel());
        assertEquals(201, updated.getExpLevel());
        assertEquals(3100, updated.getBuilderBaseTrophies());
        assertEquals(BUILDER_BASE_LEAGUE.id(), updated.getBuilderBaseLeagueId());
        assertEquals(BUILDER_BASE_LEAGUE.name(), updated.getBuilderBaseLeagueName());
        assertEquals(LEAGUE_TIER.id(), updated.getLeagueTierId());
        assertEquals(LEAGUE_TIER.name(), updated.getLeagueTierName());
        assertEquals(CLAN_RANK, updated.getClanRank());
        assertEquals(125, updated.getTotalDonations());
        assertEquals(47, updated.getTotalDonationsReceived());
        assertNotNull(updated.getLastDonation());
        assertEquals(updated.getLastActivity(), updated.getLastDonation());
        assertEquals(updated.getLastActivity(), updated.getLastDonationsReceived());
        assertEquals(updated.getLastActivity(), updated.getLastBuilderBaseTrophiesChanged());
        assertEquals(updated.getLastActivity(), updated.getLastTownHallUpgrade());
        assertEquals(memberJoined, updated.getJoinedAt());
        assertFalse(member("#B").isInClan());
        assertEquals(20, member("#B").getTotalDonations());
        assertEquals(10, member("#B").getTotalDonationsReceived());
        assertEquals(List.of("#A"), members.findAllByInClanTrue().stream().map(Member::getTag).toList());
        assertEventTypes("#A", PlayerEventType.JOINED);
        assertEventTypes("#B", PlayerEventType.JOINED, PlayerEventType.LEFT);

        PlayerEvent leftEvent = playerEvents("#B").get(1);
        assertNotNull(leftEvent.getDetectedAt());

        synchronize(snapshot("#A", 3, 2));
        assertEquals(128, member("#A").getTotalDonations());
        assertEquals(49, member("#A").getTotalDonationsReceived());
        assertEventTypes("#A", PlayerEventType.JOINED);
        assertEventTypes("#B", PlayerEventType.JOINED, PlayerEventType.LEFT);

        synchronize(snapshot("#A", 3, 2), snapshot("#B", 5, 1));
        assertEquals(2, members.count());
        assertEquals(4, players.count());
        assertEventTypes(
                "#B",
                PlayerEventType.JOINED,
                PlayerEventType.LEFT,
                PlayerEventType.JOINED);
        PlayerEvent rejoinedEvent = playerEvents("#B").get(2);
        assertFalse(rejoinedEvent.getDetectedAt().isBefore(leftEvent.getDetectedAt()));
        assertEquals(returningMemberId, member("#B").getId());
        assertTrue(member("#B").isInClan());
        assertEquals(20, member("#B").getTotalDonations());
        assertEquals(10, member("#B").getTotalDonationsReceived());
        assertEquals(5, member("#B").getDonations());
        assertEquals(1, member("#B").getDonationsReceived());
        assertEquals(2, members.findAllByInClanTrue().size());
    }

    @Test
    void unchangedSnapshotKeepsMemberDatesAndDoesNotCreateEvents() {
        synchronize(snapshot("#A", 100, 40));
        Member before = member("#A");

        synchronize(snapshot("#A", 100, 40));

        Member after = member("#A");
        assertTrue(after.isInClan());
        assertEquals(before.getId(), after.getId());
        assertFalse(MemberDelta.merge(before, after).hasChanges());
        assertEquals(before.getTotalDonations(), after.getTotalDonations());
        assertEquals(before.getTotalDonationsReceived(), after.getTotalDonationsReceived());
        assertEquals(before.getLastActivity(), after.getLastActivity());
        assertEquals(before.getJoinedAt(), after.getJoinedAt());
        assertNull(after.getLastDonation());
        assertNull(after.getLastDonationsReceived());
        assertNull(after.getLastBuilderBaseTrophiesChanged());
        assertNull(after.getLastTownHallUpgrade());
        assertEquals(1, players.count());
        assertEventTypes("#A", PlayerEventType.JOINED);
    }

    @Test
    void emptyClanDeactivatesMembersWithoutLosingStatisticsOrRepeatingLeftEvents() {
        synchronize(snapshot("#A", 100, 40));
        Member before = member("#A");
        synchronize();

        assertEquals(1, members.count());
        assertTrue(members.findAllByInClanTrue().isEmpty());
        Member left = member("#A");
        assertFalse(left.isInClan());
        assertEquals(before.getId(), left.getId());
        assertEquals(before.getJoinedAt(), left.getJoinedAt());
        assertEquals(before.getLastActivity(), left.getLastActivity());
        assertEquals(100, left.getTotalDonations());
        assertEquals(40, left.getTotalDonationsReceived());
        assertEquals(2, players.count());
        assertEventTypes("#A", PlayerEventType.JOINED, PlayerEventType.LEFT);
        Instant leftDetectedAt = playerEvents("#A").get(1).getDetectedAt();
        assertNotNull(leftDetectedAt);

        synchronize();

        assertFalse(member("#A").isInClan());
        assertEquals(1, members.count());
        assertEquals(2, players.count());
        assertEquals(leftDetectedAt, playerEvents("#A").get(1).getDetectedAt());
    }

    @Test
    void repositoriesAndEventColumnsUseTheNewIdentifiers() {
        synchronize(snapshot("#A", 100, 40));

        Member stored = member("#A");
        assertNotNull(stored.getId());
        assertTrue(members.findById(stored.getId()).isPresent());
        assertTrue(members.existsByTag("#A"));

        JdbcTemplate jdbc = new JdbcTemplate(context.getBean(DataSource.class));
        Map<String, Object> row = jdbc.queryForMap("""
                select m.id as member_id, m.tag, m.name, m.role, m.in_clan,
                       m.town_hall_level, m.exp_level, m.builder_base_trophies,
                       m.builder_base_league_id, m.builder_base_league_name,
                       m.league_tier_id, m.league_tier_name, m.clan_rank,
                       m.donations, m.donations_received, m.total_donations,
                       m.total_donations_received, m.last_activity, m.joined_at,
                       p.id as event_id, p.type, p.detected_at
                from members m join players p on p.tag = m.tag
                where m.tag = ?
                """, "#A");

        assertEquals(stored.getId().longValue(), ((Number) row.get("member_id")).longValue());
        assertEquals(Boolean.TRUE, row.get("in_clan"));
        assertEquals(100, ((Number) row.get("total_donations")).intValue());
        assertEquals(BUILDER_BASE_LEAGUE.name(), row.get("builder_base_league_name"));
        assertEquals(LEAGUE_TIER.name(), row.get("league_tier_name"));
        assertEquals(CLAN_RANK, ((Number) row.get("clan_rank")).intValue());
        assertEquals(PlayerEventType.JOINED.name(), row.get("type"));
        assertNotNull(row.get("detected_at"));
    }

    @Test
    void failedSynchronizationRollsBackMembersAndPlayerEvents() {
        synchronize(snapshot("#A", 100, 40), snapshot("#B", 20, 10));
        Long memberId = member("#A").getId();
        long eventCount = players.count();
        client.response = new MembersResponse(List.of(
                snapshot("#A", 150, 60),
                new MemberResponse(
                        "#C", "x".repeat(256), "member", 15, 200, 1, 1, 3000,
                        BUILDER_BASE_LEAGUE, LEAGUE_TIER, CLAN_RANK)
        ));

        assertThrows(RuntimeException.class, memberSyncService::sync);

        assertEquals(memberId, member("#A").getId());
        assertEquals(100, member("#A").getTotalDonations());
        assertEquals(40, member("#A").getTotalDonationsReceived());
        assertTrue(members.existsByTag("#B"));
        assertTrue(member("#B").isInClan());
        assertFalse(members.existsByTag("#C"));
        assertEquals(eventCount, players.count());
        assertEventTypes("#A", PlayerEventType.JOINED);
        assertEventTypes("#B", PlayerEventType.JOINED);
        assertTrue(playerEvents("#C").isEmpty());
    }

    @Test
    void schedulerContinuesWarSynchronizationAfterMemberFailure() {
        synchronize(snapshot("#A", 100, 40), snapshot("#B", 20, 10));
        long eventCount = players.count();
        int warSyncCalls = wars.getSyncCalls();
        int warFinishCalls = wars.getFinishCalls();
        client.response = new MembersResponse(List.of(
                snapshot("#A", 150, 60),
                new MemberResponse(
                        "#C", "x".repeat(256), "member", 15, 200, 1, 1, 3000,
                        BUILDER_BASE_LEAGUE, LEAGUE_TIER, CLAN_RANK)
        ));

        assertDoesNotThrow(scheduler::sync);

        assertEquals(warSyncCalls + 1, wars.getSyncCalls());
        assertEquals(warFinishCalls + 1, wars.getFinishCalls());
        assertEquals(100, member("#A").getTotalDonations());
        assertEquals(40, member("#A").getTotalDonationsReceived());
        assertTrue(members.existsByTag("#B"));
        assertTrue(member("#B").isInClan());
        assertFalse(members.existsByTag("#C"));
        assertEquals(eventCount, players.count());
        assertEventTypes("#A", PlayerEventType.JOINED);
        assertEventTypes("#B", PlayerEventType.JOINED);
        assertTrue(playerEvents("#C").isEmpty());
    }

    @Test
    void rejoiningCreatesAnotherJoinedEventWithTheCurrentName() {
        synchronize(snapshot("#A", 100, 40));
        synchronize();

        synchronize(new MemberResponse(
                "#A", "New name", "member", 15, 200, 5, 1, 3000,
                BUILDER_BASE_LEAGUE, LEAGUE_TIER, CLAN_RANK));

        List<PlayerEvent> events = playerEvents("#A");
        assertEquals(3, events.size());
        assertEquals(
                List.of(
                        PlayerEventType.JOINED,
                        PlayerEventType.LEFT,
                        PlayerEventType.JOINED),
                events.stream().map(PlayerEvent::getType).toList());
        assertEquals("New name", events.get(2).getName());
        assertEquals("New name", member("#A").getName());
        assertFalse(events.get(2).getDetectedAt().isBefore(events.get(1).getDetectedAt()));
    }

    @Test
    void warFailureDoesNotRollBackMembersAndPlayerEvents() {
        synchronize(snapshot("#A", 100, 40), snapshot("#B", 20, 10));
        long eventCount = players.count();
        int warSyncCalls = wars.getSyncCalls();
        int warFinishCalls = wars.getFinishCalls();
        client.response = new MembersResponse(List.of(
                new MemberResponse(
                        "#A", "Renamed", "elder", 16, 201, 150, 60, 3100,
                        BUILDER_BASE_LEAGUE, LEAGUE_TIER, CLAN_RANK),
                snapshot("#C", 5, 1)
        ));
        IllegalStateException failure = new IllegalStateException("War synchronization failed");
        wars.failWith(failure);

        assertDoesNotThrow(scheduler::sync);

        assertEquals(warSyncCalls + 1, wars.getSyncCalls());
        assertEquals(warFinishCalls + 1, wars.getFinishCalls());
        assertEquals("Renamed", member("#A").getName());
        assertEquals(150, member("#A").getTotalDonations());
        assertEquals(60, member("#A").getTotalDonationsReceived());
        assertFalse(member("#B").isInClan());
        assertTrue(members.existsByTag("#C"));
        assertEquals(eventCount + 2, players.count());
        assertEventTypes("#A", PlayerEventType.JOINED);
        assertEventTypes("#B", PlayerEventType.JOINED, PlayerEventType.LEFT);
        assertEventTypes("#C", PlayerEventType.JOINED);
    }

    @Test
    void warFinishingFailureDoesNotRollBackMembersOrPreventTheNextRun() {
        wars.failFinishingWith(new IllegalStateException("Simulated finishing failure"));

        assertDoesNotThrow(() -> synchronize(snapshot("#A", 100, 40)));

        assertEquals(1, wars.getSyncCalls());
        assertEquals(1, wars.getFinishCalls());
        assertEquals(100, member("#A").getTotalDonations());
        assertEventTypes("#A", PlayerEventType.JOINED);

        wars.failFinishingWith(null);
        assertDoesNotThrow(() -> synchronize(snapshot("#A", 150, 60)));

        assertEquals(2, wars.getSyncCalls());
        assertEquals(2, wars.getFinishCalls());
        assertEquals(150, member("#A").getTotalDonations());
        assertEventTypes("#A", PlayerEventType.JOINED);
    }

    @Test
    void donationResetToZeroPreservesActivityTotalsAndEventHistory() {
        synchronize(snapshot("#A", 100, 40));
        synchronize(snapshot("#A", 125, 47));
        Member before = member("#A");
        assertNotNull(before.getLastDonation());
        assertNotNull(before.getLastDonationsReceived());

        synchronize(snapshot("#A", 0, 0));

        Member after = member("#A");
        assertEquals(0, after.getDonations());
        assertEquals(0, after.getDonationsReceived());
        assertEquals(before.getTotalDonations(), after.getTotalDonations());
        assertEquals(before.getTotalDonationsReceived(), after.getTotalDonationsReceived());
        assertEquals(before.getLastActivity(), after.getLastActivity());
        assertEquals(before.getLastDonation(), after.getLastDonation());
        assertEquals(before.getLastDonationsReceived(), after.getLastDonationsReceived());
        assertEquals(1, players.count());
        assertEventTypes("#A", PlayerEventType.JOINED);
    }

    @Test
    void townHallUpgradeIsRecordedWithoutCountingAsActivity() {
        synchronize(snapshot("#A", 100, 40));
        Member before = member("#A");

        MemberResponse upgraded = new MemberResponse(
                "#A", "Player #A", "member", 16, 200, 100, 40, 3000,
                BUILDER_BASE_LEAGUE, LEAGUE_TIER, CLAN_RANK);
        synchronize(upgraded);

        Member after = member("#A");
        assertEquals(16, after.getTownHallLevel());
        assertNotNull(after.getLastTownHallUpgrade());
        assertEquals(before.getLastActivity(), after.getLastActivity());
        assertEquals(before.getJoinedAt(), after.getJoinedAt());
        assertEquals(before.getTotalDonations(), after.getTotalDonations());
        assertNull(after.getLastDonation());
        assertNull(after.getLastDonationsReceived());
        assertNull(after.getLastBuilderBaseTrophiesChanged());
        assertEventTypes("#A", PlayerEventType.JOINED);

        synchronize(upgraded);
        assertEquals(after.getLastTownHallUpgrade(), member("#A").getLastTownHallUpgrade());
        assertEquals(before.getLastActivity(), member("#A").getLastActivity());
    }

    @ParameterizedTest
    @CsvSource({"200, 90", "150, 60", "5, 1", "0, 0"})
    void rejoiningPreservesHistoryAndUsesReturnedCountersAsANewBaseline(int donations, int received) {
        synchronize(snapshot("#A", 100, 40));
        synchronize(new MemberResponse(
                "#A", "Player #A", "member", 16, 201, 150, 60, 3100,
                BUILDER_BASE_LEAGUE, LEAGUE_TIER, CLAN_RANK));
        Member before = member("#A");
        synchronize();
        synchronize();
        assertEventTypes("#A", PlayerEventType.JOINED, PlayerEventType.LEFT);

        MemberResponse returned = new MemberResponse(
                "#A", "Returned player", "elder", 17, 202, donations, received, 3200,
                BUILDER_BASE_LEAGUE, LEAGUE_TIER, CLAN_RANK);
        synchronize(returned);

        Member rejoined = member("#A");
        assertEquals(before.getId(), rejoined.getId());
        assertTrue(rejoined.isInClan());
        assertEquals("Returned player", rejoined.getName());
        assertEquals("elder", rejoined.getRole());
        assertEquals(17, rejoined.getTownHallLevel());
        assertEquals(202, rejoined.getExpLevel());
        assertEquals(3200, rejoined.getBuilderBaseTrophies());
        assertEquals(donations, rejoined.getDonations());
        assertEquals(received, rejoined.getDonationsReceived());
        assertEquals(150, rejoined.getTotalDonations());
        assertEquals(60, rejoined.getTotalDonationsReceived());
        assertEquals(before.getLastDonation(), rejoined.getLastDonation());
        assertEquals(before.getLastDonationsReceived(), rejoined.getLastDonationsReceived());
        assertEquals(before.getLastBuilderBaseTrophiesChanged(), rejoined.getLastBuilderBaseTrophiesChanged());
        assertEquals(before.getLastTownHallUpgrade(), rejoined.getLastTownHallUpgrade());
        assertFalse(rejoined.getJoinedAt().isBefore(before.getJoinedAt()));
        assertEquals(rejoined.getJoinedAt(), rejoined.getLastActivity());
        assertEventTypes("#A", PlayerEventType.JOINED, PlayerEventType.LEFT, PlayerEventType.JOINED);

        synchronize(returned);

        Member unchanged = member("#A");
        assertEquals(150, unchanged.getTotalDonations());
        assertEquals(60, unchanged.getTotalDonationsReceived());
        assertEquals(rejoined.getJoinedAt(), unchanged.getJoinedAt());
        assertEquals(rejoined.getLastActivity(), unchanged.getLastActivity());
        assertEquals(3, players.count());

        synchronize(new MemberResponse(
                returned.tag(), returned.name(), returned.role(), returned.townHallLevel(),
                returned.expLevel(), donations + 7, received + 3, returned.builderBaseTrophies(),
                returned.builderBaseLeague(), returned.leagueTier(), returned.clanRank()));

        Member updated = member("#A");
        assertEquals(before.getId(), updated.getId());
        assertEquals(157, updated.getTotalDonations());
        assertEquals(63, updated.getTotalDonationsReceived());
        assertEquals(rejoined.getJoinedAt(), updated.getJoinedAt());
        assertEquals(updated.getLastActivity(), updated.getLastDonation());
        assertEquals(updated.getLastActivity(), updated.getLastDonationsReceived());
        assertEquals(1, members.count());
        assertEquals(1, members.findAllByInClanTrue().size());
        assertEquals(3, players.count());
    }

    @Test
    void failedRejoiningRollsBackMembershipStatisticsAndEvents() {
        synchronize(snapshot("#A", 100, 40));
        synchronize();
        Member before = member("#A");
        client.response = new MembersResponse(List.of(
                snapshot("#A", 200, 90),
                new MemberResponse(
                        "#C", "x".repeat(256), "member", 15, 200, 1, 1, 3000,
                        BUILDER_BASE_LEAGUE, LEAGUE_TIER, CLAN_RANK)));

        assertThrows(RuntimeException.class, memberSyncService::sync);

        Member after = member("#A");
        assertFalse(after.isInClan());
        assertEquals(before.getId(), after.getId());
        assertEquals(before.getJoinedAt(), after.getJoinedAt());
        assertEquals(before.getLastActivity(), after.getLastActivity());
        assertEquals(100, after.getDonations());
        assertEquals(40, after.getDonationsReceived());
        assertEquals(100, after.getTotalDonations());
        assertEquals(40, after.getTotalDonationsReceived());
        assertFalse(members.existsByTag("#C"));
        assertTrue(members.findAllByInClanTrue().isEmpty());
        assertEquals(2, players.count());
        assertEventTypes("#A", PlayerEventType.JOINED, PlayerEventType.LEFT);

        synchronize(snapshot("#A", 200, 90));
        assertTrue(member("#A").isInClan());
        assertEquals(100, member("#A").getTotalDonations());
        assertEquals(40, member("#A").getTotalDonationsReceived());
        assertEventTypes("#A", PlayerEventType.JOINED, PlayerEventType.LEFT, PlayerEventType.JOINED);
    }

    @Test
    void addingMembershipFlagKeepsExistingMembersActiveAndPreservesTheirStatistics() {
        DataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:membership_upgrade;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "");
        Member existing = Member.initFrom(Member.from(snapshot("#LEGACY", 100, 40)));
        var originalFactory = schemaFactory(dataSource, "create");
        try (var entityManager = originalFactory.getObject().createEntityManager()) {
            entityManager.getTransaction().begin();
            entityManager.persist(existing);
            entityManager.getTransaction().commit();
        } finally {
            originalFactory.destroy();
        }

        // Recreate the previous schema, where every saved member was still in the clan.
        new JdbcTemplate(dataSource).execute("alter table members drop column in_clan");

        var updatedFactory = schemaFactory(dataSource, "update");
        try (var entityManager = updatedFactory.getObject().createEntityManager()) {
            Member restored = entityManager.find(Member.class, existing.getId());
            assertNotNull(restored);
            assertTrue(restored.isInClan());
            assertEquals("#LEGACY", restored.getTag());
            assertEquals(100, restored.getDonations());
            assertEquals(40, restored.getDonationsReceived());
            assertEquals(100, restored.getTotalDonations());
            assertEquals(40, restored.getTotalDonationsReceived());
        } finally {
            updatedFactory.destroy();
        }
    }

    private static LocalContainerEntityManagerFactoryBean schemaFactory(DataSource dataSource, String mode) {
        var factory = new PersistenceConfiguration().entityManagerFactory(dataSource);
        factory.setJpaPropertyMap(Map.of(
                "hibernate.hbm2ddl.auto", mode,
                "hibernate.hbm2ddl.halt_on_error", true,
                "hibernate.jdbc.time_zone", "UTC"));
        factory.afterPropertiesSet();
        return factory;
    }

    private Member member(String tag) {
        return members.findByTag(tag).orElseThrow();
    }

    private List<PlayerEvent> playerEvents(String tag) {
        return players.findAll().stream()
                .filter(player -> player.getTag().equals(tag))
                .sorted(Comparator.comparing(PlayerEvent::getId))
                .toList();
    }

    private void assertEventTypes(String tag, PlayerEventType... expected) {
        List<PlayerEvent> events = playerEvents(tag);
        assertEquals(
                List.of(expected),
                events.stream().map(PlayerEvent::getType).toList());
        assertTrue(events.stream().allMatch(event -> event.getDetectedAt() != null));
    }

    private void synchronize(MemberResponse... snapshots) {
        client.response = new MembersResponse(List.of(snapshots));
        scheduler.sync();
    }

    private static MemberResponse snapshot(String tag, int donations, int received) {
        return new MemberResponse(tag, "Player " + tag, "member", 15, 200,
                donations, received, 3000,
                BUILDER_BASE_LEAGUE, LEAGUE_TIER, CLAN_RANK);
    }

    static class StubClient extends Client {
        private MembersResponse response = new MembersResponse(List.of());

        StubClient() {
            super(null);
        }

        @Override
        public MembersResponse getMembers() {
            return response;
        }
    }

    static class StubWarService extends WarService {
        private RuntimeException failure;
        private RuntimeException finishingFailure;
        private int syncCalls;
        private int finishCalls;

        StubWarService() {
            super(null, null, null, null, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
        }

        public void failWith(RuntimeException failure) {
            this.failure = failure;
        }

        public void failFinishingWith(RuntimeException failure) {
            finishingFailure = failure;
        }

        public void reset() {
            failure = null;
            finishingFailure = null;
            syncCalls = 0;
            finishCalls = 0;
        }

        public int getSyncCalls() {
            return syncCalls;
        }

        public int getFinishCalls() {
            return finishCalls;
        }

        @Override
        public Optional<War> syncWar() {
            syncCalls++;
            if (failure != null) throw failure;
            return Optional.empty();
        }

        @Override
        public void finishEndedWars() {
            finishCalls++;
            if (finishingFailure != null) throw finishingFailure;
        }
    }

    @Configuration
    @EnableTransactionManagement
    @EnableJpaRepositories(basePackageClasses = {
            MemberRepository.class,
            PlayerEventRepository.class
    })
    static class PersistenceConfiguration {
        @Bean
        DataSource dataSource() {
            return new DriverManagerDataSource(
                    "jdbc:h2:mem:unified_models;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "");
        }

        @Bean
        LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
            var factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(dataSource);
            factory.setPackagesToScan(Member.class.getPackageName(), PlayerEvent.class.getPackageName());
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
        StubClient gucciClient() {
            return new StubClient();
        }

        @Bean
        MemberService memberService(StubClient client, MemberRepository members) {
            return new MemberService(client, members, new MemberDeltaService());
        }

        @Bean
        PlayerEventService playerEventService(PlayerEventRepository players) {
            return new PlayerEventService(players);
        }

        @Bean
        MemberSyncService memberSyncService(MemberService members, PlayerEventService players) {
            return new MemberSyncService(members, players);
        }

        @Bean
        StubWarService warService() {
            return new StubWarService();
        }

        @Bean
        Scheduler scheduler(MemberSyncService memberSyncService, StubWarService wars) {
            var raids = new RaidSeasonService(null, null, null, null) {
                @Override
                public Optional<RaidSeason> syncRaidSeason() {
                    return Optional.empty();
                }
            };
            return new Scheduler(memberSyncService, wars, raids);
        }
    }
}
