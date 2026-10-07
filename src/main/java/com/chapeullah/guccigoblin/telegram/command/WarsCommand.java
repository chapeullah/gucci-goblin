package com.chapeullah.guccigoblin.telegram.command;

import com.chapeullah.guccigoblin.telegram.TelegramCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WarsCommand implements BotCommand {

    private final TelegramCommandService telegramCommandService;

    @Override
    public String name() {
        return "/wars";
    }

    @Override
    public String execute(String args) {
        return telegramCommandService.wars();
    }

}
