package com.chapeullah.guccigoblin.telegram;

import com.chapeullah.guccigoblin.telegram.dto.TelegramMessage;
import com.chapeullah.guccigoblin.telegram.dto.TelegramUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramBot {

    private final TelegramClient telegramClient;
    private final TelegramCommandDispatcher telegramCommandDispatcher;

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

    private void handle(TelegramUpdate telegramUpdate) {
        TelegramMessage telegramMessage = telegramUpdate.telegramMessage();

        if (telegramMessage == null || telegramMessage.telegramChat() == null || telegramMessage.text() == null) {
            return;
        }

        long chatId = telegramMessage.telegramChat().id();
        String messageString = telegramMessage.text().trim();
        if(messageString.isEmpty()) {
            telegramClient.sendMessage(chatId, "Неверный ввод.");
        }

        String[] messageParts = telegramMessage.text().trim().split("\\s+", 2);
        String command = messageParts[0];
        String args = messageParts.length == 2 ? messageParts[1] : "";

        telegramClient.sendMessage(
                chatId,
                telegramCommandDispatcher.execute(command, args));
    }

}