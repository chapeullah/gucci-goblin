package com.chapeullah.guccigoblin.telegram;

import com.chapeullah.guccigoblin.member.Member;
import com.chapeullah.guccigoblin.member.MemberRepository;
import com.chapeullah.guccigoblin.war.model.War;
import com.chapeullah.guccigoblin.war.model.WarAttack;
import com.chapeullah.guccigoblin.war.model.WarParticipant;
import com.chapeullah.guccigoblin.war.repository.WarAttackRepository;
import com.chapeullah.guccigoblin.war.repository.WarParticipantRepository;
import com.chapeullah.guccigoblin.war.repository.WarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommandService {

    private final MemberRepository memberRepository;
    private final WarRepository warRepository;
    private final WarParticipantRepository participantRepository;
    private final WarAttackRepository attackRepository;

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

        StringBuilder result = new StringBuilder(
                "<b>Топ по пожертвованиям</b>\n");

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
                .findFirstByStateInOrderByStartsAtDesc(
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
            time = DATE_TIME_FORMATTER.format(war.getStartsAt());
        } else {
            timeTitle = "Окончание";
            time = DATE_TIME_FORMATTER.format(war.getEndsAt());
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
                .findTop5ByStateOrderByEndsAtDesc("warEnded");

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
                    .append(DATE_TIME_FORMATTER.format(war.getEndsAt()))
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
                .findFirstByStateInOrderByStartsAtDesc(
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

    private static String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

}
