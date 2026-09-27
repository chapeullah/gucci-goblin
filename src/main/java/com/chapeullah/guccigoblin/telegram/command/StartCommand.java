package com.chapeullah.guccigoblin.telegram.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartCommand implements BotCommand{

    @Override
    public String name() {
        return "/start";
    }

    @Override
    public String execute(String args) {
        return "Старт";
    }

}
