package com.chapeullah.guccigoblin.telegram;

import com.chapeullah.guccigoblin.member.Member;
import com.chapeullah.guccigoblin.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramBot {

    private final TelegramClient telegramClient;
    private final MemberRepository memberRepository;

    private long nextOffset;

    @Scheduled(
            fixedDelayString =
                    "${telegram.bot.poll-delay-ms:1000}")
    public void poll() {
        try {
            for (var update :
                    telegramClient.getUpdates(nextOffset)) {

                nextOffset = Math.max(
                        nextOffset,
                        update.updateId() + 1);

                handle(update);
            }
        } catch (RuntimeException exception) {
            // Не выводим сообщение исключения:
            // URL может содержать Telegram-токен.
            log.warn(
                    "Telegram polling failed: {}",
                    exception.getClass().getSimpleName());
        }
    }

    private void handle(TelegramClient.Update update) {
        var message = update.message();

        if (message == null
                || message.chat() == null
                || message.text() == null) {
            return;
        }

        String command = message.text()
                .trim()
                .split("\\s+", 2)[0]
                .split("@", 2)[0];

        switch (command) {
            case "/start", "/help" ->
                    telegramClient.sendMessage(
                            message.chat().id(),
                            "Доступные команды:\n" +
                                    "/members — участники клана");

            case "/members" ->
                    telegramClient.sendMessage(
                            message.chat().id(),
                            buildMembersMessage());

            default -> {
                // Обычные сообщения игнорируем.
            }
        }
    }

    private String buildMembersMessage() {
        var members = memberRepository
                .findAllByInClanTrue()
                .stream()
                .sorted(Comparator.comparing(
                        Member::getClanRank))
                .toList();

        if (members.isEmpty()) {
            return "Участники клана ещё не загружены.";
        }

        StringBuilder result = new StringBuilder()
                .append("Участники клана: ")
                .append(members.size())
                .append("\n\n");

        for (Member member : members) {
            result.append(member.getClanRank())
                    .append(". ")
                    .append(member.getName())
                    .append(" — TH")
                    .append(member.getTownHallLevel())
                    .append("\n");
        }

        return result.toString();
    }
}