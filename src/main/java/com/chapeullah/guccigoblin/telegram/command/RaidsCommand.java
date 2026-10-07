package com.chapeullah.guccigoblin.telegram.command;

import com.chapeullah.guccigoblin.telegram.TelegramCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RaidsCommand implements BotCommand {

    private final TelegramCommandService telegramCommandService;

    @Override
    public String name() {
        return "/raids";
    }

    @Override
    public String execute(String args) {
        return telegramCommandService.raids();
    }

}
