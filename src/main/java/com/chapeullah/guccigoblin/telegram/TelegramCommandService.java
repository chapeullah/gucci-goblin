package com.chapeullah.guccigoblin.telegram;

import com.chapeullah.guccigoblin.clan.repository.ClanRepository;
import com.chapeullah.guccigoblin.clan.model.Clan;
import com.chapeullah.guccigoblin.member.MemberService;
import com.chapeullah.guccigoblin.member.model.Member;
import com.chapeullah.guccigoblin.raidseason.model.RaidSeason;
import com.chapeullah.guccigoblin.raidseason.model.RaidSeasonParticipant;
import com.chapeullah.guccigoblin.raidseason.repository.RaidSeasonParticipantRepository;
import com.chapeullah.guccigoblin.raidseason.repository.RaidSeasonRepository;
import com.chapeullah.guccigoblin.war.model.War;
import com.chapeullah.guccigoblin.war.model.WarAttack;
import com.chapeullah.guccigoblin.war.model.WarParticipant;
import com.chapeullah.guccigoblin.war.repository.WarAttackRepository;
import com.chapeullah.guccigoblin.war.repository.WarParticipantRepository;
import com.chapeullah.guccigoblin.war.repository.WarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@DependsOn("environmentVariablesValidator")
public class TelegramCommandService {

    private final WarRepository warRepository;
    private final WarParticipantRepository participantRepository;
    private final WarAttackRepository attackRepository;
    private final RaidSeasonRepository raidSeasonRepository;
    private final RaidSeasonParticipantRepository raidSeasonParticipantRepository;
    private final MemberService memberService;
    private final ClanRepository clanRepository;

    @Value("${coc.clan-tag}")
    private String clanTag;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                    .withZone(ZoneId.of("Europe/Moscow"));

    private static final Map<String, String> CLAN_TYPES_RU = Map.ofEntries(
            Map.entry("open", "Открытый"),
            Map.entry("inviteOnly", "По приглашению"),
            Map.entry("closed", "Закрытый"));

    private static final Map<String, String> ROLE_NAMES_RU = Map.ofEntries(
            Map.entry("leader", "Глава"),
            Map.entry("coLeader", "Соруководитель"),
            Map.entry("admin", "Старейшина"),
            Map.entry("member", "Участник"));

    private static final Map<Integer, String> WAR_LEAGUE_NAMES_RU = Map.ofEntries(
            Map.entry(48000000, "Без лиги"),
            Map.entry(48000001, "Бронзовая лига III"),
            Map.entry(48000002, "Бронзовая лига II"),
            Map.entry(48000003, "Бронзовая лига I"),
            Map.entry(48000004, "Серебряная лига III"),
            Map.entry(48000005, "Серебряная лига II"),
            Map.entry(48000006, "Серебряная лига I"),
            Map.entry(48000007, "Золотая лига III"),
            Map.entry(48000008, "Золотая лига II"),
            Map.entry(48000009, "Золотая лига I"),
            Map.entry(48000010, "Хрустальная лига III"),
            Map.entry(48000011, "Хрустальная лига II"),
            Map.entry(48000012, "Хрустальная лига I"),
            Map.entry(48000013, "Лига мастеров III"),
            Map.entry(48000014, "Лига мастеров II"),
            Map.entry(48000015, "Лига мастеров I"),
            Map.entry(48000016, "Чемпионская лига III"),
            Map.entry(48000017, "Чемпионская лига II"),
            Map.entry(48000018, "Чемпионская лига I"),
            Map.entry(48000019, "Лига титанов III"),
            Map.entry(48000020, "Лига титанов II"),
            Map.entry(48000021, "Лига титанов I"),
            Map.entry(48000022, "Легендарная лига"));

    public String start() {
        return "Используй /help, чтобы посмотреть команды.";
    }

    public String help() {
        return """
                <b>Доступные команды</b>
                
                
                /help — Список команд

                /clan — Информация о клане
                /members — Текущий состав клана
                /war — Текущая война клана
                /wars — История войн клана
                /raid — Текущий сезон рейдов
                /raids — История рейдов
                
                /chatid — ID текущего чата
                """.strip();
    }

    public String getChatId(long chatId) {
        return "ID чата: <code>" + chatId + "</code>";
    }

    public String getMembersMessage() {
        List<Member> members = memberService.getCurrentMembersByClanTag(clanTag);
        StringBuilder message = new StringBuilder();
        if (members.isEmpty()) {
            return "Список участников клана пуст";
        }
        members.forEach(member -> message
                .append(String.format("<b>%02d.</b> ", member.getClanRank()))
                .append(TelegramUtils.escapeHtml(member.getName()))
                .append(" • ")
                .append(ROLE_NAMES_RU.getOrDefault(member.getRole(), member.getRole()))
                .append("\n"));
        return message.toString();
    }

