package com.chapeullah.guccigoblin.telegram;

import com.chapeullah.guccigoblin.clan.model.Clan;
import com.chapeullah.guccigoblin.leaguetier.LeagueTier;
import com.chapeullah.guccigoblin.member.MemberRepository;
import com.chapeullah.guccigoblin.member.MemberService;
import com.chapeullah.guccigoblin.member.model.Member;
import com.chapeullah.guccigoblin.raidseason.model.RaidSeason;
import com.chapeullah.guccigoblin.raidseason.model.RaidSeasonClanMemberSnapshot;
import com.chapeullah.guccigoblin.raidseason.model.RaidSeasonParticipant;
import com.chapeullah.guccigoblin.raidseason.repository.RaidSeasonParticipantRepository;
import com.chapeullah.guccigoblin.raidseason.repository.RaidSeasonRepository;
import com.chapeullah.guccigoblin.raidseason.repository.RaidSeasonClanMemberSnapshotRepository;
import com.chapeullah.guccigoblin.raidseason.service.RaidSeasonService;
import com.chapeullah.guccigoblin.raidseason.service.RaidSeasonParticipantService;
import com.chapeullah.guccigoblin.raidseason.service.RaidSeasonClanMemberSnapshotService;
import com.chapeullah.guccigoblin.telegram.command.HelpCommand;
import com.chapeullah.guccigoblin.telegram.command.MemberCommand;
import com.chapeullah.guccigoblin.telegram.command.RaidCommand;
import com.chapeullah.guccigoblin.telegram.command.RaidMissedCommand;
import com.chapeullah.guccigoblin.telegram.command.RaidsCommand;
import com.chapeullah.guccigoblin.telegram.command.WarCommand;
import com.chapeullah.guccigoblin.telegram.command.WarsCommand;
import com.chapeullah.guccigoblin.war.model.War;
import com.chapeullah.guccigoblin.war.repository.WarRepository;
import com.chapeullah.guccigoblin.war.repository.WarParticipantRepository;
import com.chapeullah.guccigoblin.war.repository.WarAttackRepository;
import com.chapeullah.guccigoblin.war.model.WarParticipant;
import com.chapeullah.guccigoblin.war.model.WarAttack;
import com.chapeullah.guccigoblin.war.service.WarService;
import com.chapeullah.guccigoblin.war.service.WarParticipantService;
import com.chapeullah.guccigoblin.war.service.WarAttackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TelegramCommandServiceTest {

    private static final String CLAN_TAG = "#TEST";
    private static final Instant START = Instant.parse("2026-10-16T08:00:00Z");

    private War currentWar;
    private List<War> warHistory = List.of();
    private RaidSeason latestSeason;
    private List<RaidSeason> raidHistory = List.of();
    private List<RaidSeasonParticipant> participants = List.of();
    private List<Member> currentMembers = List.of();
    private List<Member> storedMembers = List.of();
    private final List<String> requestedMemberTags = new ArrayList<>();
    private List<RaidSeasonClanMemberSnapshot> memberSnapshots = List.of();
    private List<WarParticipant> warParticipants = List.of();
    private List<WarAttack> warAttacks = List.of();
    private int currentRosterReads;
    private TelegramCommandService service;
    private TelegramCommandDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        WarRepository wars = repository(WarRepository.class, (proxy, method, args) -> {
            assertEquals(CLAN_TAG, args[0]);
            return switch (method.getName()) {
                case "findFirstByClanTagAndStateInOrderByStartTimeDesc" -> {
                    assertEquals(List.of("preparation", "inWar"), args[1]);
                    yield Optional.ofNullable(currentWar);
                }
                case "findTop5ByClanTagAndStateOrderByEndTimeDesc" -> {
                    assertEquals("warEnded", args[1]);
                    yield warHistory;
                }
                default -> throw new UnsupportedOperationException(method.getName());
            };
        });
        RaidSeasonRepository raids = repository(RaidSeasonRepository.class, (proxy, method, args) -> {
            assertEquals(CLAN_TAG, args[0]);
            return switch (method.getName()) {
                case "findFirstByClanTagOrderByStartTimeDesc" -> Optional.ofNullable(latestSeason);
                case "findTop5ByClanTagOrderByStartTimeDesc" -> raidHistory;
                default -> throw new UnsupportedOperationException(method.getName());
            };
        });
        RaidSeasonParticipantRepository raidParticipants = repository(
                RaidSeasonParticipantRepository.class, (proxy, method, args) -> {
                    assertEquals("findAllByRaidSeason", method.getName());
                    assertEquals(latestSeason, args[0]);
                    return participants;
                });

        MemberRepository members = repository(MemberRepository.class, (proxy, method, args) -> {
            return switch (method.getName()) {
                case "findAllByClanTagAndInClanTrueOrderByClanRankAsc" -> {
                    assertEquals(CLAN_TAG, args[0]);
                    currentRosterReads++;
                    yield currentMembers;
                }
                case "findByTagAndClanTag" -> {
                    assertEquals(CLAN_TAG, args[1]);
                    requestedMemberTags.add((String) args[0]);
                    yield storedMembers.stream()
                            .filter(member -> member.getTag().equals(args[0]))
                            .findFirst();
                }
                default -> throw new UnsupportedOperationException(method.getName());
            };
        });
        MemberService memberService = new MemberService(members, null, null, null, null);
        RaidSeasonClanMemberSnapshotRepository snapshots = repository(
                RaidSeasonClanMemberSnapshotRepository.class, (proxy, method, args) -> {
                    assertEquals("findAllByRaidSeason", method.getName());
                    assertEquals(latestSeason, args[0]);
                    return memberSnapshots;
                });
        WarParticipantRepository warParticipantRepository = repository(
                WarParticipantRepository.class, (proxy, method, args) -> {
                    assertEquals("findByWar", method.getName());
                    assertEquals(currentWar, args[0]);
                    return warParticipants;
                });
        WarAttackRepository warAttackRepository = repository(
                WarAttackRepository.class, (proxy, method, args) -> {
                    assertEquals("findAllByWar", method.getName());
                    assertEquals(currentWar, args[0]);
                    return warAttacks;
                });

        RaidSeasonParticipantService raidParticipantService =
                new RaidSeasonParticipantService(raidParticipants);
        RaidSeasonClanMemberSnapshotService snapshotService =
                new RaidSeasonClanMemberSnapshotService(null, snapshots);
        RaidSeasonService raidService = new RaidSeasonService(
                null, raids, null, raidParticipantService, snapshotService, null);
        WarParticipantService warParticipantService = new WarParticipantService(warParticipantRepository);
        WarAttackService warAttackService = new WarAttackService(warAttackRepository, warParticipantService);
        WarService warService = new WarService(
                null, wars, null, warParticipantService, warAttackService, null);

        service = new TelegramCommandService(
                warService, warParticipantService, warAttackService,
                raidService, raidParticipantService, snapshotService, memberService, null);
        ReflectionTestUtils.setField(service, "clanTag", CLAN_TAG);
        dispatcher = new TelegramCommandDispatcher(List.of(
                new WarCommand(service),
                new WarsCommand(service),
                new RaidCommand(service),
                new RaidsCommand(service),
                new RaidMissedCommand(service),
                new MemberCommand(service),
                new HelpCommand(service)));
    }

    @Test
    void commandsRouteAndExplainMissingData() {
        assertEquals("Сейчас активной войны нет", execute("/war"));
        assertEquals("Завершённые войны не найдены", execute("/wars"));
        assertEquals("Сезоны рейдов ещё не сохранены", execute("/raid"));
        assertEquals("Сезоны рейдов ещё не сохранены", execute("/raids"));
        assertEquals("Сезоны рейдов ещё не сохранены", execute("/raidmissed"));
        assertEquals("Команда не найдена", execute("/getCurrentWarMessage"));
    }

    @Test
    void currentWarShowsBothSidesAndEscapesClanNames() {
        currentWar = war("inWar", 10, 8, 75.5, 60.0, false);

        String text = execute("/war");

        assertTrue(text.contains("<b>Текущая война</b>"));
        assertTrue(text.contains("Наш &lt;клан&gt;"));
        assertTrue(text.contains("Соперник &amp; Co"));
        assertTrue(text.contains("Статус: Идёт"));
        assertTrue(text.contains("Звёзды: 10 — 8"));
        assertTrue(text.contains("Наши атаки: 7 из 20"));
        assertTrue(text.contains("Атаки соперника: 6 из 20"));
        assertTrue(text.contains("Окончание: 17.10.2026 11:00 (МСК)"));
    }

    @Test
    void preparationShowsStartTimeInsteadOfEndTime() {
        currentWar = war("preparation", 0, 0, 0.0, 0.0, false);

        String text = execute("/war");

        assertTrue(text.contains("Статус: Подготовка"));
        assertTrue(text.contains("Начало: 16.10.2026 11:00 (МСК)"));
        assertFalse(text.contains("Окончание:"));
    }

    @Test
    void warHistoryUsesStarsBeforeDestructionAndMarksUnconfirmedResults() {
        warHistory = List.of(
                war("warEnded", 10, 9, 50.0, 99.0, true),
                war("warEnded", 10, 10, 95.0, 90.0, true),
                war("warEnded", 10, 10, 95.0, 95.0, true),
                war("warEnded", 9, 10, 99.0, 50.0, true),
                war("warEnded", 10, 9, 95.0, 90.0, false));

        String text = execute("/wars");

        assertTrue(text.startsWith("<b>История войн</b>"));
        assertEquals(2, text.split("Результат: Победа", -1).length - 1);
        assertTrue(text.contains("Результат: Поражение"));
        assertTrue(text.contains("Результат: Ничья"));
        assertTrue(text.contains("Результат: Уточняется"));
        assertTrue(text.contains("Наш &lt;клан&gt;"));
    }

    @Test
    void ongoingRaidCountsOnlyParticipantsWithAttacks() {
        latestSeason = season("ongoing", 7);
        participants = participants(3, 4, 0);

        String text = execute("/raid");

        assertTrue(text.startsWith("<b>Текущий сезон рейдов</b>"));
        assertTrue(text.contains("Статус: Идёт"));
        assertTrue(text.contains("Участников с атаками: 2"));
        assertTrue(text.contains("Столичное золото: 12000"));
    }

    @Test
    void endedLatestRaidIsClearlyLabelledAsLastSeason() {
        latestSeason = season("ended", 0);

        String text = execute("/raid");

        assertTrue(text.startsWith("<b>Последний сезон рейдов</b>"));
        assertTrue(text.contains("Статус: Завершён"));
    }

    @Test
    void incompleteOrInvalidParticipantCountsAreNotReportedAsComplete() {
        latestSeason = season("ongoing", 7);
        for (Integer[] counts : new Integer[][]{
                {3, 3}, {null, 7}, {-1, 8}, {}}) {
            participants = participants(counts);
            assertTrue(execute("/raid").contains("Участников с атаками: Уточняется"));
        }

        latestSeason.setTotalAttacks(null);
        participants = participants(7);
        assertTrue(execute("/raid").contains("Участников с атаками: Уточняется"));

        latestSeason.setTotalAttacks(-1);
        assertTrue(execute("/raid").contains("Участников с атаками: Уточняется"));
    }

    @Test
    void zeroAttacksWithNoParticipantsIsAValidZero() {
        latestSeason = season("ongoing", 0);

        assertTrue(execute("/raid").contains("Участников с атаками: 0"));
    }

    @Test
    void nullableRaidFieldsUseNoDataText() {
        latestSeason = season(null, null);
        latestSeason.setCapitalTotalLoot(null);
        latestSeason.setRaidsCompleted(null);
        latestSeason.setEnemyDistrictsDestroyed(null);

        String text = execute("/raid");

        assertTrue(text.contains("Статус: Нет данных"));
        assertTrue(text.contains("Столичное золото: Нет данных"));
        assertTrue(text.contains("Атаки: Нет данных"));
        assertFalse(text.contains("null"));
    }

    @Test
    void raidHistoryPreservesNewestFirstOrderAndEscapesUnknownState() {
        RaidSeason newest = season("ongoing", 7);
        RaidSeason older = season("<unknown>", null);
        older.setStartTime(START.minusSeconds(7 * 24 * 60 * 60));
        older.setEndTime(START.minusSeconds(4 * 24 * 60 * 60));
        older.setCapitalTotalLoot(null);
        raidHistory = List.of(newest, older);

        String text = execute("/raids");

        assertTrue(text.startsWith("<b>История рейдов</b>"));
        assertTrue(text.indexOf("16.10.2026") < text.indexOf("09.10.2026"));
        assertTrue(text.contains("Статус: &lt;unknown&gt;"));
        assertTrue(text.contains("Столичное золото: Нет данных"));
        assertFalse(text.contains("null"));
    }

    @Test
    void raidMissedUsesSnapshotNamesAndIncludesDepartedMembersButNotLaterJoiners() {
        latestSeason = season("ongoing", 6);
        currentMembers = List.of(
                member("#NEW", "Новичок", 1),
                member("#P0", "Новое имя", 2));
        memberSnapshots = List.of(
                snapshot("#P2", "Ушедший ⚡<ник>"),
                snapshot("#P0", "Имя & без атаки"),
                snapshot("#P1", "Сделал атаки"));
        participants = List.of(
                new RaidSeasonParticipant(latestSeason, "#P0", "Другое имя", 0, 5, 1, 0),
                new RaidSeasonParticipant(latestSeason, "#P1", "Другое имя", 5, 5, 1, 1000),
                new RaidSeasonParticipant(latestSeason, "#OUTSIDE", "Вне снимка", 1, 5, 1, 100));

        String text = execute("/raidmissed");

        assertTrue(text.startsWith("<b>Участники без атак в рейдах</b>"));
        assertTrue(text.contains("Статус: Идёт"));
        assertTrue(text.contains("Состав клана на момент сохранения снимка"));
        assertTrue(text.contains("Ушедший □&lt;ник&gt;"));
        assertTrue(text.contains("Имя &amp; без атаки"));
        assertTrue(text.indexOf("#P2") < text.indexOf("#P0"));
        assertFalse(text.contains("#P1"));
        assertFalse(text.contains("#OUTSIDE"));
        assertFalse(text.contains("#NEW"));
        assertFalse(text.contains("Новое имя"));
        assertEquals(0, currentRosterReads);
    }

    @Test
    void raidMissedDoesNotAccuseMembersWhenParticipantDataIsIncomplete() {
        latestSeason = season("ended", 7);
        participants = participants(3, 3);
        memberSnapshots = List.of(snapshot("#P2", "Без атаки"));

        String text = execute("/raidmissed");

        assertEquals("Данные об участниках рейда пока неполные", text);
        assertFalse(text.contains("#P2"));
    }

    @Test
    void raidMissedExplainsWhenEveryoneInSnapshotHasAttacked() {
        latestSeason = season("ongoing", 3);
        participants = participants(1, 2);
        memberSnapshots = List.of(
                snapshot("#P0", "Первый"),
                snapshot("#P1", "Второй"));

        String text = execute("/raidmissed");

        assertTrue(text.contains("У всех участников из снимка есть атаки в этом сезоне"));
        assertFalse(text.contains("<code>"));
    }

    @Test
    void raidMissedListsSnapshotMembersWhenTheSeasonHasZeroAttacks() {
        latestSeason = season("ended", 0);
        memberSnapshots = List.of(
                snapshot("#P0", "Первый"),
                snapshot("#P1", "Второй"));

        String text = execute("/raidmissed");

        assertTrue(text.contains("Статус: Завершён"));
        assertTrue(text.contains("01. Первый | <code>#P0</code>"));
        assertTrue(text.contains("02. Второй | <code>#P1</code>"));
        assertEquals(0, currentRosterReads);
    }

    @Test
    void raidMissedDoesNotReplaceMissingSnapshotWithCurrentRoster() {
        latestSeason = season("ongoing", 0);
        currentMembers = List.of(member("#NEW", "Новичок", 1));

        assertEquals("Снимок состава клана для этого сезона не сохранён", execute("/raidmissed"));
        assertEquals(0, currentRosterReads);
    }

    @Test
    void attacksUsesServicesAndFiltersOurClanInMapOrder() {
        currentWar = war("inWar", 3, 1, 100.0, 50.0, false);
        WarParticipant first = new WarParticipant(currentWar, "#P1", "Первый <ник>", CLAN_TAG, 10, 1);
        WarParticipant second = new WarParticipant(currentWar, "#P2", "Второй", CLAN_TAG, 10, 2);
        WarParticipant enemy = new WarParticipant(currentWar, "#ENEMY1", "Соперник", "#ENEMY", 10, 1);
        warParticipants = List.of(second, enemy, first);
        warAttacks = List.of(
                new WarAttack(currentWar, second, enemy, 10, 10, 3, 100.0, 1, 60),
                new WarAttack(currentWar, enemy, first, 10, 10, 1, 50.0, 2, 60));

        String text = service.attacks();

        assertTrue(text.contains("1. Первый &lt;ник&gt; — 2 из 2"));
        assertTrue(text.contains("2. Второй — 1 из 2"));
        assertTrue(text.indexOf("Первый") < text.indexOf("Второй"));
        assertFalse(text.contains("Соперник"));
    }

    @Test
    void attacksHandlesMissingWarAndPreparationThroughWarService() {
        assertEquals("Сейчас активной войны нет.", service.attacks());
        currentWar = war("preparation", 0, 0, 0.0, 0.0, false);
        assertEquals("Война ещё не началась.", service.attacks());
    }

    @Test
    void helpIncludesRaidMissed() {
        assertTrue(execute("/help").contains(
                "/raidmissed — Участники без атак в рейдах"));
    }

    @Test
    void memberExplainsMissingArgumentsWithoutQueryingTheRepository() {
        for (String args : new String[]{null, "", "   ", "\t\n"}) {
            assertEquals("Используй: /member #TAG", dispatcher.execute("/member", args, 123L));
        }
        assertTrue(requestedMemberTags.isEmpty());
    }

    @Test
    void memberRejectsMalformedTagsAndExtraArgumentsWithoutQueryingTheRepository() {
        for (String args : new String[]{"#", "##P1", "#P1 #P2", "#P1\n#P2", "<b>#P1</b>", "#P_1"}) {
            assertEquals("Некорректный тег. Используй: /member #TAG",
                    dispatcher.execute("/member", args, 123L));
        }
        assertTrue(requestedMemberTags.isEmpty());
    }

    @Test
    void memberNormalizesTagAndShowsMissingLocalData() {
        String text = dispatcher.execute("/member", "  #tag123  ", 123L);

        assertEquals(List.of("#TAG123"), requestedMemberTags);
        assertEquals("Участник <code>#TAG123</code> не найден в сохранённых данных клана", text);
        assertEquals(text, dispatcher.execute("/member", "tag123", 123L));
    }

    @Test
    void memberCardEscapesNamesAndShowsSeasonAndTrackedDonationTotals() {
        Member member = member("#P1", "Игрок ⚡<имя> & Co", 3);
        LeagueTier league = new LeagueTier(1, "Лига <I> & II", null, null);
        updateMember(member, "coLeader", league, 2500, 1800, 9000, 5000);
        updateMember(member, "coLeader", league, 2500, 1800, 120, 80);
        storedMembers = List.of(member);

        String text = dispatcher.execute("/member", "#P1", 123L);

        assertTrue(text.startsWith("<b>Игрок □&lt;имя&gt; &amp; Co</b>\n<code>#P1</code>"));
        assertFalse(text.contains("Статус:"));
        assertTrue(text.contains("Роль: Соруководитель"));
        assertTrue(text.contains("Место в клане: 3"));
        assertTrue(text.contains("Ратуша: 10"));
        assertTrue(text.contains("Уровень: 100"));
        assertTrue(text.contains("Лига: Лига &lt;I&gt; &amp; II"));
        assertTrue(text.contains("Трофеи: 2500"));
        assertTrue(text.contains("Трофеи деревни строителя: 1800"));
        assertTrue(text.contains("<b>Пожертвования за сезон</b>\nОтдано: 120\nПолучено: 80"));
        assertTrue(text.contains("<b>Пожертвования за всё время</b>\nОтдано: 9120\nПолучено: 5080"));
        assertFalse(text.contains("Последние сохранённые данные"));
    }

    @Test
    void departedMemberCardMarksHistoricalDataAndOmitsStaleClanRank() {
        Member member = member("#P1", "Ушедший", 3);
        member.leave(START);
        storedMembers = List.of(member);

        String text = dispatcher.execute("/member", "#P1", 123L);

        assertFalse(text.contains("Статус:"));
        assertTrue(text.contains("<i>Последние сохранённые данные участника</i>"));
        assertFalse(text.contains("Место в клане:"));
    }

    @Test
    void memberCardTranslatesKnownLeagueTierIdsInsteadOfUsingApiNames() {
        Member member = member("#P1", "Игрок", 1);
        storedMembers = List.of(member);
        int[] ids = {105000000, 105000001, 105000031, 105000036};
        String[] names = {"Без лиги", "Лига скелетов 1", "Лига электро 31", "Легендарная лига I"};

        for (int index = 0; index < ids.length; index++) {
            updateMember(member, member.getRole(),
                    new LeagueTier(ids[index], "Original API name", null, null),
                    member.getTrophies(), member.getBuilderBaseTrophies(),
                    member.getDonations(), member.getDonationsReceived());

            String text = dispatcher.execute("/member", "#P1", 123L);

            assertTrue(text.contains("Лига: " + names[index]));
            assertFalse(text.contains("Original API name"));
        }
    }

    @Test
    void memberCardHandlesMissingOptionalFieldsAndEscapesUnknownRole() {
        Member member = member("#P1", "Игрок", 1);
        updateMember(member, "<unknown>", null, null, null, 0, 0);
        storedMembers = List.of(member);

        String text = dispatcher.execute("/member", "#P1", 123L);

        assertTrue(text.contains("Роль: &lt;unknown&gt;"));
        assertTrue(text.contains("Лига: Нет данных"));
        assertTrue(text.contains("Трофеи: Нет данных"));
        assertTrue(text.contains("Трофеи деревни строителя: Нет данных"));
        assertFalse(text.contains("null"));
    }

    @Test
    void helpIncludesMemberCommandWithTagArgument() {
        assertTrue(execute("/help").contains("/member #TAG — Информация об участнике клана"));
    }

    private String execute(String command) {
        return dispatcher.execute(command, "", 123L);
    }

    private static War war(
            String state, int ourStars, int theirStars,
            double ourDestruction, double theirDestruction, boolean confirmed) {
        War war = new War(
                CLAN_TAG, "Наш <клан>", 7, ourStars, ourDestruction, 10,
                "#ENEMY", "Соперник & Co", 6, theirStars, theirDestruction, 10,
                state, 10, 2, "none",
                START.minusSeconds(24 * 60 * 60),
                START, START.plusSeconds(24 * 60 * 60));
        if (confirmed) {
            war.markWarLogSynced(START.plusSeconds(24 * 60 * 60));
        }
        return war;
    }

    private static RaidSeason season(String state, Integer attacks) {
        RaidSeason season = new RaidSeason();
        season.setState(state);
        season.setStartTime(START);
        season.setEndTime(START.plusSeconds(3 * 24 * 60 * 60));
        season.setCapitalTotalLoot(12000);
        season.setTotalAttacks(attacks);
        season.setRaidsCompleted(2);
        season.setEnemyDistrictsDestroyed(12);
        return season;
    }

    private List<RaidSeasonParticipant> participants(Integer... counts) {
        List<RaidSeasonParticipant> result = new ArrayList<>();
        for (int index = 0; index < counts.length; index++) {
            result.add(new RaidSeasonParticipant(
                    latestSeason, "#P" + index, "Игрок " + index,
                    counts[index], 5, 1, 1000));
        }
        return result;
    }

    private RaidSeasonClanMemberSnapshot snapshot(String tag, String name) {
        return new RaidSeasonClanMemberSnapshot(latestSeason, tag, name);
    }

    private static Member member(String tag, String name, int rank) {
        Clan clan = BeanUtils.instantiateClass(Clan.class);
        ReflectionTestUtils.setField(clan, "tag", CLAN_TAG);
        return new Member(
                tag, clan, name, "member", 10, 100,
                null, 0, 0, rank, rank, 0, 0, null);
    }

    private static void updateMember(
            Member member, String role, LeagueTier league,
            Integer trophies, Integer builderBaseTrophies,
            int donations, int donationsReceived) {
        member.update(
                member.getName(), role, member.getTownHallLevel(), member.getExpLevel(),
                league, trophies, builderBaseTrophies,
                member.getClanRank(), member.getPreviousClanRank(),
                donations, donationsReceived, member.getBuilderBaseLeague(), START);
    }

    @SuppressWarnings("unchecked")
    private static <T> T repository(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(), new Class<?>[]{type}, handler);
    }
}
