package com.chapeullah.guccigoblin.telegram;

import com.chapeullah.guccigoblin.member.Member;
import com.chapeullah.guccigoblin.member.MemberRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CommandService {

    private final MemberRepository memberRepository;
    private final WarRepository warRepository;
    private final WarParticipantRepository participantRepository;
    private final WarAttackRepository attackRepository;
    private final RaidSeasonRepository raidSeasonRepository;
    private final RaidSeasonParticipantRepository raidSeasonParticipantRepository;

    @Value("${coc.clanTag}")
    private String clanTag;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                    .withZone(ZoneId.of("Europe/Moscow"));

    public String start() {
        return "Используй /help, чтобы посмотреть команды.";
    }

    public String help() {
        return """
                <b>Доступные команды</b>

                /member <code>#TAG</code> — информация об участнике
                /members — список участников клана
                /top — рейтинг участников
                /war — текущая война
                /wars — история войн
                /attacks — неиспользованные атаки
                /raid — текущий или последний рейд
                /raidmissed — нынешние участники без атак в текущем или последнем рейде
                /raids — история рейдов
                """.strip();
    }

    public String member(String args) {
        if (args == null || args.isBlank()) {
            return "Нужен тег участника. Пример: /member #ABC123";
        }

        String tag = args.trim().split("\\s+")[0];
        tag = tag.toUpperCase();
        if (!tag.matches("^#[A-Z0-9]+$")) {
            return "Неверный формат тега. Пример: #ABC123";
        }

        Member member = memberRepository
                .findByTag(tag)
                .orElse(null);
        if (member == null) {
            return "Участник с тегом " + tag + " не найден";
        }

        String joinedAt = DATE_TIME_FORMATTER.format(member.getJoinedAt());
        String lastActivity = DATE_TIME_FORMATTER.format(member.getLastActivity());

        return """
        <b>%d. %s</b>
        <code>%s</code>

        🏠 Ратуша: %d
        ⭐ Уровень: %d
        🏆 Лига: %s
        👤 Роль: %s

        <b>Пожертвования</b>
        Текущие: %d отдано · %d получено
        Всего: %d отдано · %d получено

        <b>Деревня строителя</b>
        🏆 Лига: %s
        🏅 Кубки: %d

        📅 Дата вступления: %s
        🕒 Последняя активность: %s
        """.formatted(
                member.getClanRank(),
                escapeHtml(member.getName()),
                member.getTag(),
                member.getTownHallLevel(),
                member.getExpLevel(),
                escapeHtml(member.getLeagueTierName()),
                escapeHtml(member.getRole()),
                member.getDonations(),
                member.getDonationsReceived(),
                member.getTotalDonations(),
                member.getTotalDonationsReceived(),
                escapeHtml(member.getBuilderBaseLeagueName()),
                member.getBuilderBaseTrophies(),
                joinedAt,
                lastActivity).strip();
    }

    public String members() {
        List<Member> members = memberRepository
                .findAllByInClanTrue()
                .stream()
                .sorted(Comparator.comparing(Member::getClanRank))
                .toList();
        if (members.isEmpty()) {
            return "Участники клана не найдены.";
        }
        StringBuilder result = new StringBuilder()
                .append("Участники клана ")
                .append(members.size())
                .append("\n");
        for (Member member : members) {
            result.append("\n")
                    .append(member.getClanRank())
                    .append(" — <code>")
                    .append(member.getTag())
                    .append("</code> — <code>")
                    .append(escapeHtml(member.getName()))
                    .append("</code> — TH")
                    .append(member.getTownHallLevel());
        }
        return result.toString();
    }

    public String top() {
        List<Member> members = memberRepository
                .findAllByInClanTrue()
                .stream()
                .sorted(
                        Comparator.comparingInt(Member::getDonations)
                                .reversed()
                                .thenComparingInt(Member::getClanRank))
                .limit(10)
                .toList();

        if (members.isEmpty()) {
            return "Участники клана не найдены.";
        }

        StringBuilder result =
                new StringBuilder("<b>Топ по пожертвованиям</b>\n");

        for (int i = 0; i < members.size(); i++) {
            Member member = members.get(i);

            result.append("\n")
                    .append(i + 1)
                    .append(". <b>")
                    .append(escapeHtml(member.getName()))
                    .append("</b> — ")
                    .append(member.getDonations())
                    .append(" отдано · ")
                    .append(member.getDonationsReceived())
                    .append(" получено");
        }

        return result.toString();
    }

    public String war() {
        War war = warRepository
                .findFirstByStateInOrderByStartTimeDesc(
                        List.of("preparation", "inWar"))
                .orElse(null);

        if (war == null) {
            return "Сейчас активной войны нет.";
        }

        String state = switch (war.getState()) {
            case "preparation" -> "Подготовка";
            case "inWar" -> "Идёт";
            default -> war.getState();
        };

        int maximumAttacks =
                war.getTeamSize() * war.getAttacksPerMember();

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
                escapeHtml(war.getClanName()),
                escapeHtml(war.getOpponentName()),
                escapeHtml(state),
                war.getClanStars(),
                war.getOpponentStars(),
                war.getClanDestructionPercentage(),
                war.getOpponentDestructionPercentage(),
                war.getClanAttacks(),
                maximumAttacks,
                war.getTeamSize(),
                war.getTeamSize(),
                timeTitle,
                time
        ).strip();
    }

    public String wars() {
        List<War> wars = warRepository
                .findTop5ByStateOrderByEndTimeDesc("warEnded");

        if (wars.isEmpty()) {
            return "Завершённые войны не найдены.";
        }

        StringBuilder result = new StringBuilder(
                "<b>Последние войны</b>\n");

        for (War war : wars) {
            String warResult = getWarResult(war);

            result.append("\n")
                    .append(warResult)
                    .append(" <b>")
                    .append(escapeHtml(war.getClanName()))
                    .append(" против ")
                    .append(escapeHtml(war.getOpponentName()))
                    .append("</b>\n")
                    .append("⭐ ")
                    .append(war.getClanStars())
                    .append(" — ")
                    .append(war.getOpponentStars())
                    .append("\n")
                    .append("💥 ")
                    .append("%.2f%% — %.2f%%".formatted(
                            war.getClanDestructionPercentage(),
                            war.getOpponentDestructionPercentage()))
                    .append("\n")
                    .append("📅 ")
                    .append(DATE_TIME_FORMATTER.format(war.getEndTime()))
                    .append("\n");
        }

        return result.toString().strip();
    }

    private String getWarResult(War war) {
        if (war.getClanStars() > war.getOpponentStars()) {
            return "✅ Победа";
        }

        if (war.getClanStars() < war.getOpponentStars()) {
            return "❌ Поражение";
        }

        int destructionComparison = Double.compare(
                war.getClanDestructionPercentage(),
                war.getOpponentDestructionPercentage());

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
                    .append(escapeHtml(participant.getPlayerName()))
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
                ? Long.toString(participants.stream().filter(p -> p.getAttacks() > 0).count())
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
                escapeHtml(getRaidState(season)),
                getRaidPeriod(season),
                season.getCapitalTotalLoot(),
                season.getTotalAttacks(),
                participantCount,
                season.getRaidsCompleted(),
                season.getEnemyDistrictsDestroyed()).strip();
    }

    @Transactional(readOnly = true)
    public String raidMissed() {
        RaidSeason season = raidSeasonRepository
                .findFirstByClanTagOrderByStartTimeDesc(clanTag)
                .orElse(null);

        if (season == null) {
            return "Рейды ещё не сохранены.";
        }

        StringBuilder result = new StringBuilder("<b>Без атак в рейде</b>\n")
                .append("📌 Статус: ").append(escapeHtml(getRaidState(season)))
                .append("\n📅 ").append(getRaidPeriod(season)).append(" (МСК)\n")
                .append("👥 Текущий состав клана\n");

        List<RaidSeasonParticipant> participants =
                raidSeasonParticipantRepository.findAllByRaidSeason(season);
        if (!hasCompleteRaidParticipants(season, participants)) {
            return result.append("\nДанные об участниках рейда пока неполные. "
                    + "Повтори команду после синхронизации.").toString();
        }

        List<Member> members = memberRepository.findAllByInClanTrue().stream()
                .sorted(Comparator.comparingInt(Member::getClanRank).thenComparing(Member::getTag))
                .toList();
        if (members.isEmpty()) {
            return result.append("\nТекущие участники клана не найдены.").toString();
        }

        Set<String> attackedTags = new HashSet<>();
        for (RaidSeasonParticipant participant : participants) {
            if (participant.getAttacks() > 0) {
                attackedTags.add(participant.getTag());
            }
        }

        int missed = 0;
        for (Member member : members) {
            if (attackedTags.contains(member.getTag())) {
                continue;
            }

            result.append("\n").append(++missed).append(". <b>")
                    .append(escapeHtml(member.getName())).append("</b> — <code>")
                    .append(escapeHtml(member.getTag())).append("</code>");
        }

        if (missed == 0) {
            return result.append("\nВсе нынешние участники клана сделали хотя бы одну атаку.")
                    .toString();
        }

        return result.append("\n\nВсего без атак: ").append(missed).toString();
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
                    .append("📌 ").append(escapeHtml(getRaidState(season))).append("\n")
                    .append("💰 ").append(season.getCapitalTotalLoot()).append(" столичного золота\n")
                    .append("🗡 Атаки: ").append(season.getTotalAttacks())
                    .append(" · 💥 Районы: ").append(season.getEnemyDistrictsDestroyed()).append("\n");
        }
        return result.toString().strip();
    }

    private static boolean hasCompleteRaidParticipants(
            RaidSeason season, List<RaidSeasonParticipant> participants) {
        return participants.stream().mapToLong(p -> p.getAttacks()).sum() == season.getTotalAttacks();
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

    private static String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

}