    @Transactional(readOnly = true)
    public String getClanMessage() {
        Clan clan = clanRepository.findById(clanTag).orElse(null);

        if (clan == null) {
            return "Клан не найден: <code>" + TelegramUtils.escapeHtml(clanTag) + "</code>";
        }

        String type = CLAN_TYPES_RU.getOrDefault(clan.getType(), clan.getType());

        String warLeague = clan.getWarLeague() == null
                ? "Нет данных"
                : TelegramUtils.escapeHtml(clan.getWarLeague().getName());

        String location = clan.getLocation() == null
                ? "Не указано"
                : TelegramUtils.escapeHtml(clan.getLocation().getName());

        String description = clan.getDescription() == null
                || clan.getDescription().isBlank()
                ? "Без описания"
                : TelegramUtils.escapeHtml(clan.getDescription());

        return """
            <b>%s</b>
            <code>%s</code>

            📝 %s

            🏰 Уровень: %s
            👥 Участники: %s из 50
            🚪 Тип: %s
            🏆 Очки клана: %s
            🏛 Очки столицы: %s
            ⚔️ Военная лига: %s
            📈 Винрейт: %s
            ✅ Победы: %s
            🔥 Серия побед: %s
            📍 Расположение: %s
            """.formatted(
                    TelegramUtils.escapeHtml(clan.getName()),
                    TelegramUtils.escapeHtml(clan.getTag()),
                    description,
                    valueOrNoData(clan.getClanLevel()),
                    valueOrNoData(clan.getMemberCount()),
                    TelegramUtils.escapeHtml(type),
                    valueOrNoData(clan.getClanPoints()),
                    valueOrNoData(clan.getClanCapitalPoints()),
                    warLeague,
                    formatWarWinRate(clan),
                    valueOrNoData(clan.getWarWins()),
                    valueOrNoData(clan.getWarWinStreak()),
                    location)
                .strip();
    }

    private static String valueOrNoData(Object value) {
        return value == null ? "Нет данных" : value.toString();
    }

    private static String formatWarWinRate(Clan clan) {
        Integer wins = clan.getWarWins();
        Integer ties = clan.getWarTies();
        Integer losses = clan.getWarLosses();
        if (wins == null || ties == null || losses == null) {
            return "Нет данных";
        }
        int totalWars = wins + ties + losses;
        if (totalWars == 0) {
            return "Нет сыгранных войн";
        }
        double winRate = (wins + ties) * 100.0 / totalWars;
        return "%.1f%%".formatted(winRate);
    }

    public String getCurrentWarMessage() {
        War war = warRepository
                .findFirstByStateInOrderByStartTimeDesc(List.of("preparation", "inWar"))
                .orElse(null);
        if (war == null) {
            return "Сейчас активной войны нет";
        }
        String state = switch (war.getState()) {
            case "preparation" -> "Подготовка";
            case "inWar" -> "Идёт";
            default -> war.getState();
        };
        int maximumAttacks = war.getTeamSize() * war.getAttacksPerMember();
        String timeTitle;
        String time;
        if ("preparation".equals(war.getState())) {
            timeTitle = "Начало";
            time = DATE_TIME_FORMATTER.format(war.getStartTime());
        } else {
            timeTitle = "Окончание";
            time = DATE_TIME_FORMATTER.format(war.getEndTime());
        }
        return """
            <b>Текущая война</b>

            ⚔️ %s против %s
            📌 Статус: %s
            ⭐ Звёзды: %d — %d
            💥 Разрушение: %.2f%% — %.2f%%
            🗡 Атаки: %d из %d
            👥 Размер: %d на %d
            🕒 %s: %s
            """.formatted(
                    TelegramUtils.escapeHtml(war.getClanName()),
                    TelegramUtils.escapeHtml(war.getOpponentClanName()),
                    TelegramUtils.escapeHtml(state),
                    war.getClanStars(),
                    war.getOpponentClanStars(),
                    war.getClanDestructionPercentage(),
                    war.getOpponentClanDestructionPercentage(),
                    war.getClanAttacks(),
                    maximumAttacks,
                    war.getTeamSize(),
                    war.getTeamSize(),
                    timeTitle,
                    time)
                .strip();
    }

    public String wars() {
        List<War> wars = warRepository.findTop5ByStateOrderByEndTimeDesc("warEnded");
        if (wars.isEmpty()) {
            return "Завершённые войны не найдены.";
        }
        StringBuilder result = new StringBuilder("<b>Последние войны</b>\n");
        for (War war : wars) {
            String warResult = getWarResult(war);
            result.append("\n")
                    .append(warResult)
                    .append(" <b>")
                    .append(TelegramUtils.escapeHtml(war.getClanName()))
                    .append(" против ")
                    .append(TelegramUtils.escapeHtml(war.getOpponentClanName()))
                    .append("</b>\n")
                    .append("⭐ ")
                    .append(war.getClanStars())
                    .append(" — ")
                    .append(war.getOpponentClanStars())
                    .append("\n")
                    .append("💥 ")
                    .append("%.2f%% — %.2f%%".formatted(
                            war.getClanDestructionPercentage(),
                            war.getOpponentClanDestructionPercentage()))
                    .append("\n")
                    .append("📅 ")
                    .append(DATE_TIME_FORMATTER.format(war.getEndTime()))
                    .append("\n");
        }
        return result.toString().strip();
    }

