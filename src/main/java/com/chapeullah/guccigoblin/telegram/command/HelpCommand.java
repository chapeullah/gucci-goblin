package com.chapeullah.guccigoblin.telegram.command;

import com.chapeullah.guccigoblin.telegram.TelegramCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HelpCommand implements BotCommand {

    private final TelegramCommandService telegramCommandService;

    @Override
    public String name() {
        return "/help";
    }

    @Override
    public String execute(String args) {
        return telegramCommandService.help();
    }

}
