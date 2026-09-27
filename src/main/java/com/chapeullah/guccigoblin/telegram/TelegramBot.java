package com.chapeullah.guccigoblin.telegram;

import com.chapeullah.guccigoblin.telegram.dto.Message;
import com.chapeullah.guccigoblin.telegram.dto.Update;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramBot {

    private final TelegramClient telegramClient;
    private final CommandDispatcher commandDispatcher;

    private long nextOffset;

    @Scheduled(fixedDelayString = "${telegram.bot.poll-delay-ms:1000}")
    public void poll() {
        try {
            for (var update : telegramClient.getUpdates(nextOffset)) {
                nextOffset = Math.max(nextOffset, update.updateId() + 1);
                handle(update);
            }
        } catch (RuntimeException exception) {
            log.warn("Telegram polling failed", exception);
        }
    }

    private void handle(Update update) {
        Message message = update.message();

        if (message == null || message.chat() == null || message.text() == null) {
            return;
        }

        long chatId = message.chat().id();
        String messageString = message.text().trim();
        if(messageString.isEmpty()) {
            telegramClient.sendMessage(chatId, "Неверный ввод.");
        }

        String[] messageParts = message.text().trim().split("\\s+", 2);
        String command = messageParts[0];
        String args = messageParts.length == 2 ? messageParts[1] : "";

        telegramClient.sendMessage(
                chatId,
                commandDispatcher.execute(command, args));
    }

}