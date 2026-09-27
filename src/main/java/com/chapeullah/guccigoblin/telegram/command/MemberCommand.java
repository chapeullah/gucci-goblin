package com.chapeullah.guccigoblin.telegram.command;

import com.chapeullah.guccigoblin.member.Member;
import com.chapeullah.guccigoblin.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class MemberCommand implements BotCommand {

    private final MemberRepository memberRepository;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                    .withZone(ZoneId.of("Europe/Moscow"));

    @Override
    public String name() {
        return "/member";
    }

    @Override
    public String execute(String args) {
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

        String joined = DATE_TIME_FORMATTER.format(member.getJoined());
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
                member.getName(),
                member.getTag(),
                member.getTownHallLevel(),
                member.getExpLevel(),
                member.getLeagueTierName(),
                member.getRole(),
                member.getDonations(),
                member.getDonationsReceived(),
                member.getTotalDonations(),
                member.getTotalDonationsReceived(),
                member.getBuilderBaseLeagueName(),
                member.getBuilderBaseTrophies(),
                joined,
                lastActivity).strip();
    }

}