    private String getWarResult(War war) {
        if (war.getClanStars() > war.getOpponentClanStars()) {
            return "✅ Победа";
        }

        if (war.getClanStars() < war.getOpponentClanStars()) {
            return "❌ Поражение";
        }

        int destructionComparison = Double.compare(
                war.getClanDestructionPercentage(),
                war.getOpponentClanDestructionPercentage());

        if (destructionComparison > 0) {
            return "✅ Победа";
        }

        if (destructionComparison < 0) {
            return "❌ Поражение";
        }

        return "➖ Ничья";
    }

    public String attacks() {
        War war = warRepository
                .findFirstByStateInOrderByStartTimeDesc(
                        List.of("preparation", "inWar"))
                .orElse(null);

        if (war == null) {
            return "Сейчас активной войны нет.";
        }

        if ("preparation".equals(war.getState())) {
            return "Война ещё не началась.";
        }

        List<WarParticipant> participants = participantRepository
                .findAllByWarAndClanTagOrderByMapPositionAsc(
                        war,
                        war.getClanTag());

        Map<String, Integer> usedAttacks = new HashMap<>();

        for (WarAttack attack : attackRepository.findAllByWar(war)) {
            usedAttacks.merge(
                    attack.getAttackerTag(),
                    1,
                    Integer::sum);
        }

        StringBuilder result = new StringBuilder(
                "<b>Неиспользованные атаки</b>\n");

        boolean hasUnusedAttacks = false;

        for (WarParticipant participant : participants) {
            int used = usedAttacks.getOrDefault(
                    participant.getPlayerTag(),
                    0);

            int remaining = Math.max(
                    0,
                    war.getAttacksPerMember() - used);

            if (remaining == 0) {
                continue;
            }

            hasUnusedAttacks = true;

            result.append("\n")
                    .append(participant.getMapPosition())
                    .append(". ")
                    .append(TelegramUtils.escapeHtml(participant.getPlayerName()))
                    .append(" — ")
                    .append(remaining)
                    .append(" из ")
                    .append(war.getAttacksPerMember());
        }

        if (!hasUnusedAttacks) {
            return "Все участники использовали свои атаки.";
        }

        return result.toString();
    }

    @Transactional(readOnly = true)
    public String raid() {
        RaidSeason season = raidSeasonRepository
                .findFirstByClanTagOrderByStartTimeDesc(clanTag)
                .orElse(null);

        if (season == null) {
            return "Рейды ещё не сохранены.";
        }

        List<RaidSeasonParticipant> participants =
                raidSeasonParticipantRepository.findAllByRaidSeason(season);
        String participantCount = hasCompleteRaidParticipants(season, participants)
                ? Long.toString(participants.stream().filter(p -> p.getAttackCount() > 0).count())
                : "данные ещё не загружены";

        return """
                <b>%s</b>

                📌 Статус: %s
                📅 %s (МСК)
                💰 Добыча: %d столичного золота
                🗡 Атаки: %d
                👥 Участников с атаками: %s
                🏰 Завершено рейдов: %d
                💥 Уничтожено районов: %d
                """.formatted(
                "ongoing".equals(season.getState()) ? "Текущий рейд" : "Последний рейд",
                TelegramUtils.escapeHtml(getRaidState(season)),
                getRaidPeriod(season),
                season.getCapitalTotalLoot(),
                season.getTotalAttacks(),
                participantCount,
                season.getRaidsCompleted(),
                season.getEnemyDistrictsDestroyed()).strip();
    }

    @Transactional(readOnly = true)
    public String raids() {
        List<RaidSeason> seasons = raidSeasonRepository
                .findTop5ByClanTagOrderByStartTimeDesc(clanTag);

        if (seasons.isEmpty()) {
            return "Рейды ещё не сохранены.";
        }

        StringBuilder result = new StringBuilder("<b>Последние рейды</b>\n");
        for (RaidSeason season : seasons) {
            result.append("\n📅 ").append(getRaidPeriod(season)).append(" (МСК)\n")
                    .append("📌 ").append(TelegramUtils.escapeHtml(getRaidState(season))).append("\n")
                    .append("💰 ").append(season.getCapitalTotalLoot()).append(" столичного золота\n")
                    .append("🗡 Атаки: ").append(season.getTotalAttacks())
                    .append(" · 💥 Районы: ").append(season.getEnemyDistrictsDestroyed()).append("\n");
        }
        return result.toString().strip();
    }

    private static boolean hasCompleteRaidParticipants(
            RaidSeason season, List<RaidSeasonParticipant> participants) {
        // TODO
        return true;
    }

    private static String getRaidPeriod(RaidSeason season) {
        return DATE_TIME_FORMATTER.format(season.getStartTime()) + " — "
                + DATE_TIME_FORMATTER.format(season.getEndTime());
    }

    private static String getRaidState(RaidSeason season) {
        return switch (season.getState()) {
            case "ongoing" -> "Идёт";
            case "ended" -> "Завершён";
            default -> season.getState();
        };
    }

}
