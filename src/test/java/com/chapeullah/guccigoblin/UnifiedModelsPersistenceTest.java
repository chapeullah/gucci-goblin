package com.chapeullah.guccigoblin;

import com.chapeullah.guccigoblin.client.Client;
import com.chapeullah.guccigoblin.config.Scheduler;
import com.chapeullah.guccigoblin.member.dto.MemberResponse;
import com.chapeullah.guccigoblin.member.dto.MembersResponse;
import com.chapeullah.guccigoblin.member.Member;
import com.chapeullah.guccigoblin.member.MemberDelta;
import com.chapeullah.guccigoblin.player.Player;
import com.chapeullah.guccigoblin.member.MemberRepository;
import com.chapeullah.guccigoblin.player.PlayerRepository;
import com.chapeullah.guccigoblin.member.MemberDeltaService;
import com.chapeullah.guccigoblin.member.MemberService;
import com.chapeullah.guccigoblin.player.PlayerService;
import com.chapeullah.guccigoblin.war.WarService;
import com.chapeullah.guccigoblin.war.model.War;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UnifiedModelsPersistenceTest {

    private static AnnotationConfigApplicationContext context;
    private MemberRepository members;
    private PlayerRepository players;
    private StubClient client;
    private Scheduler scheduler;
    private StubWarService wars;
    private TransactionTemplate transaction;

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
        players = context.getBean(PlayerRepository.class);
        client = context.getBean(StubClient.class);
        scheduler = context.getBean(Scheduler.class);
        wars = context.getBean(StubWarService.class);
        transaction = new TransactionTemplate(context.getBean(PlatformTransactionManager.class));
        members.deleteAllInBatch();
        players.deleteAllInBatch();
        client.response = new MembersResponse(List.of());
        wars.failWith(null);
    }

    @Test
    void synchronizationPreservesCountersAndPlayerHistoryAcrossLeavingAndRejoining() {
        synchronize(snapshot("#A", 100, 40), snapshot("#B", 20, 10));
        Instant memberJoined = member("#A").getJoined();
        Instant playerJoined = player("#A").getJoinedAt();

        synchronize(new MemberResponse(
                "#A", "Renamed", "elder", 16, 201, 125, 47, 3100));
        Member updated = member("#A");
        assertEquals("Renamed", updated.getName());
        assertEquals("Renamed", player("#A").getName());
        assertEquals("#A", player("#A").getTag());
        assertEquals("elder", updated.getRole());
        assertEquals(16, updated.getTownHallLevel());
        assertEquals(201, updated.getExpLevel());
        assertEquals(3100, updated.getBuilderBaseTrophies());
        assertEquals(125, updated.getTotalDonations());
        assertEquals(47, updated.getTotalDonationsReceived());
        assertNotNull(updated.getLastDonation());
        assertEquals(updated.getLastActivity(), updated.getLastDonation());
        assertEquals(updated.getLastActivity(), updated.getLastDonationsReceived());
        assertEquals(updated.getLastActivity(), updated.getLastBuilderBaseTrophiesChanged());
        assertEquals(updated.getLastActivity(), updated.getLastTownHallUpgrade());
        assertEquals(memberJoined, updated.getJoined());
        assertEquals(playerJoined, player("#A").getJoinedAt());
        assertNull(player("#A").getLeftAt());
        assertFalse(members.existsById("#B"));
        Instant leftAt = player("#B").getLeftAt();
        assertNotNull(leftAt);

        // A season reset adds the new counters to the previously accumulated totals.
        synchronize(snapshot("#A", 3, 2));
        assertEquals(128, member("#A").getTotalDonations());
        assertEquals(49, member("#A").getTotalDonationsReceived());
        assertEquals(leftAt, player("#B").getLeftAt());

        synchronize(snapshot("#A", 3, 2), snapshot("#B", 5, 1));
        assertEquals(2, players.count());
        assertNull(player("#B").getLeftAt());
        assertFalse(player("#B").getJoinedAt().isBefore(leftAt));
        assertEquals(5, member("#B").getTotalDonations());
        assertEquals(1, member("#B").getTotalDonationsReceived());
    }

    @Test
    void unchangedSnapshotKeepsActivityAndJoiningDates() {
        synchronize(snapshot("#A", 100, 40));
        Member before = member("#A");
        Instant joinedAt = player("#A").getJoinedAt();

        synchronize(snapshot("#A", 100, 40));
        Member after = member("#A");
        assertFalse(MemberDelta.merge(before, after).hasChanges());
        assertEquals(before.getTotalDonations(), after.getTotalDonations());
        assertEquals(before.getTotalDonationsReceived(), after.getTotalDonationsReceived());
        assertEquals(before.getLastActivity(), after.getLastActivity());
        assertEquals(before.getJoined(), after.getJoined());
        assertNull(after.getLastDonation());
        assertNull(after.getLastDonationsReceived());
        assertNull(after.getLastBuilderBaseTrophiesChanged());
        assertNull(after.getLastTownHallUpgrade());
        assertEquals(joinedAt, player("#A").getJoinedAt());
    }

    @Test
    void emptyClanRemovesCurrentMembersButKeepsPlayerHistory() {
        synchronize(snapshot("#A", 100, 40));
        synchronize();
        assertEquals(0, members.count());
        assertEquals(1, players.count());
        Instant leftAt = player("#A").getLeftAt();
        assertNotNull(leftAt);

        synchronize();
        assertEquals(leftAt, player("#A").getLeftAt());
    }

    @Test
    void repositoryQueriesAndExistingColumnNamesStillWork() {
        synchronize(snapshot("#A", 100, 40));
        assertEquals("Player #A", members.findNameByTag("#A"));
        Instant activity = Instant.parse("2026-01-02T03:04:05Z");
        transaction.executeWithoutResult(status ->
                assertEquals(1, members.updateLastActivity("#A", activity)));
        assertEquals(activity, members.findLastActivity("#A"));
        assertEquals(activity, member("#A").getLastActivity());

        JdbcTemplate jdbc = new JdbcTemplate(context.getBean(DataSource.class));
        Map<String, Object> row = jdbc.queryForMap("""
                select m.tag, m.name, m.role, m.town_hall_level, m.exp_level,
                       m.builder_base_trophies, m.donations, m.donations_received,
                       m.total_donations, m.total_donations_received, m.last_activity,
                       m.last_donation, m.last_donations_received,
                       m.last_builder_base_trophies_changed, m.last_town_hall_upgrade,
                       m.joined, p.joined_at, p.left_at
                from members m join players p on p.tag = m.tag where m.tag = ?
                """, "#A");
        assertEquals(100, ((Number) row.get("total_donations")).intValue());
        assertNotNull(row.get("joined_at"));
        assertNull(row.get("left_at"));
    }

    @Test
    void equalityByTagWorksWithJpaReferences() {
        synchronize(snapshot("#A", 100, 40));
        Member detachedMember = member("#A");
        Player detachedPlayer = player("#A");
        transaction.executeWithoutResult(status -> {
            Member reference = members.getReferenceById("#A");
            assertEquals(detachedMember, reference);
            assertEquals(reference, detachedMember);
            assertEquals(detachedMember.hashCode(), reference.hashCode());
            Player playerReference = players.getReferenceById("#A");
            assertEquals(detachedPlayer, playerReference);
            assertEquals(playerReference, detachedPlayer);
            assertEquals(detachedPlayer.hashCode(), playerReference.hashCode());
        });
    }

    @Test
    void failedSynchronizationRollsBackChangesToMembersAndPlayers() {
        synchronize(snapshot("#A", 100, 40), snapshot("#B", 20, 10));
        Instant joinedAt = player("#B").getJoinedAt();
        client.response = new MembersResponse(List.of(
                snapshot("#A", 150, 60),
                new MemberResponse(
                        "#C", "x".repeat(256), "member", 15, 200, 1, 1, 3000)
        ));

        assertThrows(RuntimeException.class, scheduler::sync);
        assertEquals(100, member("#A").getTotalDonations());
        assertEquals(40, member("#A").getTotalDonationsReceived());
        assertTrue(members.existsById("#B"));
        assertFalse(members.existsById("#C"));
        assertEquals(joinedAt, player("#B").getJoinedAt());
        assertNull(player("#B").getLeftAt());
        assertFalse(players.existsById("#C"));
    }

    @Test
    void rejoiningPlayerUpdatesNameWithoutCreatingAnotherPlayer() {
        synchronize(snapshot("#A", 100, 40));
        synchronize();
        Instant leftAt = player("#A").getLeftAt();

        synchronize(new MemberResponse(
                "#A", "New name", "member", 15, 200, 5, 1, 3000));

        Player rejoined = player("#A");
        assertEquals(1, players.count());
        assertEquals("#A", rejoined.getTag());
        assertEquals("New name", rejoined.getName());
        assertEquals(member("#A").getName(), rejoined.getName());
        assertNull(rejoined.getLeftAt());
        assertFalse(rejoined.getJoinedAt().isBefore(leftAt));
    }

    @Test
    void warFailureRollsBackMemberAndPlayerChanges() {
        synchronize(snapshot("#A", 100, 40), snapshot("#B", 20, 10));
        Instant joinedAt = player("#A").getJoinedAt();
        client.response = new MembersResponse(List.of(
                new MemberResponse(
                        "#A", "Renamed", "elder", 16, 201, 150, 60, 3100),
                snapshot("#C", 5, 1)
        ));
        IllegalStateException failure = new IllegalStateException("War synchronization failed");
        wars.failWith(failure);

        assertSame(failure, assertThrows(IllegalStateException.class, scheduler::sync));
        assertEquals("Player #A", member("#A").getName());
        assertEquals(100, member("#A").getTotalDonations());
        assertEquals("Player #A", player("#A").getName());
        assertEquals(joinedAt, player("#A").getJoinedAt());
        assertTrue(members.existsById("#B"));
        assertNull(player("#B").getLeftAt());
        assertFalse(members.existsById("#C"));
        assertFalse(players.existsById("#C"));
    }

    @Test
    void donationResetToZeroPreservesActivityAndAccumulatedTotals() {
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
    }

    private Member member(String tag) {
        return members.findById(tag).orElseThrow();
    }

    private Player player(String tag) {
        return players.findById(tag).orElseThrow();
    }

    private void synchronize(MemberResponse... snapshots) {
        client.response = new MembersResponse(List.of(snapshots));
        scheduler.sync();
    }

    private static MemberResponse snapshot(String tag, int donations, int received) {
        return new MemberResponse(tag, "Player " + tag, "member", 15, 200,
                donations, received, 3000);
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

        StubWarService() {
            super(null, null, null, null);
        }

        public void failWith(RuntimeException failure) {
            this.failure = failure;
        }

        @Override
        public Optional<War> syncWar() {
            if (failure != null) throw failure;
            return Optional.empty();
        }
    }

    @Configuration
    @EnableTransactionManagement
    @EnableJpaRepositories(basePackageClasses = {
            MemberRepository.class,
            PlayerRepository.class
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
            factory.setPackagesToScan(Member.class.getPackageName(), Player.class.getPackageName());
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
        PlayerService playerService(PlayerRepository players, MemberRepository members) {
            return new PlayerService(players, members);
        }

        @Bean
        StubWarService warService() {
            return new StubWarService();
        }

        @Bean
        Scheduler scheduler(MemberService members, PlayerService players, StubWarService wars) {
            return new Scheduler(members, players, wars);
        }
    }
}
