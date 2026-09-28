package com.chapeullah.guccigoblin.telegram;

import com.chapeullah.guccigoblin.member.Member;
import com.chapeullah.guccigoblin.member.MemberRepository;
import com.chapeullah.guccigoblin.member.dto.BuilderBaseLeagueResponse;
import com.chapeullah.guccigoblin.member.dto.LeagueTierResponse;
import com.chapeullah.guccigoblin.member.dto.MemberResponse;
import com.chapeullah.guccigoblin.raidseason.model.RaidSeason;
import com.chapeullah.guccigoblin.raidseason.model.RaidSeasonParticipant;
import com.chapeullah.guccigoblin.raidseason.repository.RaidSeasonParticipantRepository;
import com.chapeullah.guccigoblin.raidseason.repository.RaidSeasonRepository;
import com.chapeullah.guccigoblin.telegram.command.HelpCommand;
import com.chapeullah.guccigoblin.telegram.command.RaidCommand;
import com.chapeullah.guccigoblin.telegram.command.RaidMissedCommand;
import com.chapeullah.guccigoblin.telegram.command.RaidsCommand;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.MapPropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RaidCommandsTest {
    private static final String CLAN_TAG = "#HOME";
    private static final Instant RAID_START = Instant.parse("2026-09-25T07:00:00Z");

    private static AnnotationConfigApplicationContext context;
    private CommandDispatcher dispatcher;
    private MemberRepository members;
    private RaidSeasonRepository seasons;
    private RaidSeasonParticipantRepository participants;

    @BeforeAll
    static void startPersistence() {
        context = new AnnotationConfigApplicationContext();
        context.getEnvironment().getPropertySources().addFirst(
                new MapPropertySource("raid-commands-test", Map.of("coc.clanTag", CLAN_TAG)));
        context.register(PersistenceConfiguration.class);
        context.refresh();
    }

    @AfterAll
    static void closePersistence() {
        if (context != null) context.close();
    }

    @BeforeEach
    void resetData() {
        dispatcher = context.getBean(CommandDispatcher.class);
        members = context.getBean(MemberRepository.class);
        seasons = context.getBean(RaidSeasonRepository.class);
        participants = context.getBean(RaidSeasonParticipantRepository.class);
        participants.deleteAllInBatch();
        seasons.deleteAllInBatch();
        members.deleteAllInBatch();
    }

    @Test
    void commandsExplainMissingSeasonsAndAreListedInHelp() {
        for (String command : List.of("/raid", "/raidmissed", "/raids")) {
            assertEquals("Рейды ещё не сохранены.", execute(command));
        }
        String help = execute("/help");
        assertTrue(help.contains("/raid —"));
        assertTrue(help.contains("/raidmissed —"));
        assertTrue(help.contains("/raids —"));
    }

    @Test
    void currentRaidShowsOnlyCurrentMembersWithoutAnyAttacks() {
        RaidSeason old = season(CLAN_TAG, "ended", RAID_START.minus(7, ChronoUnit.DAYS), 1);
        RaidSeason current = season(CLAN_TAG, "ongoing", RAID_START, 1);
        season("#OTHER", "ongoing", RAID_START.plus(7, ChronoUnit.DAYS), 500);
        participant(old, "#ABSENT", "Absent", 1);
        participant(current, "#ATTACKED", "Previous name", 1);
        participant(current, "#ZERO", "Zero", 0);

        member("#ATTACKED", "Renamed attacker", 3, true);
        member("#ZERO", "<Zero & player>", 2, true);
        member("#ABSENT", "Absent", 1, true);
        member("#LEFT", "Left clan", 4, false);

        String summary = execute("/raid");
        assertTrue(summary.contains("<b>Текущий рейд</b>"));
        assertTrue(summary.contains("Статус: Идёт"));
        assertTrue(summary.contains("25.09.2026 10:00 — 28.09.2026 10:00 (МСК)"));
        assertTrue(summary.contains("Добыча: 1200 столичного золота"));
        assertTrue(summary.contains("Атаки: 1\n"));
        assertTrue(summary.contains("Участников с атаками: 1"));

        String missed = execute("/raidmissed");
        assertTrue(missed.contains("25.09.2026 10:00"));
        assertTrue(missed.contains("<code>#ABSENT</code>"));
        assertTrue(missed.contains("&lt;Zero &amp; player&gt;"));
        assertTrue(missed.contains("<code>#ZERO</code>"));
        assertFalse(missed.contains("#ATTACKED"));
        assertFalse(missed.contains("#LEFT"));
        assertFalse(missed.contains("18.09.2026"));
        assertTrue(missed.indexOf("#ABSENT") < missed.indexOf("#ZERO"));
        assertTrue(missed.contains("Всего без атак: 2"));
    }

    @Test
    void latestEndedRaidIsUsedBetweenRaidsEvenIfOlderSeasonStillSaysOngoing() {
        season(CLAN_TAG, "ongoing", RAID_START.minus(7, ChronoUnit.DAYS), 0);
        season(CLAN_TAG, "ended", RAID_START, 0);
        member("#CURRENT", "Current member", 1, true);

        String summary = execute("/raid");
        assertTrue(summary.contains("<b>Последний рейд</b>"));
        assertTrue(summary.contains("Статус: Завершён"));
        assertTrue(summary.contains("25.09.2026 10:00"));
        assertTrue(summary.contains("Участников с атаками: 0"));

        String missed = execute("/raidmissed");
        assertTrue(missed.contains("Статус: Завершён"));
        assertTrue(missed.contains("25.09.2026 10:00"));
        assertTrue(missed.contains("#CURRENT"));
        assertTrue(missed.contains("Всего без атак: 1"));
    }

    @Test
    void participantCountersAreEnoughEvenWithoutDetailedAttackRows() {
        RaidSeason current = season(CLAN_TAG, "ongoing", RAID_START, 2);
        member("#A", "Player A", 1, true);
        member("#B", "Player B", 2, true);
        participant(current, "#A", "Player A", 1);
        participant(current, "#B", "Player B", 1);

        String missed = execute("/raidmissed");
        assertTrue(missed.contains("Все нынешние участники клана сделали хотя бы одну атаку."));
        assertTrue(missed.contains("25.09.2026 10:00"));
        assertFalse(missed.contains("<code>"));
    }

    @Test
    void missingOrPartialParticipantsAreNotReportedAsMissedAttacks() {
        RaidSeason current = season(CLAN_TAG, "ongoing", RAID_START, 3);
        member("#A", "Player A", 1, true);
        member("#B", "Player B", 2, true);

        String missing = execute("/raidmissed");
        assertTrue(missing.contains("Данные об участниках рейда пока неполные."));
        assertFalse(missing.contains("#A"));
        assertFalse(missing.contains("#B"));
        assertTrue(execute("/raid").contains("Участников с атаками: данные ещё не загружены"));

        participant(current, "#A", "Player A", 1);
        assertTrue(execute("/raidmissed").contains("Данные об участниках рейда пока неполные."));

        participant(current, "#B", "Player B", 2);
        assertTrue(execute("/raidmissed")
                .contains("Все нынешние участники клана сделали хотя бы одну атаку."));
    }

    @Test
    void emptyCurrentRosterIsNotReportedAsEveryoneHavingAttacked() {
        season(CLAN_TAG, "ongoing", RAID_START, 0);
        member("#LEFT", "Left clan", 1, false);

        String result = execute("/raidmissed");
        assertTrue(result.contains("Текущие участники клана не найдены."));
        assertFalse(result.contains("Все нынешние участники"));
    }

    @Test
    void raidHistoryContainsFiveNewestSeasonsForConfiguredClan() {
        Instant first = Instant.parse("2026-08-07T07:00:00Z");
        for (int i = 0; i < 6; i++) {
            season(CLAN_TAG, i == 5 ? "ongoing" : "ended", first.plus(i * 7L, ChronoUnit.DAYS), i);
        }
        season("#OTHER", "ongoing", first.plus(42, ChronoUnit.DAYS), 999);

        String history = execute("/raids");
        int previousIndex = -1;
        for (String date : List.of("11.09.2026", "04.09.2026", "28.08.2026", "21.08.2026", "14.08.2026")) {
            int index = history.indexOf(date);
            assertTrue(index > previousIndex, "Expected next season in descending order: " + date);
            previousIndex = index;
        }
        assertFalse(history.contains("07.08.2026"));
        assertFalse(history.contains("18.09.2026"));
        assertTrue(history.contains("Идёт"));
        assertTrue(history.contains("Завершён"));
    }

    private String execute(String command) {
        return dispatcher.execute(command, "");
    }

    private RaidSeason season(String clanTag, String state, Instant start, int attacks) {
        return seasons.save(new RaidSeason(clanTag, state, start, start.plus(3, ChronoUnit.DAYS),
                1200, 2, attacks, 7, 0, 0));
    }

    private void participant(RaidSeason season, String tag, String name, int attacks) {
        participants.save(new RaidSeasonParticipant(season, tag, name, attacks, 5, 1, 600));
    }

    private void member(String tag, String name, int rank, boolean inClan) {
        var snapshot = new MemberResponse(tag, name, "member", 15, 200, 0, 0, 3000,
                new BuilderBaseLeagueResponse(1, "Builder League"),
                new LeagueTierResponse(2, "League"), rank);
        Member member = Member.initFrom(Member.from(snapshot));
        if (!inClan) member.leave();
        members.save(member);
    }

    @Configuration(proxyBeanMethods = false)
    @EnableTransactionManagement
    @EnableJpaRepositories(basePackageClasses = {MemberRepository.class, RaidSeasonRepository.class})
    @Import({CommandDispatcher.class, RaidCommand.class, RaidMissedCommand.class, RaidsCommand.class, HelpCommand.class})
    static class PersistenceConfiguration {
        @Bean
        DataSource dataSource() {
            return new DriverManagerDataSource(
                    "jdbc:h2:mem:raid_commands;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "");
        }

        @Bean
        LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
            var factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(dataSource);
            factory.setPackagesToScan(Member.class.getPackageName(), RaidSeason.class.getPackageName());
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "create-drop",
                    "hibernate.hbm2ddl.halt_on_error", true, "hibernate.jdbc.time_zone", "UTC"));
            return factory;
        }

        @Bean
        PlatformTransactionManager transactionManager(EntityManagerFactory factory) {
            return new JpaTransactionManager(factory);
        }

        @Bean
        CommandService commandService(MemberRepository members, RaidSeasonRepository seasons,
                                      RaidSeasonParticipantRepository participants) {
            return new CommandService(members, null, null, null, seasons, participants);
        }
    }
}
